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

package org.springframework.jdbc.support;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for JdbcTemplate and other JDBC-accessing DAO helpers,
 * defining common properties like DataSource and exception translator.
 *
 * <p>Not intended to be used directly. See JdbcTemplate.
 *
 * @author Juergen Hoeller
 * @since 28.11.2003
 * @see org.springframework.jdbc.core.JdbcTemplate
 */
public abstract class JdbcAccessor implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	/** Used to obtain connections throughout the lifecycle of this object */
	private DataSource dataSource;

	/** Helper to translate SQL exceptions to DataAccessExceptions */
	private SQLExceptionTranslator exceptionTranslator;

	private boolean lazyInit = true;


	/**
	 * Set the JDBC DataSource to obtain connections from.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Return the DataSource used by this template.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Specify the database product name for the DataSource that this accessor uses.
	 * This allows to initialize a SQLErrorCodeSQLExceptionTranslator without
	 * obtaining a Connection from the DataSource to get the metadata.
	 * @param dbName the database product name that identifies the error codes entry
	 * @see SQLErrorCodeSQLExceptionTranslator#setDatabaseProductName
	 * @see java.sql.DatabaseMetaData#getDatabaseProductName()
	 */
	public void setDatabaseProductName(String dbName) {
		this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dbName);
	}

	/**
	 * Set the exception translator for this instance.
	 * <p>If no custom translator is provided, a default SQLErrorCodeSQLExceptionTranslator
	 * is used which examines the SQLException's vendor-specific error code.
	 * @param exceptionTranslator exception translator
	 * @see SQLErrorCodeSQLExceptionTranslator
	 * @see SQLStateSQLExceptionTranslator
	 */
	public void setExceptionTranslator(SQLExceptionTranslator exceptionTranslator) {
		this.exceptionTranslator = exceptionTranslator;
	}

	/**
	 * Return the exception translator for this instance.
	 * <p>Creates a default SQLErrorCodeSQLExceptionTranslator for the specified
	 * DataSource if none set.
	 */
	public SQLExceptionTranslator getExceptionTranslator() {
		if (this.exceptionTranslator == null) {
			DataSource dataSource = getDataSource();
			if (dataSource != null) {
				this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
			}
			else {
				this.exceptionTranslator = new SQLStateSQLExceptionTranslator();
			}
		}
		return this.exceptionTranslator;
	}

	/**
	 * Set whether to lazily initialize the SQLExceptionTranslator for this accessor,
	 * on first encounter of a SQLException. Default is "true"; can be switched to
	 * "false" for initialization on startup.
	 * <p>Early initialization only applies if <code>afterPropertiesSet</code> is called.
	 * @see #getExceptionTranslator
	 * @see #afterPropertiesSet
	 */
	public void setLazyInit(boolean lazyInit) {
		this.lazyInit = lazyInit;
	}

	/**
	 * Return whether to lazily initialize the SQLExceptionTranslator for this accessor.
	 */
	public boolean isLazyInit() {
		return lazyInit;
	}

	/**
	 * Eagerly initialize the exception translator,
	 * creating a default one for the specified DataSource if none set.
	 */
	public void afterPropertiesSet() {
		if (getDataSource() == null) {
			throw new IllegalArgumentException("dataSource is required");
		}
		if (!isLazyInit()) {
			getExceptionTranslator();
		}
	}
	
}
