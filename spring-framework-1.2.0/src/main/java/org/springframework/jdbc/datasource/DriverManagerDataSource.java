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

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.util.StringUtils;

/**
 * Simple implementation of the standard JDBC DataSource interface, configuring
 * a plain old JDBC Driver via bean properties, and returning a new Connection
 * for every <code>getConnection</code> call.
 *
 * <p>Useful for test or standalone environments outside of a J2EE container, either
 * as a DataSource bean in a respective ApplicationContext, or in conjunction with a
 * simple JNDI environment. Pool-assuming <code>Connection.close()</code> calls will
 * simply close the Connection, so any DataSource-aware persistence code should work.
 *
 * <p>In a J2EE container, it is recommended to use a JNDI DataSource provided by
 * the container. Such a DataSource can be exported as a DataSource bean in an
 * ApplicationContext via JndiObjectFactoryBean, for seamless switching to and from
 * a local DataSource bean like this class.
 *
 * <p>If you need a "real" connection pool outside of a J2EE container, consider
 * <a href="http://jakarta.apache.org/commons/dbcp">Apache's Jakarta Commons DBCP</a>.
 * Its BasicDataSource is a full connection pool bean, supporting the same basic
 * properties as this class plus specific settings. It can be used as a replacement
 * for an instance of this class just by changing the class name of the bean
 * definition to "org.apache.commons.dbcp.BasicDataSource".
 *
 * @author Juergen Hoeller
 * @since 14.03.2003
 * @see org.springframework.jndi.JndiObjectFactoryBean
 * @see org.springframework.mock.jndi.SimpleNamingContextBuilder
 * @see org.apache.commons.dbcp.BasicDataSource
 */
public class DriverManagerDataSource extends AbstractDataSource {

	private String driverClassName;

	private String url;

	private String username;

	private String password;


	/**
	 * Constructor for bean-style configuration.
	 */
	public DriverManagerDataSource() {
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard
	 * DriverManager parameters.
	 * @param driverClassName the JDBC driver class name
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param username the JDBC username to use for accessing the DriverManager
	 * @param password the JDBC password to use for accessing the DriverManager
	 * @see DriverManager#getConnection(String, String, String)
	 */
	public DriverManagerDataSource(String driverClassName, String url, String username, String password)
			throws CannotGetJdbcConnectionException {
		setDriverClassName(driverClassName);
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Create a new DriverManagerDataSource with the given standard
	 * DriverManager parameters.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @param username the JDBC username to use for accessing the DriverManager
	 * @param password the JDBC password to use for accessing the DriverManager
	 * @see DriverManager#getConnection(String, String, String)
	 */
	public DriverManagerDataSource(String url, String username, String password)
			throws CannotGetJdbcConnectionException {
		setUrl(url);
		setUsername(username);
		setPassword(password);
	}

	/**
	 * Create a new DriverManagerDataSource with the given JDBC URL,
	 * not specifying a username or password for JDBC access.
	 * @param url the JDBC URL to use for accessing the DriverManager
	 * @see DriverManager#getConnection(String)
	 */
	public DriverManagerDataSource(String url)
			throws CannotGetJdbcConnectionException {
		setUrl(url);
	}


	/**
	 * Set the JDBC driver class name. This driver will get initialized
	 * on startup, registering itself with the JDK's DriverManager.
	 * <p>Alternatively, consider initializing the JDBC driver yourself
	 * before instantiating this DataSource.
	 * @see Class#forName(String)
	 * @see DriverManager#registerDriver(java.sql.Driver)
	 */
	public void setDriverClassName(String driverClassName) throws CannotGetJdbcConnectionException {
		if (!StringUtils.hasText(driverClassName)) {
			throw new IllegalArgumentException("driverClassName must not be empty");
		}
		this.driverClassName = driverClassName;
		try {
			Class.forName(this.driverClassName, true, Thread.currentThread().getContextClassLoader());
		}
		catch (ClassNotFoundException ex) {
			throw new CannotGetJdbcConnectionException(
					"Could not load JDBC driver class [" + this.driverClassName + "]", ex);
		}
		if (logger.isInfoEnabled()) {
			logger.info("Loaded JDBC driver: " + this.driverClassName);
		}
	}

	/**
	 * Return the JDBC driver class name, if any.
	 */
	public String getDriverClassName() {
		return driverClassName;
	}

	/**
	 * Set the JDBC URL to use for accessing the DriverManager.
	 * @see DriverManager#getConnection(String, String, String)
	 */
	public void setUrl(String url) {
		if (!StringUtils.hasText(url)) {
			throw new IllegalArgumentException("url must not be empty");
		}
		this.url = url;
	}

	/**
	 * Return the JDBC URL to use for accessing the DriverManager.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the JDBC username to use for accessing the DriverManager.
	 * @see DriverManager#getConnection(String, String, String)
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Return the JDBC username to use for accessing the DriverManager.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the JDBC password to use for accessing the DriverManager.
	 * @see DriverManager#getConnection(String, String, String)
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Return the JDBC password to use for accessing the DriverManager.
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * This implementation delegates to <code>getConnectionFromDriverManager</code>,
	 * using the default username and password of this DataSource.
	 * @see #getConnectionFromDriverManager()
	 */
	public Connection getConnection() throws SQLException {
		return getConnectionFromDriverManager();
	}

	/**
	 * This implementation delegates to <code>getConnectionFromDriverManager</code>,
	 * using the given username and password.
	 * @see #getConnectionFromDriverManager(String, String, String)
	 */
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnectionFromDriverManager(getUrl(), username, password);
	}

	/**
	 * Get a Connection from the DriverManager,
	 * using the default username and password of this DataSource.
	 * @see #getConnectionFromDriverManager(String, String, String)
	 */
	protected Connection getConnectionFromDriverManager() throws SQLException {
		return getConnectionFromDriverManager(getUrl(), getUsername(), getPassword());
	}

	/**
	 * Getting a connection using the nasty static from DriverManager is extracted
	 * into a protected method to allow for easy unit testing.
	 * @see DriverManager#getConnection(String, String, String)
	 */
	protected Connection getConnectionFromDriverManager(String url, String username, String password)
	    throws SQLException {

		if (logger.isDebugEnabled()) {
			logger.debug("Creating new JDBC connection to [" + url + "]");
		}
		return DriverManager.getConnection(url, username, password);
	}

}
