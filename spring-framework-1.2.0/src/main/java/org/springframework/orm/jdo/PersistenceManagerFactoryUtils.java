/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.orm.jdo;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOOptimisticVerificationException;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class featuring methods for JDO PersistenceManager handling,
 * allowing for reuse of PersistenceManager instances within transactions.
 *
 * <p>Used by JdoTemplate, JdoInterceptor, and JdoTransactionManager.
 * Can also be used directly in application code, e.g. in combination
 * with JdoInterceptor.
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see JdoTemplate
 * @see JdoInterceptor
 * @see JdoTransactionManager
 */
public abstract class PersistenceManagerFactoryUtils {

	/**
	 * Order value for TransactionSynchronization objects that clean up JDO
	 * PersistenceManagers. Return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100
	 * to execute PersistenceManager cleanup before JDBC Connection cleanup, if any.
	 * @see DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
	 */
	public static final int PERSISTENCE_MANAGER_SYNCHRONIZATION_ORDER =
			DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100;

	private static final Log logger = LogFactory.getLog(PersistenceManagerFactoryUtils.class);


	/**
	 * Create an appropriate SQLExceptionTranslator for the given PersistenceManagerFactory.
	 * If a DataSource is found, create a SQLErrorCodeSQLExceptionTranslator for the
	 * DataSource; else, fall back to a SQLStateSQLExceptionTranslator.
	 * @param pmf the PersistenceManagerFactory to create the translator for
	 * @return the SQLExceptionTranslator
	 * @see PersistenceManagerFactory#getConnectionFactory
	 * @see SQLErrorCodeSQLExceptionTranslator
	 * @see SQLStateSQLExceptionTranslator
	 */
	public static SQLExceptionTranslator newJdbcExceptionTranslator(PersistenceManagerFactory pmf) {
		if (pmf != null) {
			// Check for PersistenceManagerFactory's DataSource.
			Object cf = pmf.getConnectionFactory();
			if (cf instanceof DataSource) {
				return new SQLErrorCodeSQLExceptionTranslator((DataSource) cf);
			}
		}
		return new SQLStateSQLExceptionTranslator();
	}

	/**
	 * Get a JDO PersistenceManager via the given factory. Is aware of a
	 * corresponding PersistenceManager bound to the current thread,
	 * for example when using JdoTransactionManager. Will create a new
	 * PersistenceManager else, if allowCreate is true.
	 * @param pmf PersistenceManagerFactory to create the PersistenceManager with
	 * @param allowCreate if a non-transactional PersistenceManager should be created
	 * when no transactional PersistenceManager can be found for the current thread
	 * @return the PersistenceManager
	 * @throws DataAccessResourceFailureException if the PersistenceManager couldn't be created
	 * @throws IllegalStateException if no thread-bound PersistenceManager found and allowCreate false
	 */
	public static PersistenceManager getPersistenceManager(PersistenceManagerFactory pmf, boolean allowCreate)
	    throws DataAccessResourceFailureException, IllegalStateException {

		try {
			return doGetPersistenceManager(pmf, allowCreate);
		}
		catch (JDOException ex) {
			throw new DataAccessResourceFailureException("Could not open JDO PersistenceManager", ex);
		}
	}

	/**
	 * Get a JDO PersistenceManager via the given factory. Is aware of a
	 * corresponding PersistenceManager bound to the current thread,
	 * for example when using JdoTransactionManager. Will create a new
	 * PersistenceManager else, if allowCreate is true.
	 * <p>Same as <code>getPersistenceManager</code>, but throwing the original JDOException.
	 * @param pmf PersistenceManagerFactory to create the PersistenceManager with
	 * @param allowCreate if a non-transactional PersistenceManager should be created
	 * when no transactional PersistenceManager can be found for the current thread
	 * @return the PersistenceManager
	 * @throws JDOException if the PersistenceManager couldn't be created
	 * @throws IllegalStateException if no thread-bound PersistenceManager found and allowCreate false
	 */
	public static PersistenceManager doGetPersistenceManager(PersistenceManagerFactory pmf, boolean allowCreate)
	    throws JDOException, IllegalStateException {

		Assert.notNull(pmf, "No PersistenceManagerFactory specified");

		PersistenceManagerHolder pmHolder =
				(PersistenceManagerHolder) TransactionSynchronizationManager.getResource(pmf);
		if (pmHolder != null) {
			return pmHolder.getPersistenceManager();
		}

		if (!allowCreate && !TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("No JDO PersistenceManager bound to thread, " +
					"and configuration does not allow creation of non-transactional one here");
		}

		logger.debug("Opening JDO PersistenceManager");
		PersistenceManager pm = pmf.getPersistenceManager();

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering transaction synchronization for JDO PersistenceManager");
			// Use same PersistenceManager for further JDO actions within the transaction
			// thread object will get removed by synchronization at transaction completion.
			pmHolder = new PersistenceManagerHolder(pm);
			pmHolder.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.registerSynchronization(
					new PersistenceManagerSynchronization(pmHolder, pmf));
			TransactionSynchronizationManager.bindResource(pmf, pmHolder);
		}

		return pm;
	}

	/**
	 * Apply the current transaction timeout, if any, to the given JDO Query object.
	 * @param query the JDO Query object
	 * @param pmf JDO PersistenceManagerFactory that the Query was created for
	 * @param jdoDialect the JdoDialect to use for applying a query timeout
	 * (must not be null)
	 * @see JdoDialect#applyQueryTimeout
	 */
	public static void applyTransactionTimeout(
			Query query, PersistenceManagerFactory pmf, JdoDialect jdoDialect) throws JDOException {

		Assert.notNull(query, "No Query object specified");
		PersistenceManagerHolder pmHolder =
		    (PersistenceManagerHolder) TransactionSynchronizationManager.getResource(pmf);
		if (pmHolder != null && pmHolder.hasTimeout()) {
			jdoDialect.applyQueryTimeout(query, pmHolder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Convert the given JDOException to an appropriate exception from the
	 * org.springframework.dao hierarchy.
	 * <p>The most important cases like object not found or optimistic verification
	 * failure are covered here. For more fine-granular conversion, JdoAccessor and
	 * JdoTransactionManager support sophisticated translation of exceptions via a
	 * JdoDialect.
	 * @param ex JDOException that occured
	 * @return the corresponding DataAccessException instance
	 * @see JdoAccessor#convertJdoAccessException
	 * @see JdoTransactionManager#convertJdoAccessException
	 * @see JdoDialect#translateException
	 */
	public static DataAccessException convertJdoAccessException(JDOException ex) {
		if (ex instanceof JDOObjectNotFoundException) {
			throw new JdoObjectRetrievalFailureException((JDOObjectNotFoundException) ex);
		}
		else if (ex instanceof JDOOptimisticVerificationException) {
			throw new JdoOptimisticLockingFailureException((JDOOptimisticVerificationException) ex);
		}
		else if (ex instanceof JDODataStoreException) {
			return new JdoResourceFailureException((JDODataStoreException) ex);
		}
		else if (ex instanceof JDOFatalDataStoreException) {
			return new JdoResourceFailureException((JDOFatalDataStoreException) ex);
		}
		else if (ex instanceof JDOUserException) {
			return new JdoUsageException((JDOUserException) ex);
		}
		else if (ex instanceof JDOFatalUserException) {
			return new JdoUsageException((JDOFatalUserException) ex);
		}
		else {
			// fallback: assuming internal exception
			return new JdoSystemException(ex);
		}
	}

	/**
	 * Close the given PersistenceManager, created via the given factory,
	 * if it isn't bound to the thread.
	 * @deprecated in favor of releasePersistenceManager
	 * @see #releasePersistenceManager
	 */
	public static void closePersistenceManagerIfNecessary(PersistenceManager pm, PersistenceManagerFactory pmf) {
		releasePersistenceManager(pm, pmf);
	}

	/**
	 * Close the given PersistenceManager, created via the given factory,
	 * if it is not managed externally (i.e. not bound to the thread).
	 * @param pm PersistenceManager to close
	 * @param pmf PersistenceManagerFactory that the PersistenceManager was created with
	 * (can be null)
	 */
	public static void releasePersistenceManager(PersistenceManager pm, PersistenceManagerFactory pmf) {
		try {
			doReleasePersistenceManager(pm, pmf);
		}
		catch (JDOException ex) {
			logger.error("Could not close JDO PersistenceManager", ex);
		}
		catch (RuntimeException ex) {
			logger.error("Unexpected exception on closing JDO PersistenceManager", ex);
		}
	}

	/**
	 * Actually release a PersistenceManager for the given factory.
	 * Same as <code>releasePersistenceManager</code>, but throwing the original JDOException.
	 * @param pm PersistenceManager to close
	 * @param pmf PersistenceManagerFactory that the PersistenceManager was created with
	 * (can be null)
	 * @throws JDOException if thrown by JDO methods
	 */
	public static void doReleasePersistenceManager(PersistenceManager pm, PersistenceManagerFactory pmf)
			throws JDOException {

		if (pm == null) {
			return;
		}

		if (pmf != null) {
			PersistenceManagerHolder pmHolder =
					(PersistenceManagerHolder) TransactionSynchronizationManager.getResource(pmf);
			if (pmHolder != null && pm == pmHolder.getPersistenceManager()) {
				// It's the transactional PersistenceManager: Don't close it.
				return;
			}
		}

		logger.debug("Closing JDO PersistenceManager");
		pm.close();
	}


	/**
	 * Callback for resource cleanup at the end of a non-JDO transaction
	 * (e.g. when participating in a JtaTransactionManager transaction).
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class PersistenceManagerSynchronization extends TransactionSynchronizationAdapter {

		private final PersistenceManagerHolder persistenceManagerHolder;

		private final PersistenceManagerFactory persistenceManagerFactory;

		public PersistenceManagerSynchronization(PersistenceManagerHolder pmHolder, PersistenceManagerFactory pmf) {
			this.persistenceManagerHolder = pmHolder;
			this.persistenceManagerFactory = pmf;
		}

		public int getOrder() {
			return PERSISTENCE_MANAGER_SYNCHRONIZATION_ORDER;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.persistenceManagerFactory);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.persistenceManagerFactory, this.persistenceManagerHolder);
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.persistenceManagerFactory);
			releasePersistenceManager(
			    this.persistenceManagerHolder.getPersistenceManager(),
			    this.persistenceManagerFactory);
		}
	}

}
