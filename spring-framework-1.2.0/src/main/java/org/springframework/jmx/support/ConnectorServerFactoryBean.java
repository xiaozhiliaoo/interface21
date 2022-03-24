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

package org.springframework.jmx.support;

import java.io.IOException;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NestedRuntimeException;

/**
 * <code>FactoryBean</code> that creates a JSR-160 <code>JMXConnectorServer</code>,
 * optionally registers it with the <code>MBeanServer</code> and then starts it.
 *
 * <p>The <code>JMXConnectorServer</code> can be started in a separate thread by setting the
 * <code>threaded</code> property to <code>true</code>. You can configure this thread to be a
 * daemon thread by setting the <code>daemon</code> property to <code>true</code>.
 *
 * <p>The <code>JMXConnectorServer</code> is correctly shutdown when an instance of this
 * class is destroyed on shutdown of the containing <code>ApplicationContext</code>.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see	FactoryBean
 * @see JMXConnectorServer
 * @see MBeanServer
 */
public class ConnectorServerFactoryBean implements FactoryBean, InitializingBean, DisposableBean {

	/**
	 * The default service URL.
	 */
	public static final String DEFAULT_SERVICE_URL = "service:jmx:jmxmp://localhost:9875";


	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Stores a reference to the <code>MBeanServer</code> that the connector is exposing.
	 */
	private MBeanServer server;

	/**
	 * Stores the actual service URL used for the connector.
	 */
	private String serviceUrl = DEFAULT_SERVICE_URL;

	/**
	 * Stores the JSR-160 environment parameters to pass to the <code>JMXConnectorServerFactory</code>.
	 */
	private Map environment;

	/**
	 * The <code>String</code> representation of the <code>ObjectName</code> for the
	 * <code>JMXConnectorServer</code>.
	 */
	private ObjectName objectName;

	/**
	 * Indicates whether or not the <code>JMXConnectorServer</code> should be started in a
	 * separate thread.
	 */
	private boolean threaded = false;

	/**
	 * Indicates whether or not the <code>JMXConnectorServer</code> should be started in a
	 * daemon thread. Only applicable if <code>threaded</code> is set to <code>true</code>.
	 */
	private boolean daemon = false;

	/**
	 * Stores the <code>JMXConnectoreServer</code> instance.
	 */
	private JMXConnectorServer connectorServer;


	/**
	 * Set the <code>MBeanServer</code> that the <code>JMXConnectorServer</code>
	 * should expose.
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Set the service URL for the <code>JMXConnectorServer</code>.
	 */
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	/**
	 * Set the environment properties used to construct the <code>JMXConnectorServer</code>.
	 */
	public void setEnvironment(Map environment) {
		this.environment = environment;
	}

	/**
	 * Set the <code>ObjectName</code> used to register the <code>JMXConnectorServer</code>
	 * itself with the <code>MBeanServer</code>.
	 * @throws MalformedObjectNameException if the <code>ObjectName</code> is malformed
	 */
	public void setObjectName(String objectName) throws MalformedObjectNameException {
		this.objectName = ObjectNameManager.getInstance(objectName);
	}

	/**
	 * Set the <code>threaded</code> flag, indicating whether the <code>JMXConnectorServer</code>
	 * should be started in a separate thread.
	 */
	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	/**
	 * Set the <code>daemon</code> flag, indicating whether any threads started for the
	 * <code>JMXConnectorServer</code> should be started daemon threads.
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}


	/**
	 * Start the connector server If the <code>threaded</code> flag is set to <code>true</code>,
	 * the <code>JMXConnectorServer</code> will be started in a separate thread.
	 * If the <code>daemon</code> flag is set to <code>true</code>, that thread will be
	 * started as a daemon thread.
	 * @throws JMException if a problem occured when registering the connector server
	 * with the <code>MBeanServer</code>
	 * @throws IOException if there is a problem starting the connector server
	 */
	public void afterPropertiesSet() throws JMException, IOException {
		if (this.server == null) {
			this.server = JmxUtils.locateMBeanServer();
		}

		// Create the JMX service URL.
		JMXServiceURL url = new JMXServiceURL(this.serviceUrl);

		// Create the connector server now.
		this.connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(url, this.environment, this.server);

		// Do we want to register the connector with the MBean server?
		if (this.objectName != null) {
			this.server.registerMBean(this.connectorServer, this.objectName);
		}

		try {
			if (this.threaded) {
				// Start the connector server asynchronously (in a separate thread).
				Thread connectorThread = new Thread() {
					public void run() {
						try {
							connectorServer.start();
						}
						catch (IOException ex) {
							throw new DelayedConnectorStartException(ex);
						}
					}
				};

				connectorThread.setName("JMX Connector Thread [" + this.serviceUrl + "]");
				connectorThread.setDaemon(this.daemon);
				connectorThread.start();
			}
			else {
				// Start the connector server in the same thread.
				this.connectorServer.start();
			}

			if (logger.isInfoEnabled()) {
				logger.info("JMX connector server started: " + this.connectorServer);
			}
		}

		catch (IOException ex) {
			// Unregister the connector server if startup failed.
			unregisterConnectorServer();
			throw ex;
		}
	}


	public Object getObject() {
		return this.connectorServer;
	}

	public Class getObjectType() {
		return (this.connectorServer != null ? this.connectorServer.getClass() : JMXConnectorServer.class);
	}

	public boolean isSingleton() {
		return true;
	}


	/**
	 * Stop the <code>JMXConnectorServer</code> managed by an instance of this class.
	 * Automatically called on <code>ApplicationContext</code> shutdown.
	 * @throws IOException if there is an error stopping the connector server
	 */
	public void destroy() throws IOException {
		if (logger.isInfoEnabled()) {
			logger.info("Stopping JMX connector server: " + this.connectorServer);
		}
		try {
			this.connectorServer.stop();
		}
		finally {
			unregisterConnectorServer();
		}
	}

	/**
	 * Unregister the connection server from the <code>MBeanServer</code>.
	 * Logs an exception instead of rethrowing it.
	 */
	private void unregisterConnectorServer() {
		if (this.objectName != null) {
			try {
				this.server.unregisterMBean(this.objectName);
			}
			catch (JMException ex) {
				logger.error("Could not unregister JMX connector server", ex);
			}
		}
	}


	/**
	 * Exception to be thrown if the JMX connector server cannot be started
	 * (in a concurrent thread).
	 */
	public static class DelayedConnectorStartException extends NestedRuntimeException {

		private DelayedConnectorStartException(IOException ex) {
			super("Could not start JMX connector server after delay", ex);
		}
	}

}
