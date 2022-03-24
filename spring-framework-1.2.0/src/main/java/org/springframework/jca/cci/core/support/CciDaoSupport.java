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

package org.springframework.jca.cci.core.support;

import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jca.cci.CannotGetCciConnectionException;
import org.springframework.jca.cci.connection.ConnectionFactoryUtils;
import org.springframework.jca.cci.core.CciTemplate;

/**
 * Convenient super class for CCI data access objects.
 * Requires a ConnectionFactory to be set, providing a
 * CciTemplate based on it to subclasses.
 *
 * <p>This base class is mainly intended for CciTemplate usage
 * but can also be used when working with ConnectionFactoryUtils directly
 * or with org.springframework.cci.object classes.
 *
 * @author Thierry Templier
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setConnectionFactory
 * @see CciTemplate
 * @see ConnectionFactoryUtils
 */
public abstract class CciDaoSupport implements InitializingBean {

	protected final Log logger = LogFactory.getLog(getClass());

	private CciTemplate cciTemplate;


	/**
	 * Set the ConnectionFactory to be used by this DAO.
	 */
	public final void setConnectionFactory(ConnectionFactory connectionFactory) {
	  this.cciTemplate = createCciTemplate(connectionFactory);
	}

	/**
	 * Create a CciTemplate for the given ConnectionFactory.
	 * Only invoked if populating the DAO with a ConnectionFactory reference!
	 * <p>Can be overridden in subclasses to provide a CciTemplate instance
	 * with different configuration, or a custom CciTemplate subclass.
	 * @param connectionFactory the CCI ConnectionFactory to create a CciTemplate for
	 * @return the new CciTemplate instance
	 * @see #setConnectionFactory(javax.resource.cci.ConnectionFactory)
	 */
	protected CciTemplate createCciTemplate(ConnectionFactory connectionFactory) {
		return new CciTemplate(connectionFactory);
	}

	/**
	 * Return the ConnectionFactory used by this DAO.
	 */
	public final ConnectionFactory getConnectionFactory() {
		return this.cciTemplate.getConnectionFactory();
	}

	/**
	 * Set the CciTemplate for this DAO explicitly,
	 * as an alternative to specifying a ConnectionFactory.
	 */
	public final void setCciTemplate(CciTemplate cciTemplate) {
		this.cciTemplate = cciTemplate;
	}

	/**
	 * Return the CciTemplate for this DAO,
	 * pre-initialized with the ConnectionFactory or set explicitly.
	 */
	public final CciTemplate getCciTemplate() {
	  return cciTemplate;
	}

	public final void afterPropertiesSet() throws Exception {
		if (this.cciTemplate == null) {
			throw new IllegalArgumentException("connectionFactory or cciTemplate is required");
		}
		initDao();
	}

	/**
	 * Subclasses can override this for custom initialization behavior.
	 * Gets called after population of this instance's bean properties.
	 * @throws Exception if initialization fails
	 */
	protected void initDao() throws Exception {
	}


	/**
	 * Get a CCI Connection, either from the current transaction or a new one.
	 * @return the CCI Connection
	 * @throws CannotGetCciConnectionException
	 * if the attempt to get a Connection failed
	 * @see ConnectionFactoryUtils#getConnection(javax.resource.cci.ConnectionFactory)
	 */
	protected final Connection getConnection() throws CannotGetCciConnectionException {
		return ConnectionFactoryUtils.getConnection(getConnectionFactory());
	}

	/**
	 * Close the given CCI Connection, created via this bean's ConnectionFactory,
	 * if it isn't bound to the thread.
	 * @param con Connection to close
	 * @see ConnectionFactoryUtils#releaseConnection
	 */
	protected final void releaseConnection(Connection con) {
		ConnectionFactoryUtils.releaseConnection(con, getConnectionFactory());
	}

}
