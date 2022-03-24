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

package org.springframework.orm.hibernate3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.JDBCException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.PersistentObjectException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.TransientObjectException;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.WrongClassException;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;

import org.springframework.core.Ordered;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class featuring methods for Hibernate Session handling,
 * allowing for reuse of Hibernate Session instances within transactions.
 *
 * <p>Supports synchronization with both Spring-managed JTA transactions
 * (i.e. JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA
 * or EJB CMT). See the <code>getSession</code> version with all parameters
 * for details.
 *
 * <p>Used internally by HibernateTemplate, HibernateInterceptor, and
 * HibernateTransactionManager. Can also be used directly in application code,
 * e.g. in combination with HibernateInterceptor.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see HibernateTemplate
 * @see HibernateInterceptor
 * @see HibernateTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public abstract class SessionFactoryUtils {

	/**
	 * Order value for TransactionSynchronization objects that clean up Hibernate
	 * Sessions. Return DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100
	 * to execute Session cleanup before JDBC Connection cleanup, if any.
	 * @see DataSourceUtils#CONNECTION_SYNCHRONIZATION_ORDER
	 */
	public static final int SESSION_SYNCHRONIZATION_ORDER =
			DataSourceUtils.CONNECTION_SYNCHRONIZATION_ORDER - 100;

	private static final Log logger = LogFactory.getLog(SessionFactoryUtils.class);

	private static ThreadLocal deferredCloseHolder = new ThreadLocal();


	/**
	 * Determine the DataSource of the given SessionFactory.
	 * @param sessionFactory the SessionFactory to check
	 * @return the DataSource, or null if none found
	 * @see org.hibernate.engine.SessionFactoryImplementor#getConnectionProvider
	 * @see LocalDataSourceConnectionProvider
	 */
	public static DataSource getDataSource(SessionFactory sessionFactory) {
		if (sessionFactory instanceof SessionFactoryImplementor) {
			ConnectionProvider cp = ((SessionFactoryImplementor) sessionFactory).getConnectionProvider();
			if (cp instanceof LocalDataSourceConnectionProvider) {
				return ((LocalDataSourceConnectionProvider) cp).getDataSource();
			}
		}
		return null;
	}

	/**
	 * Create an appropriate SQLExceptionTranslator for the given SessionFactory.
	 * If a DataSource is found, a SQLErrorCodeSQLExceptionTranslator for the DataSource
	 * is created; else, a SQLStateSQLExceptionTranslator as fallback.
	 * @param sessionFactory the SessionFactory to create the translator for
	 * @return the SQLExceptionTranslator
	 * @see #getDataSource
	 * @see SQLErrorCodeSQLExceptionTranslator
	 * @see SQLStateSQLExceptionTranslator
	 */
	public static SQLExceptionTranslator newJdbcExceptionTranslator(SessionFactory sessionFactory) {
		DataSource ds = getDataSource(sessionFactory);
		if (ds != null) {
			return new SQLErrorCodeSQLExceptionTranslator(ds);
		}
		return new SQLStateSQLExceptionTranslator();
	}

	/**
	 * Try to retrieve the JTA TransactionManager from the given SessionFactory
	 * and/or Session. Check the passed-in SessionFactory for implementing
	 * SessionFactoryImplementor (the usual case), falling back to the
	 * SessionFactory reference that the Session itself carries (for example,
	 * when using Hibernate's JCA Connector, i.e. JCASessionFactoryImpl).
	 * @param sessionFactory Hibernate SessionFactory
	 * @param session Hibernate Session (can also be null)
	 * @return the JTA TransactionManager, if any
	 * @see TransactionManager
	 * @see SessionFactoryImplementor#getTransactionManager
	 * @see Session#getSessionFactory
	 * @see org.hibernate.impl.SessionFactoryImpl
	 * @see org.hibernate.jca.JCASessionFactoryImpl
	 */
	public static TransactionManager getJtaTransactionManager(SessionFactory sessionFactory, Session session) {
		SessionFactoryImplementor sessionFactoryImpl = null;
		if (sessionFactory instanceof SessionFactoryImplementor) {
			sessionFactoryImpl = ((SessionFactoryImplementor) sessionFactory);
		}
		else if (session != null) {
			SessionFactory internalFactory = session.getSessionFactory();
			if (internalFactory instanceof SessionFactoryImplementor) {
				sessionFactoryImpl = (SessionFactoryImplementor) internalFactory;
			}
		}
		return (sessionFactoryImpl != null ? sessionFactoryImpl.getTransactionManager() : null);
	}


	/**
	 * Get a Hibernate Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using HibernateTransactionManager. Will create a new Session
	 * otherwise, if allowCreate is true.
	 * <p>This is the <code>getSession</code> method used by typical data access code,
	 * in combination with <code>releaseSession</code> called when done with
	 * the Session. Note that HibernateTemplate allows to write data access code
	 * without caring about such resource handling.
	 * <p>Supports synchronization with both Spring-managed JTA transactions
	 * (i.e. JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA
	 * or EJB CMT). See the <code>getSession</code> version with all parameters
	 * for details.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param allowCreate if a non-transactional Session should be created when no
	 * transactional Session can be found for the current thread
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see #getSession(SessionFactory, Interceptor, SQLExceptionTranslator)
	 * @see #releaseSession
	 * @see HibernateTemplate
	 */
	public static Session getSession(SessionFactory sessionFactory, boolean allowCreate)
	    throws DataAccessResourceFailureException, IllegalStateException {

		try {
			return doGetSession(sessionFactory, null, null, allowCreate);
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
		}
	}

	/**
	 * Get a Hibernate Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using HibernateTransactionManager. Will always create a new
	 * Session otherwise.
	 * <p>Supports synchronization with Spring-managed JTA transactions
	 * (i.e. JtaTransactionManager) via TransactionSynchronizationManager, to allow
	 * for transaction-scoped Hibernate Sessions and proper transactional handling
	 * of the JVM-level cache.
	 * <p>Supports synchronization with non-Spring JTA transactions (i.e. plain JTA
	 * or EJB CMT) via TransactionSynchronizationManager, to allow for
	 * transaction-scoped Hibernate Sessions without JtaTransactionManager.
	 * This only applies when a JTA TransactionManagerLookup is specified in the
	 * Hibernate configuration.
	 * <p>Supports setting a Session-level Hibernate entity interceptor that allows
	 * to inspect and change property values before writing to and reading from the
	 * database. Such an interceptor can also be set at the SessionFactory level
	 * (i.e. on LocalSessionFactoryBean), on HibernateTransactionManager, or on
	 * HibernateInterceptor/HibernateTemplate.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param entityInterceptor Hibernate entity interceptor, or null if none
	 * @param jdbcExceptionTranslator SQLExcepionTranslator to use for flushing the
	 * Session on transaction synchronization (can be null; only used when actually
	 * registering a transaction synchronization)
	 * @return the Hibernate Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @see LocalSessionFactoryBean#setEntityInterceptor
	 * @see HibernateInterceptor#setEntityInterceptor
	 * @see HibernateTemplate#setEntityInterceptor
	 * @see HibernateTransactionManager
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 * @see TransactionSynchronizationManager
	 */
	public static Session getSession(
			SessionFactory sessionFactory, Interceptor entityInterceptor,
			SQLExceptionTranslator jdbcExceptionTranslator) throws DataAccessResourceFailureException {

		try {
			return doGetSession(sessionFactory, entityInterceptor, jdbcExceptionTranslator, true);
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
		}
	}

	/**
	 * Get a Hibernate Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using HibernateTransactionManager. Will create a new Session
	 * otherwise, if allowCreate is true.
	 * <p>Supports synchronization with both Spring-managed JTA transactions
	 * (i.e. JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA
	 * or EJB CMT). See the <code>getSession</code> version with all parameters
	 * for details.
	 * <p>Throws the original HibernateException, in contrast to <code>getSession</code>.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param allowCreate if a non-transactional Session should be created when no
	 * transactional Session can be found for the current thread
	 * @return the Hibernate Session
	 * @throws HibernateException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see #getSession(SessionFactory, Interceptor, SQLExceptionTranslator)
	 * @see #releaseSession
	 * @see HibernateTemplate
	 */
	public static Session doGetSession(SessionFactory sessionFactory, boolean allowCreate)
	    throws HibernateException, IllegalStateException {

		return doGetSession(sessionFactory, null, null, allowCreate);
	}

	/**
	 * Get a Hibernate Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using HibernateTransactionManager. Will create a new Session
	 * otherwise, if allowCreate is true.
	 * <p>Same as <code>getSession</code>, but throwing the original HibernateException.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param entityInterceptor Hibernate entity interceptor, or null if none
	 * @param jdbcExceptionTranslator SQLExcepionTranslator to use for flushing the
	 * Session on transaction synchronization (can be null)
	 * @param allowCreate if a non-transactional Session should be created when no
	 * transactional Session can be found for the current thread
	 * @return the Hibernate Session
	 * @throws HibernateException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 */
	private static Session doGetSession(
			SessionFactory sessionFactory, Interceptor entityInterceptor,
			SQLExceptionTranslator jdbcExceptionTranslator, boolean allowCreate)
			throws HibernateException, IllegalStateException {

		Assert.notNull(sessionFactory, "No SessionFactory specified");

		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (sessionHolder != null && !sessionHolder.isEmpty()) {
			// pre-bound Hibernate Session
			if (TransactionSynchronizationManager.isSynchronizationActive() &&
					sessionHolder.doesNotHoldNonDefaultSession()) {
				// Spring transaction management is active ->
				// register pre-bound Session with it for transactional flushing.
				if (!sessionHolder.isSynchronizedWithTransaction()) {
					logger.debug("Registering Spring transaction synchronization for existing Hibernate Session");
					TransactionSynchronizationManager.registerSynchronization(
							new SpringSessionSynchronization(sessionHolder, sessionFactory, jdbcExceptionTranslator, false));
					sessionHolder.setSynchronizedWithTransaction(true);
					// Switch to FlushMode.AUTO, as we have to assume a thread-bound Session
					// with FlushMode.NEVER, which needs to allow flushing within the transaction.
					FlushMode flushMode = sessionHolder.getSession().getFlushMode();
					if (FlushMode.NEVER.equals(flushMode) &&
							!TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
						sessionHolder.getSession().setFlushMode(FlushMode.AUTO);
						sessionHolder.setPreviousFlushMode(flushMode);
					}
				}
				return sessionHolder.getSession();
			}
			else {
				// No Spring transaction management active -> try JTA transaction synchronization.
				Session session = getJtaSynchronizedSession(sessionHolder, sessionFactory, jdbcExceptionTranslator);
				if (session != null) {
					return session;
				}
			}
		}

		logger.debug("Opening Hibernate Session");
		Session session = (entityInterceptor != null ?
				sessionFactory.openSession(entityInterceptor) : sessionFactory.openSession());

		// Use same Session for further Hibernate actions within the transaction.
		// Thread object will get removed by synchronization at transaction completion.
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			// We're within a Spring-managed transaction, possibly from JtaTransactionManager.
			logger.debug("Registering Spring transaction synchronization for new Hibernate Session");
			sessionHolder = new SessionHolder(session);
			if (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
				session.setFlushMode(FlushMode.NEVER);
			}
			TransactionSynchronizationManager.registerSynchronization(
					new SpringSessionSynchronization(sessionHolder, sessionFactory, jdbcExceptionTranslator, true));
			sessionHolder.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
		}
		else {
			// No Spring transaction management active -> try JTA transaction synchronization.
			registerJtaSynchronization(session, sessionFactory, jdbcExceptionTranslator, sessionHolder);
		}

		// Check whether we are allowed to return the Session.
		if (!allowCreate && !isSessionTransactional(session, sessionFactory)) {
			doClose(session);
			throw new IllegalStateException("No Hibernate Session bound to thread, " +
			    "and configuration does not allow creation of non-transactional one here");
		}

		return session;
	}

	/**
	 * Retrieve a Session from the given SessionHolder, potentially from a
	 * JTA transaction synchronization.
	 * @param sessionHolder the SessionHolder to check
	 * @param sessionFactory the SessionFactory to get the JTA TransactionManager from
	 * @param jdbcExceptionTranslator SQLExcepionTranslator to use for flushing the
	 * Session on transaction synchronization (can be null)
	 * @return the associated Session, if any
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 */
	private static Session getJtaSynchronizedSession(
	    SessionHolder sessionHolder, SessionFactory sessionFactory,
	    SQLExceptionTranslator jdbcExceptionTranslator) throws DataAccessResourceFailureException {

		// JTA synchronization is only possible with a javax.transaction.TransactionManager.
		// We'll check the Hibernate SessionFactory: If a TransactionManagerLookup is specified
		// in Hibernate configuration, it will contain a TransactionManager reference.
		TransactionManager jtaTm = getJtaTransactionManager(sessionFactory, sessionHolder.getAnySession());
		if (jtaTm != null) {
			// Check whether JTA transaction management is active ->
			// fetch pre-bound Session for the current JTA transaction, if any.
			// (just necessary for JTA transaction suspension, with an individual
			// Hibernate Session per currently active/suspended transaction)
			try {
				int jtaStatus = jtaTm.getStatus();
				if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
					// look for transaction-specific Session
					Transaction jtaTx = jtaTm.getTransaction();
					Session session = sessionHolder.getSession(jtaTx);
					if (session == null && !sessionHolder.isSynchronizedWithTransaction()) {
						// No transaction-specific Session found: If not already marked as
						// synchronized with transaction, register the default thread-bound
						// Session as JTA-transactional. If there is no default Session,
						// we're a new inner JTA transaction with an outer one being suspended:
						// In that case, we'll return null to trigger opening of a new Session.
						session = sessionHolder.getSession();
						if (session != null) {
							logger.debug("Registering JTA transaction synchronization for existing Hibernate Session");
							sessionHolder.addSession(jtaTx, session);
							jtaTx.registerSynchronization(
									new JtaSessionSynchronization(
											new SpringSessionSynchronization(
													sessionHolder, sessionFactory, jdbcExceptionTranslator, false),
											jtaTm));
							sessionHolder.setSynchronizedWithTransaction(true);
							// Switch to FlushMode.AUTO, as we have to assume a thread-bound Session
							// with FlushMode.NEVER, which needs to allow flushing within the transaction.
							FlushMode flushMode = sessionHolder.getSession().getFlushMode();
							if (FlushMode.NEVER.equals(flushMode)) {
								sessionHolder.getSession().setFlushMode(FlushMode.AUTO);
								sessionHolder.setPreviousFlushMode(flushMode);
							}
						}
					}
					return session;
				}
				else {
					// No transaction active -> simply return default thread-bound Session, if any
					// (possibly from OpenSessionInViewFilter/Interceptor).
					return sessionHolder.getSession();
				}
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not check JTA transaction", ex);
			}
		}
		else {
			// No JTA TransactionManager -> simply return default thread-bound Session, if any
			// (possibly from OpenSessionInViewFilter/Interceptor).
			return sessionHolder.getSession();
		}
	}

	/**
	 * Register a JTA synchronization for the given Session, if any.
	 * @param sessionHolder the existing thread-bound SessionHolder, if any
	 * @param session the Session to register
	 * @param sessionFactory the SessionFactory that the Session was created with
	 * @param jdbcExceptionTranslator SQLExcepionTranslator to use for flushing the
	 * Session on transaction synchronization (can be null)
	 */
	private static void registerJtaSynchronization(Session session, SessionFactory sessionFactory,
			SQLExceptionTranslator jdbcExceptionTranslator, SessionHolder sessionHolder) {

		// JTA synchronization is only possible with a javax.transaction.TransactionManager.
		// We'll check the Hibernate SessionFactory: If a TransactionManagerLookup is specified
		// in Hibernate configuration, it will contain a TransactionManager reference.
		TransactionManager jtaTm = getJtaTransactionManager(sessionFactory, session);
		if (jtaTm != null) {
			try {
				int jtaStatus = jtaTm.getStatus();
				if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
					logger.debug("Registering JTA transaction synchronization for new Hibernate Session");
					Transaction jtaTx = jtaTm.getTransaction();
					SessionHolder holderToUse = sessionHolder;
					// Register JTA Transaction with existing SessionHolder.
					// Create a new SessionHolder if none existed before.
					if (holderToUse == null) {
						holderToUse = new SessionHolder(jtaTx, session);
					}
					else {
						holderToUse.addSession(jtaTx, session);
					}
					jtaTx.registerSynchronization(
							new JtaSessionSynchronization(
									new SpringSessionSynchronization(
											holderToUse, sessionFactory, jdbcExceptionTranslator, true),
									jtaTm));
					holderToUse.setSynchronizedWithTransaction(true);
					if (holderToUse != sessionHolder) {
						TransactionSynchronizationManager.bindResource(sessionFactory, holderToUse);
					}
				}
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException(
						"Could not register synchronization with JTA TransactionManager", ex);
			}
		}
	}


	/**
	 * Get a new Hibernate Session from the given SessionFactory.
	 * Will return a new Session even if there already is a pre-bound
	 * Session for the given SessionFactory.
	 * <p>Within a transaction, this method will create a new Session
	 * that shares the transaction's JDBC Connection. More specifically,
	 * it will use the same JDBC Connection as the pre-bound Hibernate Session.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @return the new Session
	 */
	public static Session getNewSession(SessionFactory sessionFactory) {
		return getNewSession(sessionFactory, null);
	}

	/**
	 * Get a new Hibernate Session from the given SessionFactory.
	 * Will return a new Session even if there already is a pre-bound
	 * Session for the given SessionFactory.
	 * <p>Within a transaction, this method will create a new Session
	 * that shares the transaction's JDBC Connection. More specifically,
	 * it will use the same JDBC Connection as the pre-bound Hibernate Session.
	 * @param sessionFactory Hibernate SessionFactory to create the session with
	 * @param entityInterceptor Hibernate entity interceptor, or null if none
	 * @return the new Session
	 */
	public static Session getNewSession(SessionFactory sessionFactory, Interceptor entityInterceptor) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");

		try {
			SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
			if (sessionHolder != null && !sessionHolder.isEmpty()) {
				if (entityInterceptor != null) {
					return sessionFactory.openSession(sessionHolder.getAnySession().connection(), entityInterceptor);
				}
				else {
					return sessionFactory.openSession(sessionHolder.getAnySession().connection());
				}
			}
			else {
				if (entityInterceptor != null) {
					return sessionFactory.openSession(entityInterceptor);
				}
				else {
					return sessionFactory.openSession();
				}
			}
		}
		catch (HibernateException ex) {
			throw new DataAccessResourceFailureException("Could not open Hibernate Session", ex);
		}
	}


	/**
	 * Return whether the given Hibernate Session is transactional, that is,
	 * bound to the current thread by Spring's transaction facilities.
	 * @param session the Hibernate Session to check
	 * @param sessionFactory Hibernate SessionFactory that the Session was created with
	 * (can be null)
	 * @return whether the Session is transactional
	 */
	public static boolean isSessionTransactional(Session session, SessionFactory sessionFactory) {
		if (sessionFactory == null) {
			return false;
		}
		SessionHolder sessionHolder =
				(SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		return (sessionHolder != null && sessionHolder.containsSession(session));
	}

	/**
	 * Apply the current transaction timeout, if any, to the given
	 * Hibernate Query object.
	 * @param query the Hibernate Query object
	 * @param sessionFactory Hibernate SessionFactory that the Query was created for
	 * (can be null)
	 * @see org.hibernate.Query#setTimeout
	 */
	public static void applyTransactionTimeout(Query query, SessionFactory sessionFactory) {
		Assert.notNull(query, "No Query object specified");
		if (sessionFactory != null) {
			SessionHolder sessionHolder =
					(SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
			if (sessionHolder != null && sessionHolder.hasTimeout()) {
				query.setTimeout(sessionHolder.getTimeToLiveInSeconds());
			}
		}
	}

	/**
	 * Apply the current transaction timeout, if any, to the given
	 * Hibernate Criteria object.
	 * @param criteria the Hibernate Criteria object
	 * @param sessionFactory Hibernate SessionFactory that the Criteria was created for
	 * @see org.hibernate.Criteria#setTimeout
	 */
	public static void applyTransactionTimeout(Criteria criteria, SessionFactory sessionFactory) {
		Assert.notNull(criteria, "No Criteria object specified");
		SessionHolder sessionHolder =
		    (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (sessionHolder != null && sessionHolder.hasTimeout()) {
			criteria.setTimeout(sessionHolder.getTimeToLiveInSeconds());
		}
	}

	/**
	 * Convert the given HibernateException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy. Note that it is advisable to
	 * handle JDBCException specifically by using a SQLExceptionTranslator for the
	 * underlying SQLException.
	 * @param ex HibernateException that occured
	 * @return the corresponding DataAccessException instance
	 * @see HibernateAccessor#convertHibernateAccessException
	 * @see HibernateAccessor#convertJdbcAccessException
	 * @see HibernateTransactionManager#convertHibernateAccessException
	 * @see HibernateTransactionManager#convertJdbcAccessException
	 * @see org.hibernate.JDBCException#getSQLException
	 * @see SQLExceptionTranslator
	 */
	public static DataAccessException convertHibernateAccessException(HibernateException ex) {
		if (ex instanceof JDBCException) {
			// SQLException during Hibernate access: only passed in here from custom code,
			// as HibernateTemplate etc will use SQLExceptionTranslator-based handling
			return new HibernateJdbcException((JDBCException) ex);
		}
		if (ex instanceof UnresolvableObjectException) {
			return new HibernateObjectRetrievalFailureException((UnresolvableObjectException) ex);
		}
		if (ex instanceof ObjectNotFoundException) {
			return new HibernateObjectRetrievalFailureException((ObjectNotFoundException) ex);
		}
		if (ex instanceof ObjectDeletedException) {
			return new HibernateObjectRetrievalFailureException((ObjectDeletedException) ex);
		}
		if (ex instanceof WrongClassException) {
			return new HibernateObjectRetrievalFailureException((WrongClassException) ex);
		}
		if (ex instanceof StaleObjectStateException) {
			return new HibernateOptimisticLockingFailureException((StaleObjectStateException) ex);
		}
		if (ex instanceof QueryException) {
			return new HibernateQueryException((QueryException) ex);
		}
		if (ex instanceof PersistentObjectException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage());
		}
		if (ex instanceof TransientObjectException) {
			return new InvalidDataAccessApiUsageException(ex.getMessage());
		}
		// fallback
		return new HibernateSystemException(ex);
	}


	/**
	 * Return if deferred close is active for the current thread
	 * and the given SessionFactory.
	 * @param sessionFactory the Hibernate SessionFactory to check
	 */
	public static boolean isDeferredCloseActive(SessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");
		Map holderMap = (Map) deferredCloseHolder.get();
		return (holderMap != null && holderMap.containsKey(sessionFactory));
	}

	/**
	 * Initialize deferred close for the current thread and the given SessionFactory.
	 * Sessions will not be actually closed on close calls then, but rather at a
	 * processDeferredClose call at a finishing point (like request completion).
	 * <p>Used by OpenSessionInViewFilter and OpenSessionInViewInterceptor
	 * when not configured for a single session.
	 * @param sessionFactory Hibernate SessionFactory
	 * @see #processDeferredClose
	 * @see #releaseSession
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter#setSingleSession
	 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor#setSingleSession
	 */
	public static void initDeferredClose(SessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");
		logger.debug("Initializing deferred close of Hibernate Sessions");
		Map holderMap = (Map) deferredCloseHolder.get();
		if (holderMap == null) {
			holderMap = new HashMap();
			deferredCloseHolder.set(holderMap);
		}
		holderMap.put(sessionFactory, new HashSet());
	}

	/**
	 * Process Sessions that have been registered for deferred close
	 * for the given SessionFactory.
	 * @param sessionFactory Hibernate SessionFactory
	 * @see #initDeferredClose
	 * @see #releaseSession
	 */
	public static void processDeferredClose(SessionFactory sessionFactory) {
		Assert.notNull(sessionFactory, "No SessionFactory specified");

		Map holderMap = (Map) deferredCloseHolder.get();
		if (holderMap == null || !holderMap.containsKey(sessionFactory)) {
			throw new IllegalStateException("Deferred close not active for SessionFactory [" + sessionFactory + "]");
		}

		logger.debug("Processing deferred close of Hibernate Sessions");
		Set sessions = (Set) holderMap.remove(sessionFactory);
		for (Iterator it = sessions.iterator(); it.hasNext();) {
			doClose((Session) it.next());
		}

		if (holderMap.isEmpty()) {
			deferredCloseHolder.set(null);
		}
	}

	/**
	 * Close the given Session, created via the given factory,
	 * if it isn't bound to the thread.
	 * @deprecated in favor of releaseSession
	 * @see #releaseSession
	 */
	public static void closeSessionIfNecessary(Session session, SessionFactory sessionFactory) {
		releaseSession(session, sessionFactory);
	}

	/**
	 * Close the given Session, created via the given factory,
	 * if it is not managed externally (i.e. not bound to the thread).
	 * @param session the Hibernate Session to close
	 * @param sessionFactory Hibernate SessionFactory that the Session was created with
	 * (can be null)
	 */
	public static void releaseSession(Session session, SessionFactory sessionFactory) {
		if (session == null) {
			return;
		}

		// Only close non-transactional Sessions.
		if (!isSessionTransactional(session, sessionFactory)) {
			closeSessionOrRegisterDeferredClose(session, sessionFactory);
		}
	}

	/**
	 * Close the given Session or register it for deferred close.
	 * @param session the Hibernate Session to close
	 * @param sessionFactory Hibernate SessionFactory that the Session was created with
	 * (can be null)
	 * @see #initDeferredClose
	 * @see #processDeferredClose
	 */
	private static void closeSessionOrRegisterDeferredClose(Session session, SessionFactory sessionFactory) {
		Map holderMap = (Map) deferredCloseHolder.get();
		if (holderMap != null && sessionFactory != null && holderMap.containsKey(sessionFactory)) {
			logger.debug("Registering Hibernate Session for deferred close");
			Set sessions = (Set) holderMap.get(sessionFactory);
			sessions.add(session);
		}
		else {
			doClose(session);
		}
	}

	/**
	 * Perform the actual closing of the Hibernate Session.
	 * @param session Session to close
	 */
	private static void doClose(Session session) {
		logger.debug("Closing Hibernate Session");
		try {
			session.close();
		}
		catch (JDBCException ex) {
			// SQLException underneath
			logger.error("Could not close Hibernate Session", ex.getSQLException());
		}
		catch (HibernateException ex) {
			logger.error("Could not close Hibernate Session", ex);
		}
		catch (RuntimeException ex) {
			logger.error("Unexpected exception on closing Hibernate Session", ex);
		}
	}


	/**
	 * Callback for resource cleanup at the end of a Spring-managed JTA transaction,
	 * i.e. when participating in a JtaTransactionManager transaction.
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class SpringSessionSynchronization implements TransactionSynchronization, Ordered {

		private final SessionHolder sessionHolder;

		private final SessionFactory sessionFactory;

		private final SQLExceptionTranslator jdbcExceptionTranslator;

		private final boolean newSession;

		/**
		 * Whether Hibernate has a looked-up JTA TransactionManager that it will
		 * automatically register CacheSynchronizations with on Session connect.
		 */
		private boolean hibernateTransactionCompletion = false;

		private Transaction jtaTransaction;

		public SpringSessionSynchronization(
				SessionHolder sessionHolder, SessionFactory sessionFactory,
				SQLExceptionTranslator jdbcExceptionTranslator, boolean newSession) {

			this.sessionHolder = sessionHolder;
			this.sessionFactory = sessionFactory;
			this.jdbcExceptionTranslator = jdbcExceptionTranslator;
			this.newSession = newSession;

			// check whether the SessionFactory has a JTA TransactionManager
			TransactionManager jtaTm = getJtaTransactionManager(sessionFactory, sessionHolder.getAnySession());
			if (jtaTm != null) {
				this.hibernateTransactionCompletion = true;
				// fetch current JTA Transaction object
				// (just necessary for JTA transaction suspension, with an individual
				// Hibernate Session per currently active/suspended transaction)
				try {
					int jtaStatus = jtaTm.getStatus();
					if (jtaStatus == Status.STATUS_ACTIVE || jtaStatus == Status.STATUS_MARKED_ROLLBACK) {
						this.jtaTransaction = jtaTm.getTransaction();
					}
				}
				catch (SystemException ex) {
					throw new DataAccessResourceFailureException("Could not check JTA transaction", ex);
				}
			}
		}

		public int getOrder() {
			return SESSION_SYNCHRONIZATION_ORDER;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sessionHolder);
		}

		public void beforeCommit(boolean readOnly) throws DataAccessException {
			if (!readOnly) {
				Session session = null;
				// Check whether there is a Hibernate Session for the current JTA
				// transaction. Else, fall back to the default thread-bound Session.
				if (this.jtaTransaction != null) {
					session = this.sessionHolder.getSession(this.jtaTransaction);
				}
				if (session == null) {
					session = this.sessionHolder.getSession();
				}
				// Read-write transaction -> flush the Hibernate Session.
				// Further check: only flush when not FlushMode.NEVER.
				if (!FlushMode.NEVER.equals(session.getFlushMode())) {
					logger.debug("Flushing Hibernate Session on transaction synchronization");
					try {
						session.flush();
					}
					catch (JDBCException ex) {
						if (this.jdbcExceptionTranslator != null) {
							throw this.jdbcExceptionTranslator.translate("SessionSynchronization", null, ex.getSQLException());
						}
						else {
							throw new HibernateJdbcException(ex);
						}
					}
					catch (HibernateException ex) {
						throw convertHibernateAccessException(ex);
					}
				}
			}
		}

		public void beforeCompletion() {
			if (this.jtaTransaction != null) {
				// Typically in case of a suspended JTA transaction:
				// Remove the Session for the current JTA transaction, but keep the holder.
				Session session = this.sessionHolder.removeSession(this.jtaTransaction);
				if (session != null) {
					if (this.sessionHolder.isEmpty()) {
						// No Sessions for JTA transactions bound anymore -> could remove it.
						if (TransactionSynchronizationManager.hasResource(this.sessionFactory)) {
							// Explicit check necessary because of remote transaction propagation:
							// The synchronization callbacks will execute in a different thread
							// in such a scenario, as they're triggered by a remote server.
							// The best we can do is to leave the SessionHolder bound to the
							// thread that originally performed the data access. It will be
							// reused when a new data access operation starts on that thread.
							TransactionSynchronizationManager.unbindResource(this.sessionFactory);
						}
					}
					// Do not close a pre-bound Session. In that case, we'll find the
					// transaction-specific Session the same as the default Session.
					if (session != this.sessionHolder.getSession()) {
						closeSessionOrRegisterDeferredClose(session, this.sessionFactory);
					}
					else if (this.sessionHolder.getPreviousFlushMode() != null) {
						// In case of pre-bound Session, restore previous flush mode.
						session.setFlushMode(this.sessionHolder.getPreviousFlushMode());
					}
					return;
				}
			}
			// We'll only get here if there was no specific JTA transaction to handle.
			if (this.newSession) {
				// Default behavior: unbind and close the thread-bound Hibernate Session.
				TransactionSynchronizationManager.unbindResource(this.sessionFactory);
				if (this.hibernateTransactionCompletion) {
					// Close the Hibernate Session here in case of a Hibernate TransactionManagerLookup:
					// Hibernate will automatically defer the actual closing to JTA transaction completion.
					// Else, the Session will be closed in the afterCompletion method, to provide the
					// correct transaction status for releasing the Session's cache locks.
					closeSessionOrRegisterDeferredClose(this.sessionHolder.getSession(), this.sessionFactory);
				}
			}
			else if (this.sessionHolder.getPreviousFlushMode() != null) {
				// In case of pre-bound Session, restore previous flush mode.
				this.sessionHolder.getSession().setFlushMode(this.sessionHolder.getPreviousFlushMode());
			}
		}

		public void afterCompletion(int status) {
			if (!this.hibernateTransactionCompletion || !this.newSession) {
				// No Hibernate TransactionManagerLookup: apply afterTransactionCompletion callback.
				// Always perform explicit afterTransactionCompletion callback for pre-bound Session,
				// even with Hibernate TransactionManagerLookup (which only applies to new Sessions).
				Session session = this.sessionHolder.getSession();
				// Provide correct transaction status for releasing the Session's cache locks,
				// if possible. Else, closing will release all cache locks assuming a rollback.
				if (session instanceof SessionImplementor) {
					((SessionImplementor) session).afterTransactionCompletion(status == STATUS_COMMITTED, null);
				}
				// Close the Hibernate Session here if necessary
				// (closed in beforeCompletion in case of TransactionManagerLookup).
				if (this.newSession) {
					closeSessionOrRegisterDeferredClose(session, this.sessionFactory);
				}
			}
			if (!this.newSession && status != STATUS_COMMITTED) {
				// Clear all pending inserts/updates/deletes in the Session.
				// Necessary for pre-bound Sessions, to avoid inconsistent state.
				this.sessionHolder.getSession().clear();
			}
			if (this.sessionHolder.doesNotHoldNonDefaultSession()) {
				this.sessionHolder.setSynchronizedWithTransaction(false);
			}
		}
	}


	/**
	 * Callback for resource cleanup at the end of a non-Spring JTA transaction,
	 * i.e. when plain JTA or EJB CMT is used without Spring's JtaTransactionManager.
	 */
	private static class JtaSessionSynchronization implements Synchronization {

		private final SpringSessionSynchronization springSessionSynchronization;

		private final TransactionManager jtaTransactionManager;

		private boolean beforeCompletionCalled = false;

		public JtaSessionSynchronization(
				SpringSessionSynchronization springSessionSynchronization, TransactionManager jtaTransactionManager) {
			this.springSessionSynchronization = springSessionSynchronization;
			this.jtaTransactionManager = jtaTransactionManager;
		}

		/**
		 * JTA beforeCompletion callback: just invoked on commit.
		 * <p>In case of an exception, the JTA transaction gets set to rollback-only.
		 * (Synchronization.beforeCompletion is not supposed to throw an exception.)
		 * @see SpringSessionSynchronization#beforeCommit
		 */
		public void beforeCompletion() {
			try {
				boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
				this.springSessionSynchronization.beforeCommit(readOnly);
			}
			catch (Throwable ex) {
				logger.error("beforeCommit callback threw exception", ex);
				try {
					this.jtaTransactionManager.setRollbackOnly();
				}
				catch (SystemException ex2) {
					logger.error("Could not set JTA transaction rollback-only", ex2);
				}
			}
			// Unbind the SessionHolder from the thread early, to avoid issues
			// with strict JTA implementations that issue warnings when doing JDBC
			// operations after transaction completion (e.g. Connection.getWarnings).
			this.beforeCompletionCalled = true;
			this.springSessionSynchronization.beforeCompletion();
		}

		/**
		 * JTA afterCompletion callback: invoked after commit/rollback.
		 * <p>Needs to invoke SpringSessionSynchronization's beforeCompletion
		 * at this late stage, as there's no corresponding callback with JTA.
		 * @see SpringSessionSynchronization#beforeCompletion
		 * @see SpringSessionSynchronization#afterCompletion
		 */
		public void afterCompletion(int status) {
			if (!this.beforeCompletionCalled) {
				// beforeCompletion not called before (probably because of JTA rollback).
				// Unbind the SessionHolder from the thread here.
				this.springSessionSynchronization.beforeCompletion();
			}
			// Reset the synchronizedWithTransaction flag,
			// and clear the Hibernate Session after a rollback (if necessary).
			switch (status) {
				case Status.STATUS_COMMITTED:
					this.springSessionSynchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
					break;
				case Status.STATUS_ROLLEDBACK:
					this.springSessionSynchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
					break;
				default:
					this.springSessionSynchronization.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
			}
		}
	}

}
