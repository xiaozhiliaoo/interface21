/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx.access;

import java.net.MalformedURLException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.springframework.core.JdkVersion;

/**
 * @author Rob Harrop
 */
public class RemoteMBeanClientInterceptorTests extends MBeanClientInterceptorTests {

	private static final String SERVICE_URL = "service:jmx:jmxmp://localhost:9876";

	private JMXConnectorServer connectorServer;

	private JMXConnector connector;

	public void setUp() throws Exception {
		super.setUp();
		this.connectorServer = JMXConnectorServerFactory.newJMXConnectorServer(getServiceUrl(), null, server);
		this.connectorServer.start();
	}

	private JMXServiceURL getServiceUrl() throws MalformedURLException {
		return new JMXServiceURL(SERVICE_URL);
	}

	protected MBeanServerConnection getServerConnection() throws Exception {
		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			// to avoid NoClassDefFoundError for JSSE
			return super.getServerConnection();
		}
		
		this.connector = JMXConnectorFactory.connect(getServiceUrl());
		return this.connector.getMBeanServerConnection();
	}

	public void tearDown() throws Exception {
		if (this.connector != null) {
			this.connector.close();
		}
		this.connectorServer.stop();
		super.tearDown();
	}

}
