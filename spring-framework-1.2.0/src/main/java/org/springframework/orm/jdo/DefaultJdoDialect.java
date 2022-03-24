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

import java.sql.Connection;
import java.sql.SQLException;

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;

/**
 * Default implementation of the JdoDialect interface.
 * Updated to leverage the JDO 2.0 API, as of Spring 1.2.
 * Used by JdoAccessor and JdoTransactionManager as default.
 *
 * <p>Simply begins a standard JDO transaction in <code>beginTransaction</code>.
 * Returns a handle for a JDO2 DataStoreConnection on <code>getJdbcConnection</code>.
 * Ignores a given query timeout in <code>applyQueryTimeout</code>.
 * Calls the JDO2 flush operation on <code>flush</code>.
 * Delegates to PersistenceManagerFactoryUtils for exception translation.
 *
 * <p>Note that, even with JDO2, vendor-specific subclasses are still necessary
 * for special transaction semantics and more sophisticated exception translation.
 * Furthermore, vendor-specific subclasses are encouraged to expose the native
 * JDBC Connection on <code>getJdbcConnection</code>, rather than JDO2's wrapper
 * handle.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see JdoAccessor#setJdoDialect
 * @see JdoTransactionManager#setJdoDialect
 */
public class DefaultJdoDialect implements JdoDialect {

	protected Log logger = LogFactory.getLog(getClass());

	private PersistenceManagerFactory persistenceManagerFactory;

	private SQLExceptionTranslator jdbcExceptionTranslator;


	/**
	 * Create a new DefaultJdoDialect.
	 */
	public DefaultJdoDialect() {
	}

	/**
	 * Create a new DefaultJdoDialect.
	 * @param pmf the JDO PersistenceManagerFactory, which is used
	 * to initialize the default JDBC exception translator
	 */
	public DefaultJdoDialect(PersistenceManagerFactory pmf) {
		setPersistenceManagerFactory(pmf);
	}

	/**
	 * Set the JDO PersistenceManagerFactory, which is used to initialize
	 * the default JDBC exception translator if none specified.
	 * @see #setJdbcExceptionTranslator
	 */
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
	}

	/**
	 * Return the JDO PersistenceManagerFactory that should be used to create
	 * PersistenceManagers.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory() {
		return persistenceManagerFactory;
	}

	/**
	 * Set the JDBC exception translator for this dialect.
	 * Applied to SQLExceptions that are the cause of JDOExceptions.
	 * <p>The default exception translator is either a SQLErrorCodeSQLExceptionTranslator
	 * if a DataSource is available, or a SQLStateSQLExceptionTranslator else.
	 * @param jdbcExceptionTranslator exception translator
	 * @see SQLException
	 * @see JDOException#getCause
	 * @see PersistenceManagerFactoryUtils#newJdbcExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
	 * @see org.springframework.jdbc.support.SQLStateSQLExceptionTranslator
	 */
	public void setJdbcExceptionTranslator(SQLExceptionTranslator jdbcExceptionTranslator) {
		this.jdbcExceptionTranslator = jdbcExceptionTranslator;
	}

	/**
	 * Return the JDBC exception translator for this instance.
	 * <p>Creates a default SQLErrorCodeSQLExceptionTranslator or SQLStateSQLExceptionTranslator
	 * for the specified PersistenceManagerFactory, if no exception translator explicitly specified.
	 */
	public SQLExceptionTranslator getJdbcExceptionTranslator() {
		if (this.jdbcExceptionTranslator == null) {
			this.jdbcExceptionTranslator =
					PersistenceManagerFactoryUtils.newJdbcExceptionTranslator(this.persistenceManagerFactory);
		}
		return this.jdbcExceptionTranslator;
	}


	/**
	 * This implementation invokes the standard JDO <code>Transaction.begin</code>
	 * method. Throws an InvalidIsolationLevelException if a non-default isolation
	 * level is set.
	 * @see Transaction#begin
	 * @see InvalidIsolationLevelException
	 */
	public Object beginTransaction(Transaction transaction, TransactionDefinition definition)
			throws JDOException, SQLException, TransactionException {

		if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
			throw new InvalidIsolationLevelException(
					"Standard JDO does not support custom isolation levels - " +
					"use a special JdoDialect for your JDO implementation");
		}
		transaction.begin();
		return null;
	}

	/**
	 * This implementation does nothing, as the default beginTransaction implementation
	 * does not require any cleanup.
	 * @see #beginTransaction
	 */
	public void cleanupTransaction(Object transactionData) {
	}

	/**
	 * This implementation returns a DataStoreConnectionHandle for JDO2,
	 * which will also work on JDO1 until actually accessing the JDBC Connection.
	 * <p>For pre-JDO2 implementations, override this method to return the
	 * Connection through the corresponding vendor-specific mechanism, or null
	 * if the Connection is not retrievable.
	 * <p><b>NOTE:</b> A JDO2 DataStoreConnection is always a wrapper,
	 * never the native JDBC Connection. If you need access to the native JDBC
	 * Connection (or the connection pool handle, to be unwrapped via a Spring
	 * NativeJdbcExtractor), override this method to return the native
	 * Connection through the corresponding vendor-specific mechanism.
	 * <p>A JDO2 DataStoreConnection is only "borrowed" from the PersistenceManager:
	 * it needs to be returned as early as possible. Effectively, JDO2 requires the
	 * fetched Connection to be closed before continuing PersistenceManager work.
	 * For this reason, the exposed ConnectionHandle eagerly releases its JDBC
	 * Connection at the end of each JDBC data access operation (that is, on
	 * <code>DataSourceUtils.releaseConnection</code>).
	 * @see PersistenceManager#getDataStoreConnection()
	 * @see org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor
	 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
	 */
	public ConnectionHandle getJdbcConnection(final PersistenceManager pm, boolean readOnly)
			throws JDOException, SQLException {

		return new DataStoreConnectionHandle(pm);
	}

	/**
	 * This implementation does nothing, assuming that the Connection
	 * will implicitly be closed with the PersistenceManager.
	 * <p>If the JDO implementation returns a Connection handle that
	 * it expects the application to close, the dialect needs to invoke
	 * <code>Connection.close</code> here.
	 * @see Connection#close
	 */
	public void releaseJdbcConnection(ConnectionHandle conHandle, PersistenceManager pm)
			throws JDOException, SQLException {
	}

	/**
	 * This implementation logs a warning that it cannot apply a query timeout.
	 */
	public void applyQueryTimeout(Query query, int remainingTimeInSeconds) throws JDOException {
		logger.info("DefaultJdoDialect does not support query timeouts: ignoring remaining transaction time");
	}

	/**
	 * This implementation delegates to JDO 2.0's <code>flush</code> method.
	 * <p>To be overridden for pre-JDO2 implementations, using the corresponding
	 * vendor-specific mechanism there.
	 * @see PersistenceManager#flush()
	 */
	public void flush(PersistenceManager pm) throws JDOException {
		pm.flush();
	}

	/**
	 * This implementation delegates to PersistenceManagerFactoryUtils.
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 */
	public DataAccessException translateException(JDOException ex) {
		if (ex.getCause() instanceof SQLException) {
			return getJdbcExceptionTranslator().translate("JDO operation", null, (SQLException) ex.getCause());
		}
		else {
			return PersistenceManagerFactoryUtils.convertJdoAccessException(ex);
		}
	}


	/**
	 * ConnectionHandle implementation that fetches a new JDO2 DataStoreConnection
	 * for every <code>getConnection</code> call and closes the Connection on
	 * <code>releaseConnection</code>. This is necessary because JDO2 requires the
	 * fetched Connection to be closed before continuing PersistenceManager work.
	 * @see PersistenceManager#getDataStoreConnection()
	 */
	private static class DataStoreConnectionHandle implements ConnectionHandle {

		private final PersistenceManager persistenceManager;

		public DataStoreConnectionHandle(PersistenceManager persistenceManager) {
			this.persistenceManager = persistenceManager;
		}

		public Connection getConnection() {
			return (Connection) this.persistenceManager.getDataStoreConnection();
		}

		public void releaseConnection(Connection con) {
			JdbcUtils.closeConnection(con);
		}
	}

}
