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

import javax.jdo.JDOException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.ConnectionHandle;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * PlatformTransactionManager implementation for a single JDO PersistenceManagerFactory.
 * Binds a JDO PersistenceManager from the specified factory to the thread, potentially
 * allowing for one thread PersistenceManager per factory. PersistenceManagerFactoryUtils
 * and JdoTemplate are aware of thread-bound persistence managers and participate in such
 * transactions automatically. Using either is required for JDO access code supporting
 * this transaction management mechanism.
 *
 * <p>This implementation is appropriate for applications that solely use JDO for
 * transactional data access. JTA (usually through JtaTransactionManager) is necessary for
 * accessing multiple transactional resources. Note that you need to configure your JDO
 * tool accordingly to make it participate in JTA transactions. In contrast to Hibernate,
 * this cannot be transparently provided by the Spring transaction manager implementation.
 *
 * <p>With a JdoDialect specified, this implementation also supports direct DataSource
 * access within a transaction (i.e. plain JDBC code working with the same DataSource).
 * This allows for mixing services that access JDO (including transactional caching)
 * and services that use plain JDBC (without being aware of JDO)!
 * Application code needs to stick to the same simple Connection lookup pattern as
 * with DataSourceTransactionManager (i.e. <code>DataSourceUtils.getConnection</code>).
 *
 * <p>Note that to be able to register a DataSource's Connection for plain JDBC code,
 * this instance needs to be aware of the DataSource (see "dataSource" property).
 * The given DataSource should obviously match the one used by the given
 * PersistenceManagerFactory. This transaction manager will autodetect the DataSource
 * that acts as "connectionFactory" of the PersistenceManagerFactory, so you usually
 * don't need to explicitly specify the "dataSource" property.
 *
 * <p>On JDBC 3.0, this transaction manager supports nested transactions via JDBC
 * 3.0 Savepoints. The "nestedTransactionAllowed" flag defaults to false, though,
 * as nested transactions will just apply to the JDBC Connection, not to the JDO
 * PersistenceManager and its cached objects. You can manually set the flag to true
 * if you want to use nested transactions for JDBC access code that participates
 * in JDO transactions (provided that your JDBC driver supports Savepoints).
 *
 * @author Juergen Hoeller
 * @since 03.06.2003
 * @see #setPersistenceManagerFactory
 * @see #setDataSource
 * @see PersistenceManagerFactory#getConnectionFactory
 * @see LocalPersistenceManagerFactoryBean
 * @see PersistenceManagerFactoryUtils#getPersistenceManager
 * @see PersistenceManagerFactoryUtils#releasePersistenceManager
 * @see JdoTemplate#execute
 * @see org.springframework.jdbc.datasource.DataSourceUtils#getConnection
 * @see org.springframework.jdbc.datasource.DataSourceUtils#releaseConnection
 * @see org.springframework.jdbc.core.JdbcTemplate
 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 */
public class JdoTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

	private PersistenceManagerFactory persistenceManagerFactory;

	private DataSource dataSource;

	private boolean autodetectDataSource = true;

	private JdoDialect jdoDialect = new DefaultJdoDialect();


	/**
	 * Create a new JdoTransactionManager instance.
	 * A PersistenceManagerFactory has to be set to be able to use it.
	 * @see #setPersistenceManagerFactory
	 */
	public JdoTransactionManager() {
	}

	/**
	 * Create a new JdoTransactionManager instance.
	 * @param pmf PersistenceManagerFactory to manage transactions for
	 */
	public JdoTransactionManager(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
		afterPropertiesSet();
	}

	/**
	 * Set the PersistenceManagerFactory that this instance should manage transactions for.
	 * <p>The PersistenceManagerFactory specified here should be the target
	 * PersistenceManagerFactory to manage transactions for, not a
	 * TransactionAwarePersistenceManagerFactoryProxy. Only data access
	 * code may work with TransactionAwarePersistenceManagerFactoryProxy, while the
	 * transaction manager needs to work on the underlying target PersistenceManagerFactory.
	 * @see TransactionAwarePersistenceManagerFactoryProxy
	 */
	public void setPersistenceManagerFactory(PersistenceManagerFactory pmf) {
		this.persistenceManagerFactory = pmf;
	}

	/**
	 * Return the PersistenceManagerFactory that this instance should manage transactions for.
	 */
	public PersistenceManagerFactory getPersistenceManagerFactory() {
		return persistenceManagerFactory;
	}

	/**
	 * Set the JDBC DataSource that this instance should manage transactions for.
   * The DataSource should match the one used by the JDO PersistenceManagerFactory:
	 * for example, you could specify the same JNDI DataSource for both.
	 * <p>If the PersistenceManagerFactory uses a DataSource as connection factory,
	 * the DataSource will be autodetected: You can still explictly specify the
	 * DataSource, but you don't need to in this case.
	 * <p>A transactional JDBC Connection for this DataSource will be provided to
	 * application code accessing this DataSource directly via DataSourceUtils
	 * or JdbcTemplate. The Connection will be taken from the JDO PersistenceManager.
	 * <p>Note that you need to use a JDO dialect for a specific JDO implementation
	 * to allow for exposing JDO transactions as JDBC transactions.
	 * <p>The DataSource specified here should be the target DataSource to manage
	 * transactions for, not a TransactionAwareDataSourceProxy. Only data access
	 * code may work with TransactionAwareDataSourceProxy, while the transaction
	 * manager needs to work on the underlying target DataSource. If there's
	 * nevertheless a TransactionAwareDataSourceProxy passed in, it will be
	 * unwrapped to extract its target DataSource.
	 * @see #setAutodetectDataSource
	 * @see #setJdoDialect
	 * @see PersistenceManagerFactory#getConnectionFactory
	 * @see TransactionAwareDataSourceProxy
	 * @see org.springframework.jdbc.datasource.DataSourceUtils
	 * @see org.springframework.jdbc.core.JdbcTemplate
	 */
	public void setDataSource(DataSource dataSource) {
		if (dataSource instanceof TransactionAwareDataSourceProxy) {
			// If we got a TransactionAwareDataSourceProxy, we need to perform transactions
			// for its underlying target DataSource, else data access code won't see
			// properly exposed transactions (i.e. transactions for the target DataSource).
			this.dataSource = ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource();
		}
		else {
			this.dataSource = dataSource;
		}
	}

	/**
	 * Return the JDBC DataSource that this instance manages transactions for.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Set whether to autodetect a JDBC DataSource used by the JDO PersistenceManagerFactory,
	 * as returned by the <code>getConnectionFactory()</code> method. Default is true.
	 * <p>Can be turned off to deliberately ignore an available DataSource,
	 * to not expose JDO transactions as JDBC transactions for that DataSource.
	 * @see #setDataSource
	 * @see PersistenceManagerFactory#getConnectionFactory
	 */
	public void setAutodetectDataSource(boolean autodetectDataSource) {
		this.autodetectDataSource = autodetectDataSource;
	}

	/**
	 * Set the JDO dialect to use for this transaction manager.
	 * <p>The dialect object can be used to retrieve the underlying JDBC connection
	 * and thus allows for exposing JDO transactions as JDBC transactions.
	 * @see JdoDialect#getJdbcConnection
	 */
	public void setJdoDialect(JdoDialect jdoDialect) {
		this.jdoDialect = jdoDialect;
	}

	/**
	 * Return the JDO dialect to use for this transaction manager.
	 * Creates a default one for the specified PersistenceManagerFactory if none set.
	 */
	public JdoDialect getJdoDialect() {
		if (this.jdoDialect == null) {
			this.jdoDialect = new DefaultJdoDialect(getPersistenceManagerFactory());
		}
		return this.jdoDialect;
	}

	/**
	 * Eagerly initialize the JDO dialect, creating a default one
	 * for the specified PersistenceManagerFactory if none set.
	 * Auto-detect the PersistenceManagerFactory's DataSource, if any.
	 */
	public void afterPropertiesSet() {
		if (getPersistenceManagerFactory() == null) {
			throw new IllegalArgumentException("persistenceManagerFactory is required");
		}
		getJdoDialect();

		// Check for DataSource as connection factory.
		if (this.autodetectDataSource && getDataSource() == null) {
			Object pmfcf = getPersistenceManagerFactory().getConnectionFactory();
			if (pmfcf instanceof DataSource) {
				// Use the PersistenceManagerFactory's DataSource for exposing transactions to JDBC code.
				this.dataSource = (DataSource) pmfcf;
				if (logger.isInfoEnabled()) {
					logger.info("Using DataSource [" + this.dataSource +
							"] of JDO PersistenceManagerFactory for JdoTransactionManager");
				}
			}
		}
	}


	protected Object doGetTransaction() {
		JdoTransactionObject txObject = new JdoTransactionObject();
		txObject.setSavepointAllowed(isNestedTransactionAllowed());

		if (TransactionSynchronizationManager.hasResource(getPersistenceManagerFactory())) {
			PersistenceManagerHolder pmHolder = (PersistenceManagerHolder)
					TransactionSynchronizationManager.getResource(getPersistenceManagerFactory());
			if (logger.isDebugEnabled()) {
				logger.debug("Found thread-bound persistence manager [" +
						pmHolder.getPersistenceManager() + "] for JDO transaction");
			}
			txObject.setPersistenceManagerHolder(pmHolder, false);
			if (getDataSource() != null) {
				ConnectionHolder conHolder = (ConnectionHolder)
						TransactionSynchronizationManager.getResource(getDataSource());
				txObject.setConnectionHolder(conHolder);
			}
		}

		return txObject;
	}

	protected boolean isExistingTransaction(Object transaction) {
		return ((JdoTransactionObject) transaction).hasTransaction();
	}

	protected void doBegin(Object transaction, TransactionDefinition definition) {
		if (getDataSource() != null && TransactionSynchronizationManager.hasResource(getDataSource())) {
			throw new IllegalTransactionStateException(
					"Pre-bound JDBC connection found - JdoTransactionManager does not support " +
					"running within DataSourceTransactionManager if told to manage the DataSource itself. " +
					"It is recommended to use a single JdoTransactionManager for all transactions " +
					"on a single DataSource, no matter whether JDO or JDBC access.");
		}

		if (definition.isReadOnly()) {
			logger.info("JdoTransactionManager does not support read-only transactions: ignoring 'readOnly' hint");
		}

		PersistenceManager pm = null;

		try {
			JdoTransactionObject txObject = (JdoTransactionObject) transaction;
			if (txObject.getPersistenceManagerHolder() == null) {
				PersistenceManager newPm = getPersistenceManagerFactory().getPersistenceManager();
				if (logger.isDebugEnabled()) {
					logger.debug("Opened new persistence manager [" + newPm + "] for JDO transaction");
				}
				txObject.setPersistenceManagerHolder(new PersistenceManagerHolder(newPm), true);
			}

			txObject.getPersistenceManagerHolder().setSynchronizedWithTransaction(true);
			pm = txObject.getPersistenceManagerHolder().getPersistenceManager();

			// Delegate to JdoDialect for actual transaction begin.
			Object transactionData = getJdoDialect().beginTransaction(pm.currentTransaction(), definition);
			txObject.setTransactionData(transactionData);

			// Register transaction timeout.
			if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getPersistenceManagerHolder().setTimeoutInSeconds(definition.getTimeout());
			}

			// Register the JDO PersistenceManager's JDBC Connection for the DataSource, if set.
			if (getDataSource() != null) {
				ConnectionHandle conHandle = getJdoDialect().getJdbcConnection(pm, definition.isReadOnly());
				if (conHandle != null) {
					ConnectionHolder conHolder = new ConnectionHolder(conHandle);
					if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
						conHolder.setTimeoutInSeconds(definition.getTimeout());
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Exposing JDO transaction as JDBC transaction [" + conHolder.getConnectionHandle() + "]");
					}
					TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
					txObject.setConnectionHolder(conHolder);
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Not exposing JDO transaction [" + pm + "] as JDBC transaction because JdoDialect [" +
								getJdoDialect() + "] does not support JDBC connection retrieval");
					}
				}
			}

			// Bind the persistence manager holder to the thread.
			if (txObject.isNewPersistenceManagerHolder()) {
				TransactionSynchronizationManager.bindResource(
						getPersistenceManagerFactory(), txObject.getPersistenceManagerHolder());
			}
		}

		catch (TransactionException ex) {
			PersistenceManagerFactoryUtils.releasePersistenceManager(pm, getPersistenceManagerFactory());
			throw ex;
		}
		catch (Exception ex) {
			PersistenceManagerFactoryUtils.releasePersistenceManager(pm, getPersistenceManagerFactory());
			throw new CannotCreateTransactionException("Could not open JDO persistence manager for transaction", ex);
		}
	}

	protected Object doSuspend(Object transaction) {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;
		txObject.setPersistenceManagerHolder(null, false);
		PersistenceManagerHolder persistenceManagerHolder = (PersistenceManagerHolder)
				TransactionSynchronizationManager.unbindResource(getPersistenceManagerFactory());
		ConnectionHolder connectionHolder = null;
		if (getDataSource() != null) {
			connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
		}
		return new SuspendedResourcesHolder(persistenceManagerHolder, connectionHolder);
	}

	protected void doResume(Object transaction, Object suspendedResources) {
		SuspendedResourcesHolder resourcesHolder = (SuspendedResourcesHolder) suspendedResources;
		TransactionSynchronizationManager.bindResource(
				getPersistenceManagerFactory(), resourcesHolder.getPersistenceManagerHolder());
		if (getDataSource() != null) {
			TransactionSynchronizationManager.bindResource(getDataSource(), resourcesHolder.getConnectionHolder());
		}
	}

	protected void doCommit(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Committing JDO transaction on persistence manager [" +
					txObject.getPersistenceManagerHolder().getPersistenceManager() + "]");
		}
		try {
			txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().commit();
		}
		catch (JDOException ex) {
			// assumably failed to flush changes to database
			throw convertJdoAccessException(ex);
		}
	}

	protected void doRollback(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Rolling back JDO transaction on persistence manager [" +
					txObject.getPersistenceManagerHolder().getPersistenceManager() + "]");
		}
		try {
			txObject.getPersistenceManagerHolder().getPersistenceManager().currentTransaction().rollback();
		}
		catch (JDOException ex) {
			throw new TransactionSystemException("Could not roll back JDO transaction", ex);
		}
	}

	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		JdoTransactionObject txObject = (JdoTransactionObject) status.getTransaction();
		logger.debug("Setting JDO transaction rollback-only");
		txObject.setRollbackOnly();
	}

	protected void doCleanupAfterCompletion(Object transaction) {
		JdoTransactionObject txObject = (JdoTransactionObject) transaction;

		// Remove the persistence manager holder from the thread.
		if (txObject.isNewPersistenceManagerHolder()) {
			TransactionSynchronizationManager.unbindResource(getPersistenceManagerFactory());
		}
		txObject.getPersistenceManagerHolder().clear();

		// Remove the JDBC connection holder from the thread, if exposed.
		if (txObject.hasConnectionHolder()) {
			TransactionSynchronizationManager.unbindResource(getDataSource());
			try {
				getJdoDialect().releaseJdbcConnection(txObject.getConnectionHolder().getConnectionHandle(),
						txObject.getPersistenceManagerHolder().getPersistenceManager());
			}
			catch (Exception ex) {
				// Just log it, to keep a transaction-related exception.
				logger.error("Could not close JDBC connection after transaction", ex);
			}
		}

		getJdoDialect().cleanupTransaction(txObject.getTransactionData());

		// Remove the persistence manager holder from the thread.
		if (txObject.isNewPersistenceManagerHolder()) {
			PersistenceManager pm = txObject.getPersistenceManagerHolder().getPersistenceManager();
			if (logger.isDebugEnabled()) {
				logger.debug("Closing JDO persistence manager [" + pm + "] after transaction");
			}
			PersistenceManagerFactoryUtils.releasePersistenceManager(pm, getPersistenceManagerFactory());
		}
		else {
			logger.debug("Not closing pre-bound JDO persistence manager after transaction");
		}
	}

	/**
	 * Convert the given JDOException to an appropriate exception from the
	 * org.springframework.dao hierarchy. Delegates to the JdoDialect if set, falls
	 * back to PersistenceManagerFactoryUtils' standard exception translation else.
	 * May be overridden in subclasses.
	 * @param ex JDOException that occured
	 * @return the corresponding DataAccessException instance
	 * @see JdoDialect#translateException
	 * @see PersistenceManagerFactoryUtils#convertJdoAccessException
	 */
	protected DataAccessException convertJdoAccessException(JDOException ex) {
		return getJdoDialect().translateException(ex);
	}


	/**
	 * JDO transaction object, representing a PersistenceManagerHolder.
	 * Used as transaction object by JdoTransactionManager.
	 *
	 * <p>Derives from JdbcTransactionObjectSupport to inherit the capability
	 * to manage JDBC 3.0 Savepoints for underlying JDBC Connections.
	 *
	 * @see PersistenceManagerHolder
	 */
	private static class JdoTransactionObject extends JdbcTransactionObjectSupport {

		private PersistenceManagerHolder persistenceManagerHolder;

		private boolean newPersistenceManagerHolder;

		private Object transactionData;

		public void setPersistenceManagerHolder(
				PersistenceManagerHolder persistenceManagerHolder, boolean newPersistenceManagerHolder) {
			this.persistenceManagerHolder = persistenceManagerHolder;
			this.newPersistenceManagerHolder = newPersistenceManagerHolder;
		}

		public PersistenceManagerHolder getPersistenceManagerHolder() {
			return persistenceManagerHolder;
		}

		public boolean isNewPersistenceManagerHolder() {
			return newPersistenceManagerHolder;
		}

		public boolean hasTransaction() {
			return (this.persistenceManagerHolder != null &&
					this.persistenceManagerHolder.getPersistenceManager() != null &&
					this.persistenceManagerHolder.getPersistenceManager().currentTransaction().isActive());
		}

		public void setTransactionData(Object transactionData) {
			this.transactionData = transactionData;
		}

		public Object getTransactionData() {
			return transactionData;
		}

		public void setRollbackOnly() {
			getPersistenceManagerHolder().setRollbackOnly();
			if (getConnectionHolder() != null) {
				getConnectionHolder().setRollbackOnly();
			}
		}

		public boolean isRollbackOnly() {
			return getPersistenceManagerHolder().isRollbackOnly();
		}
	}


	/**
	 * Holder for suspended resources.
	 * Used internally by doSuspend and doResume.
	 */
	private static class SuspendedResourcesHolder {

		private final PersistenceManagerHolder persistenceManagerHolder;

		private final ConnectionHolder connectionHolder;

		private SuspendedResourcesHolder(PersistenceManagerHolder pmHolder, ConnectionHolder conHolder) {
			this.persistenceManagerHolder = pmHolder;
			this.connectionHolder = conHolder;
		}

		private PersistenceManagerHolder getPersistenceManagerHolder() {
			return persistenceManagerHolder;
		}

		private ConnectionHolder getConnectionHolder() {
			return connectionHolder;
		}
	}

}
