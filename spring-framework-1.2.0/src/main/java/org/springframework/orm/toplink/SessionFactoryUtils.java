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

package org.springframework.orm.toplink;

import oracle.toplink.exceptions.ConcurrencyException;
import oracle.toplink.exceptions.ConversionException;
import oracle.toplink.exceptions.DatabaseException;
import oracle.toplink.exceptions.OptimisticLockException;
import oracle.toplink.exceptions.QueryException;
import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.sessions.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * Helper class featuring methods for TopLink Session handling,
 * allowing for reuse of TopLink Session instances within transactions.
 *
 * <p>Supports synchronization with both Spring-managed JTA transactions
 * (i.e. JtaTransactionManager) and non-Spring JTA transactions (i.e. plain JTA
 * or EJB CMT). See the <code>getSession</code> version with all parameters
 * for details.
 *
 * <p>Used internally by TopLinkTemplate and TopLinkInterceptor. Can also be used
 * directly in application code, e.g. in combination with TopLinkInterceptor.
 *
 * @author Juergen Hoeller
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 1.2
 * @see TopLinkTemplate
 * @see TopLinkInterceptor
 * @see TopLinkTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public abstract class SessionFactoryUtils {

	private static final Log logger = LogFactory.getLog(SessionFactoryUtils.class);

	/**
	 * Get a TopLink Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using TopLinkTransactionManager. Will create a new Session
	 * otherwise, if allowCreate is true.
	 * <p>This is the <code>getSession</code> method used by typical data access code,
	 * in combination with <code>releaseSession</code> called when done with
	 * the Session. Note that TopLinkTemplate allows to write data access code
	 * without caring about such resource handling.
	 * @param sessionFactory TopLink SessionFactory to create the session with
	 * @param allowCreate if a non-transactional Session should be created when no
	 * transactional Session can be found for the current thread
	 * @return the TopLink Session
	 * @throws DataAccessResourceFailureException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see #releaseSession
	 * @see TopLinkTemplate
	 */
	public static Session getSession(SessionFactory sessionFactory, boolean allowCreate)
			throws DataAccessResourceFailureException, IllegalStateException {

		try {
			return doGetSession(sessionFactory, allowCreate);
		}
		catch (TopLinkException ex) {
			throw new DataAccessResourceFailureException("Could not open TopLink Session", ex);
		}
	}

	/**
	 * Get a TopLink Session for the given SessionFactory. Is aware of and will
	 * return any existing corresponding Session bound to the current thread, for
	 * example when using TopLinkTransactionManager. Will create a new Session
	 * otherwise, if allowCreate is true.
	 * <p>Same as <code>getSession</code>, but throwing the original TopLinkException.
	 * @param sessionFactory TopLink SessionFactory to create the session with
	 * @param allowCreate if a non-transactional Session should be created when no
	 * transactional Session can be found for the current thread
	 * @return the TopLink Session
	 * @throws TopLinkException if the Session couldn't be created
	 * @throws IllegalStateException if no thread-bound Session found and allowCreate false
	 * @see #releaseSession
	 * @see TopLinkTemplate
	 */
	public static Session doGetSession(SessionFactory sessionFactory, boolean allowCreate)
			throws TopLinkException, IllegalStateException {

		Assert.notNull(sessionFactory, "No SessionFactory specified");

		SessionHolder sessionHolder =
				(SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		if (sessionHolder != null) {
			return sessionHolder.getSession();
		}

		if (!allowCreate && !TransactionSynchronizationManager.isSynchronizationActive()) {
			throw new IllegalStateException("No TopLink Session bound to thread, " +
					"and configuration does not allow creation of non-transactional one here");
		}

		logger.debug("Creating TopLink Session");
		Session session = sessionFactory.createSession();

		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			logger.debug("Registering new Spring transaction synchronization for new TopLink Session");
			// Use same Session for further TopLink actions within the transaction.
			// Thread object will get removed by synchronization at transaction completion.
			sessionHolder = new SessionHolder(session);
			sessionHolder.setSynchronizedWithTransaction(true);
			TransactionSynchronizationManager.registerSynchronization(
					new SessionSynchronization(sessionHolder, sessionFactory));
			TransactionSynchronizationManager.bindResource(sessionFactory, sessionHolder);
		}

		return session;
	}

	/**
	 * Convert the given TopLinkException to an appropriate exception from the
	 * <code>org.springframework.dao</code> hierarchy.
	 * @param ex TopLinkException that occured
	 * @return the corresponding DataAccessException instance
	 */
	public static DataAccessException convertTopLinkAccessException(TopLinkException ex) {
		if (ex instanceof DatabaseException) {
			// SQLException during TopLink access: only passed in here from custom code,
			// as TopLinkTemplate will use SQLExceptionTranslator-based handling.
			return new TopLinkJdbcException((DatabaseException) ex);
		}
		if (ex instanceof ConcurrencyException) {
			return new ConcurrencyFailureException(ex.getMessage(), ex);
		}
		if (ex instanceof ConversionException) {
			return new TypeMismatchDataAccessException(ex.getMessage(), ex);
		}
		if (ex instanceof QueryException) {
			return new TopLinkQueryException((QueryException) ex);
		}
		if (ex instanceof OptimisticLockException) {
			return new TopLinkOptimisticLockingFailureException((OptimisticLockException) ex);
		}
		// fallback
		return new TopLinkSystemException(ex);
	}

	/**
	 * Close the given Session, created via the given factory,
	 * if it is not managed externally (i.e. not bound to the thread).
	 * @param session the TopLink Session to close
	 * @param sessionFactory TopLink SessionFactory that the Session was created with
	 * (can be null)
	 */
	public static void releaseSession(Session session, SessionFactory sessionFactory) {
		if (session == null) {
			return;
		}

		if (sessionFactory != null) {
			SessionHolder sessionHolder =
					(SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
			if (sessionHolder != null && session == sessionHolder.getSession()) {
				// It's the transactional Session: Don't close it.
				return;
			}
		}

		logger.debug("Closing TopLink Session");
		try {
			session.release();
		}
		catch (TopLinkException ex) {
			logger.error("Could not close TopLink Session", ex);
		}
		catch (RuntimeException ex) {
			logger.error("Unexpected exception on closing TopLink Session", ex);
		}
	}


	/**
	 * Callback for resource cleanup at the end of a Spring-managed JTA transaction,
	 * i.e. when participating in a JtaTransactionManager transaction.
	 * @see org.springframework.transaction.jta.JtaTransactionManager
	 */
	private static class SessionSynchronization extends TransactionSynchronizationAdapter {

		private final SessionHolder sessionHolder;

		private final SessionFactory sessionFactory;

		private SessionSynchronization(SessionHolder sessionHolder, SessionFactory sessionFactory) {
			this.sessionHolder = sessionHolder;
			this.sessionFactory = sessionFactory;
		}

		public void suspend() {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}

		public void resume() {
			TransactionSynchronizationManager.bindResource(this.sessionFactory, this.sessionHolder);
		}

		public void beforeCompletion() {
			TransactionSynchronizationManager.unbindResource(this.sessionFactory);
		}

		public void afterCompletion(int status) {
			releaseSession(this.sessionHolder.getSession(), this.sessionFactory);
		}
	}

}
