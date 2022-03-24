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

package org.springframework.orm.ibatis;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.sql.DataSource;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ibatis.sqlmap.engine.impl.ExtendedSqlMapClient;
import com.ibatis.sqlmap.engine.transaction.TransactionConfig;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.lob.LobHandler;

/**
 * FactoryBean that creates an iBATIS Database Layer SqlMapClient as singleton
 * in the current bean factory, possibly for use with SqlMapClientTemplate.
 *
 * <p>Allows to specify a DataSource at the SqlMapClient level. This is
 * preferable to per-DAO DataSource references, as it allows for lazy loading
 * and avoids repeated DataSource references.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement API has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 * @see #setConfigLocation
 * @see #setDataSource
 * @see SqlMapClientTemplate#setSqlMapClient
 * @see SqlMapClientTemplate#setDataSource
 */
public class SqlMapClientFactoryBean implements FactoryBean, InitializingBean {

	private static ThreadLocal configTimeLobHandlerHolder = new ThreadLocal();

	/**
	 * Return the LobHandler for the currently configured iBATIS SqlMapClient,
	 * to be used by TypeHandler implementations like ClobStringTypeHandler.
	 * <p>This instance will be set before initialization of the corresponding
	 * SqlMapClient, and reset immediately afterwards. It is thus only available
	 * during configuration.
	 * @see #setLobHandler
	 * @see org.springframework.orm.ibatis.support.ClobStringTypeHandler
	 * @see org.springframework.orm.ibatis.support.BlobByteArrayTypeHandler
	 * @see org.springframework.orm.ibatis.support.BlobSerializableTypeHandler
	 */
	public static LobHandler getConfigTimeLobHandler() {
		return (LobHandler) configTimeLobHandlerHolder.get();
	}


	private Resource configLocation;

	private Properties sqlMapClientProperties;

	private DataSource dataSource;

	private boolean useTransactionAwareDataSource = true;

	private Class transactionConfigClass = ExternalTransactionConfig.class;

	private Properties transactionConfigProperties;

	private LobHandler lobHandler;

	private SqlMapClient sqlMapClient;


	public SqlMapClientFactoryBean() {
		this.transactionConfigProperties = new Properties();
		this.transactionConfigProperties.setProperty("SetAutoCommitAllowed", "false");
	}

	/**
	 * Set the location of the iBATIS SqlMapClient config file.
	 * A typical value is "WEB-INF/sql-map-config.xml".
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set optional properties to be passed into the SqlMapClientBuilder, as
	 * alternative to a <code>&lt;properties&gt;</code> tag in the sql-map-config.xml
	 * file. Will be used to resolve placeholders in the config file.
	 * @see #setConfigLocation
	 * @see SqlMapClientBuilder#buildSqlMapClient(java.io.Reader, Properties)
	 */
	public void setSqlMapClientProperties(Properties sqlMapClientProperties) {
		this.sqlMapClientProperties = sqlMapClientProperties;
	}

	/**
	 * Set the DataSource to be used by iBATIS SQL Maps. This will be passed to the
	 * SqlMapClient as part of a TransactionConfig instance.
	 * <p>If specified, this will override corresponding settings in the SqlMapClient
	 * properties. Usually, you will specify DataSource and transaction configuration
	 * <i>either</i> here <i>or</i> in SqlMapClient properties.
	 * <p>Specifying a DataSource for the SqlMapClient rather than for each individual
	 * DAO allows for lazy loading, for example when using PaginatedList results.
	 * <p>With a DataSource passed in here, you don't need to specify one for each DAO.
	 * Passing the SqlMapClient to the DAOs is enough, as it already carries a DataSource.
	 * Thus, it's recommended to specify the DataSource at this central location only.
	 * <p>Thanks to Brandon Goodin from the iBATIS team for the hint on how to make
	 * this work with Spring's integration strategy!
	 * @see #setTransactionConfigClass
	 * @see #setTransactionConfigProperties
	 * @see SqlMapClient#getDataSource
	 * @see SqlMapClientTemplate#setDataSource
	 * @see SqlMapClientTemplate#queryForPaginatedList
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Set whether to use a transaction-aware DataSource for the SqlMapClient,
	 * i.e. whether to automatically wrap the passed-in DataSource with Spring's
	 * TransactionAwareDataSourceProxy.
	 * <p>Default is true: If the SqlMapClient performs direct database operations
	 * outside of Spring's SqlMapClientTemplate -- for example, lazy loading or
	 * direct SqlMapClient access --, it will still participate in active
	 * Spring-managed transactions.
	 * <p>As a further effect, using a transaction-aware DataSource will apply
	 * remaining transaction timeouts to all created JDBC Statements. This means
	 * that all operations performed by the SqlMapClient will automatically
	 * participate in Spring-managed transaction timeouts.
	 * <p>Turn this flag off to get raw DataSource handling, without Spring transaction
	 * checks. Operations on Spring's SqlMapClientTemplate will still detect
	 * Spring-managed transactions, but lazy loading or direct SqlMapClient access won't.
	 * @see #setDataSource
	 * @see TransactionAwareDataSourceProxy
	 * @see org.springframework.jdbc.datasource.DataSourceTransactionManager
	 * @see SqlMapClientTemplate
	 * @see SqlMapClient
	 */
	public void setUseTransactionAwareDataSource(boolean useTransactionAwareDataSource) {
		this.useTransactionAwareDataSource = useTransactionAwareDataSource;
	}

	/**
	 * Set the iBATIS TransactionConfig class to use. Default is
	 * <code>com.ibatis.sqlmap.engine.transaction.external.ExternalTransactionConfig</code>.
	 * <p>Will only get applied when using a Spring-managed DataSource.
	 * An instance of this class will get populated with the given DataSource
	 * and initialized with the given properties.
	 * <p>The default ExternalTransactionConfig is appropriate if there is
	 * external transaction management that the SqlMapClient should participate
	 * in: be it Spring transaction management, EJB CMT or plain JTA. This
	 * should be the typical scenario. If there is no active transaction,
	 * SqlMapClient operations will execute SQL statements non-transactionally.
	 * <p>JdbcTransactionConfig or JtaTransactionConfig is only necessary
	 * when using the iBATIS SqlMapTransactionManager API instead of external
	 * transactions. If there is no explicit transaction, SqlMapClient operations
	 * will automatically start a transaction for their own scope (in contrast
	 * to the external transaction mode, see above).
	 * <p><b>It is strongly recommended to use iBATIS SQL Maps with Spring
	 * transaction management (or EJB CMT).</b> In this case, the default
	 * ExternalTransactionConfig is fine. Lazy loading and SQL Maps operations
	 * without explicit transaction demarcation will execute non-transactionally.
	 * <p>Even with Spring transaction management, it might be desirable to
	 * specify JdbcTransactionConfig: This will still participate in existing
	 * Spring-managed transactions, but lazy loading and operations without
	 * explicit transaction demaration will execute in their own auto-started
	 * transactions. However, this is usually not necessary.
	 * @see #setDataSource
	 * @see #setTransactionConfigProperties
	 * @see TransactionConfig
	 * @see ExternalTransactionConfig
	 * @see com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig
	 * @see com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig
	 * @see com.ibatis.sqlmap.client.SqlMapTransactionManager
	 	 */
	public void setTransactionConfigClass(Class transactionConfigClass) {
		if (transactionConfigClass == null || !TransactionConfig.class.isAssignableFrom(transactionConfigClass)) {
			throw new IllegalArgumentException("Invalid transactionConfigClass: does not implement " +
					"com.ibatis.sqlmap.engine.transaction.TransactionConfig");
		}
		this.transactionConfigClass = transactionConfigClass;
	}

	/**
	 * Set properties to be passed to the TransactionConfig instance used
	 * by this SqlMapClient. Supported properties depend on the concrete
	 * TransactionConfig implementation used:
	 * <p><ul>
	 * <li><b>ExternalTransactionConfig</b> supports "DefaultAutoCommit"
	 * (default: false) and "SetAutoCommitAllowed" (default: true).
	 * Note that Spring uses SetAutoCommitAllowed = false as default,
	 * in contrast to the iBATIS default, to always keep the original
	 * autoCommit value as provided by the connection pool.
	 * <li><b>JdbcTransactionConfig</b> does not supported any properties.
	 * <li><b>JtaTransactionConfig</b> supports "UserTransaction"
	 * (no default), specifying the JNDI location of the JTA UserTransaction
	 * (usually "java:comp/UserTransaction").
	 * </ul>
	 * @see TransactionConfig#initialize
	 * @see ExternalTransactionConfig
	 * @see com.ibatis.sqlmap.engine.transaction.jdbc.JdbcTransactionConfig
	 * @see com.ibatis.sqlmap.engine.transaction.jta.JtaTransactionConfig
	 */
	public void setTransactionConfigProperties(Properties transactionConfigProperties) {
		this.transactionConfigProperties = transactionConfigProperties;
	}

	/**
	 * Set the LobHandler to be used by the SqlMapClient.
	 * Will be exposed at config time for TypeHandler implementations.
	 * @see #getConfigTimeLobHandler
	 * @see com.ibatis.sqlmap.engine.type.TypeHandler
	 * @see org.springframework.orm.ibatis.support.ClobStringTypeHandler
	 * @see org.springframework.orm.ibatis.support.BlobByteArrayTypeHandler
	 * @see org.springframework.orm.ibatis.support.BlobSerializableTypeHandler
	 */
	public void setLobHandler(LobHandler lobHandler) {
		this.lobHandler = lobHandler;
	}


	public void afterPropertiesSet() throws Exception {
		if (this.configLocation == null) {
			throw new IllegalArgumentException("configLocation is required");
		}

		if (this.lobHandler != null) {
			// Make given LobHandler available for SqlMapClient configuration.
			// Do early because because mapping resource might refer to custom types.
			configTimeLobHandlerHolder.set(this.lobHandler);
		}

		try {
			// Build the SqlMapClient.
			InputStream is = this.configLocation.getInputStream();
			this.sqlMapClient = (this.sqlMapClientProperties != null) ?
					SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is), this.sqlMapClientProperties) :
					SqlMapClientBuilder.buildSqlMapClient(new InputStreamReader(is));

			// Tell the SqlMapClient to use the given DataSource, if any.
			if (this.dataSource != null) {
				TransactionConfig transactionConfig = (TransactionConfig) this.transactionConfigClass.newInstance();
				DataSource dataSourceToUse = this.dataSource;
				if (this.useTransactionAwareDataSource && !(this.dataSource instanceof TransactionAwareDataSourceProxy)) {
					dataSourceToUse = new TransactionAwareDataSourceProxy(this.dataSource);
				}
				transactionConfig.setDataSource(dataSourceToUse);
				transactionConfig.initialize(this.transactionConfigProperties);
				applyTransactionConfig(this.sqlMapClient, transactionConfig);
			}
		}

		finally {
			if (this.lobHandler != null) {
				// Reset LobHandler holder.
				configTimeLobHandlerHolder.set(null);
			}
		}
	}

	/**
	 * Apply the given iBATIS TransactionConfig to the SqlMapClient.
	 * <p>Default implementation casts to ExtendedSqlMapClient, retrieves the maximum
	 * number of concurrent transactions from the SqlMapExecutorDelegate, and sets
	 * an iBATIS TransactionManager with the given TransactionConfig.
	 * @param sqlMapClient the SqlMapClient to apply the TransactionConfig to
	 * @param transactionConfig the iBATIS TransactionConfig to apply
	 * @see ExtendedSqlMapClient
	 * @see com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate#getMaxTransactions
	 * @see com.ibatis.sqlmap.engine.impl.SqlMapExecutorDelegate#setTxManager
	 */
	protected void applyTransactionConfig(SqlMapClient sqlMapClient, TransactionConfig transactionConfig) {
		if (!(this.sqlMapClient instanceof ExtendedSqlMapClient)) {
			throw new IllegalArgumentException(
					"Cannot set TransactionConfig with DataSource for SqlMapClient if not of type " +
					"ExtendedSqlMapClient: " + this.sqlMapClient);
		}
		ExtendedSqlMapClient extendedClient = (ExtendedSqlMapClient) this.sqlMapClient;
		transactionConfig.setMaximumConcurrentTransactions(extendedClient.getDelegate().getMaxTransactions());
		extendedClient.getDelegate().setTxManager(new TransactionManager(transactionConfig));
	}


	public Object getObject() {
		return this.sqlMapClient;
	}

	public Class getObjectType() {
		return (this.sqlMapClient != null ? this.sqlMapClient.getClass() : SqlMapClient.class);
	}

	public boolean isSingleton() {
		return true;
	}

}
