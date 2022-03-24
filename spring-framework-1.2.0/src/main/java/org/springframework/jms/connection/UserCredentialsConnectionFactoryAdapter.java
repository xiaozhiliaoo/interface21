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

package org.springframework.jms.connection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;

import org.springframework.util.Assert;

/**
 * An adapter for a target ConnectionFactory, applying the given user credentials
 * to every standard <code>createConnection()</code> call, that is, implicitly
 * invoking <code>createConnection(username, password)</code> on the target.
 * All other methods simply delegate to the corresponding methods of the
 * target ConnectionFactory.
 *
 * <p>Can be used to proxy a target JNDI ConnectionFactory that does not have user
 * credentials configured. Client code can work with the ConnectionFactory without
 * passing in username and password on every <code>createConnection()</code> call.
 *
 * <p>In the following example, client code can simply transparently work with
 * the preconfigured "myConnectionFactory", implicitly accessing
 * "myTargetConnectionFactory" with the specified user credentials.
 *
 * <pre>
 * &lt;bean id="myTargetConnectionFactory" class="org.springframework.jndi.JndiObjectFactoryBean">
 *   &lt;property name="jndiName">&lt;value>java:comp/env/jms/mycf&lt;/value>&lt;/property>
 * &lt;/bean>
 *
 * &lt;bean id="myConnectionFactory" class="org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter">
 *   &lt;property name="targetConnectionFactory">&lt;ref bean="myTargetConnectionFactory"/>&lt;/property>
 *   &lt;property name="username">&lt;value>myusername&lt;/value>&lt;/property>
 *   &lt;property name="password">&lt;value>mypassword&lt;/value>&lt;/property>
 * &lt;/bean></pre>
 *
 * <p>If the "username" is empty, this proxy will simply delegate to the standard
 * <code>createConnection()</code> method of the target ConnectionFactory.
 * This can be used to keep a UserCredentialsConnectionFactoryAdapter bean
 * definition just for the <i>option</i> of implicitly passing in user credentials
 * if  particular target ConnectionFactory requires it.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #createConnection
 * @see #createQueueConnection
 * @see #createTopicConnection
 */
public class UserCredentialsConnectionFactoryAdapter
		implements ConnectionFactory, QueueConnectionFactory, TopicConnectionFactory {

	private ConnectionFactory targetConnectionFactory;

	private String username = "";

	private String password = "";

	private final ThreadLocal threadBoundCredentials = new ThreadLocal();


	/**
	 * Set the target ConnectionFactory that this ConnectionFactory should delegate to.
	 */
	public void setTargetConnectionFactory(ConnectionFactory targetConnectionFactory) {
		this.targetConnectionFactory = targetConnectionFactory;
	}

	/**
	 * Set the username that this adapter should use for retrieving Connections.
	 * Default is the empty string, i.e. no specific user.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Set the password that this adapter should use for retrieving Connections.
	 * Default is the empty string, i.e. no specific password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	/**
	 * Set user credententials for this proxy and the current thread.
	 * The given username and password will be applied to all subsequent
	 * <code>createConnection()</code> calls on this ConnectionFactory proxy.
	 * <p>This will override any statically specified user credentials,
	 * that is, values of the "username" and "password" bean properties.
	 * @param username the username to apply
	 * @param password the password to apply
	 * @see #removeCredentialsFromCurrentThread
	 */
	public void setCredentialsForCurrentThread(String username, String password) {
		this.threadBoundCredentials.set(new String[] {username, password});
	}

	/**
	 * Remove any user credentials for this proxy from the current thread.
	 * Statically specified user credentials apply again afterwards.
	 * @see #setCredentialsForCurrentThread
	 */
	public void removeCredentialsFromCurrentThread() {
		this.threadBoundCredentials.set(null);
	}


	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 * @see #doCreateConnection
	 */
	public final Connection createConnection() throws JMSException {
		String[] threadCredentials = (String[]) this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doCreateConnection(threadCredentials[0], threadCredentials[1]);
		}
		else {
			return doCreateConnection(this.username, this.password);
		}
	}

	/**
	 * Delegate the call straight to the target ConnectionFactory.
	 */
	public Connection createConnection(String username, String password) throws JMSException {
		return doCreateConnection(username, password);
	}

	/**
	 * This implementation delegates to the <code>createConnection(username, password)</code>
	 * method of the target ConnectionFactory, passing in the specified user credentials.
	 * If the specified username is empty, it will simply delegate to the standard
	 * <code>createConnection()</code> method of the target ConnectionFactory.
	 * @param username the username to use
	 * @param password the password to use
	 * @return the Connection
	 * @see ConnectionFactory#createConnection(String, String)
	 * @see ConnectionFactory#createConnection()
	 */
	protected Connection doCreateConnection(String username, String password) throws JMSException {
		Assert.state(this.targetConnectionFactory != null, "targetConnectionFactory is required");
		if (!"".equals(username)) {
			return this.targetConnectionFactory.createConnection(username, password);
		}
		else {
			return this.targetConnectionFactory.createConnection();
		}
	}


	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 * @see #doCreateQueueConnection
	 */
	public final QueueConnection createQueueConnection() throws JMSException {
		String[] threadCredentials = (String[]) this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doCreateQueueConnection(threadCredentials[0], threadCredentials[1]);
		}
		else {
			return doCreateQueueConnection(this.username, this.password);
		}
	}

	/**
	 * Delegate the call straight to the target QueueConnectionFactory.
	 */
	public QueueConnection createQueueConnection(String username, String password) throws JMSException {
		return doCreateQueueConnection(username, password);
	}

	/**
	 * This implementation delegates to the <code>createQueueConnection(username, password)</code>
	 * method of the target QueueConnectionFactory, passing in the specified user credentials.
	 * If the specified username is empty, it will simply delegate to the standard
	 * <code>createQueueConnection()</code> method of the target ConnectionFactory.
	 * @param username the username to use
	 * @param password the password to use
	 * @return the Connection
	 * @see QueueConnectionFactory#createQueueConnection(String, String)
	 * @see QueueConnectionFactory#createQueueConnection()
	 */
	protected QueueConnection doCreateQueueConnection(String username, String password) throws JMSException {
		Assert.state(this.targetConnectionFactory != null, "targetConnectionFactory is required");
		if (!(this.targetConnectionFactory instanceof QueueConnectionFactory)) {
			throw new javax.jms.IllegalStateException("targetConnectionFactory is not a QueueConnectionFactory");
		}
		QueueConnectionFactory queueFactory = (QueueConnectionFactory) this.targetConnectionFactory;
		if (!"".equals(username)) {
			return queueFactory.createQueueConnection(username, password);
		}
		else {
			return queueFactory.createQueueConnection();
		}
	}


	/**
	 * Determine whether there are currently thread-bound credentials,
	 * using them if available, falling back to the statically specified
	 * username and password (i.e. values of the bean properties) else.
	 * @see #doCreateTopicConnection
	 */
	public final TopicConnection createTopicConnection() throws JMSException {
		String[] threadCredentials = (String[]) this.threadBoundCredentials.get();
		if (threadCredentials != null) {
			return doCreateTopicConnection(threadCredentials[0], threadCredentials[1]);
		}
		else {
			return doCreateTopicConnection(this.username, this.password);
		}
	}

	/**
	 * Delegate the call straight to the target TopicConnectionFactory.
	 */
	public TopicConnection createTopicConnection(String username, String password) throws JMSException {
		return doCreateTopicConnection(username, password);
	}

	/**
	 * This implementation delegates to the <code>createTopicConnection(username, password)</code>
	 * method of the target TopicConnectionFactory, passing in the specified user credentials.
	 * If the specified username is empty, it will simply delegate to the standard
	 * <code>createTopicConnection()</code> method of the target ConnectionFactory.
	 * @param username the username to use
	 * @param password the password to use
	 * @return the Connection
	 * @see TopicConnectionFactory#createTopicConnection(String, String)
	 * @see TopicConnectionFactory#createTopicConnection()
	 */
	protected TopicConnection doCreateTopicConnection(String username, String password) throws JMSException {
		Assert.state(this.targetConnectionFactory != null, "targetConnectionFactory is required");
		if (!(this.targetConnectionFactory instanceof TopicConnectionFactory)) {
			throw new javax.jms.IllegalStateException("targetConnectionFactory is not a TopicConnectionFactory");
		}
		TopicConnectionFactory queueFactory = (TopicConnectionFactory) this.targetConnectionFactory;
		if (!"".equals(username)) {
			return queueFactory.createTopicConnection(username, password);
		}
		else {
			return queueFactory.createTopicConnection();
		}
	}

}
