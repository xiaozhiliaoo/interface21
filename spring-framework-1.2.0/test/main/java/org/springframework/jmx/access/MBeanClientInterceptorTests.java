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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.MBeanServerConnection;

import org.springframework.jmx.AbstractJmxTests;
import org.springframework.jmx.IJmxTestBean;
import org.springframework.jmx.JmxTestBean;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.assembler.AbstractReflectiveMBeanInfoAssembler;

/**
 * @author Rob Harrop
 */
public class MBeanClientInterceptorTests extends AbstractJmxTests {

	protected static final String OBJECT_NAME = "spring:test=proxy";

	protected JmxTestBean target;

	public void setUp() throws Exception {
		super.setUp();

		target = new JmxTestBean();
		target.setAge(100);
		target.setName("Rob Harrop");

		MBeanExporter adapter = new MBeanExporter();
		Map beans = new HashMap();
		beans.put(OBJECT_NAME, target);
		adapter.setServer(server);
		adapter.setBeans(beans);
		adapter.setAssembler(new ProxyTestAssembler());
		adapter.afterPropertiesSet();
	}

	protected MBeanServerConnection getServerConnection() throws Exception {
		return server;
	}

	protected IJmxTestBean getProxy() throws Exception {
		MBeanProxyFactoryBean factory = new MBeanProxyFactoryBean();
		factory.setServer(getServerConnection());
		factory.setProxyInterface(IJmxTestBean.class);
		factory.setObjectName(OBJECT_NAME);
		factory.afterPropertiesSet();
		return (IJmxTestBean) factory.getObject();
	}

	public void testProxyClassIsDifferent() throws Exception {
		IJmxTestBean proxy = getProxy();
		assertTrue("The proxy class should be different than the base class",
				(proxy.getClass() != IJmxTestBean.class));
	}

	public void testDifferentProxiesSameClass() throws Exception {
		IJmxTestBean proxy1 = getProxy();
		IJmxTestBean proxy2 = getProxy();

		assertNotSame("The proxies should NOT be the same", proxy1, proxy2);
		assertSame("The proxy classes should be the same", proxy1.getClass(), proxy2.getClass());
	}

	public void testGetAttributeValue() throws Exception {
		IJmxTestBean proxy1 = getProxy();
		int age = proxy1.getAge();
		assertEquals("The age should be 100", 100, age);
	}

	public void testSetAttributeValue() throws Exception {
		IJmxTestBean proxy = getProxy();
		proxy.setName("Rob Harrop");
		assertEquals("The name of the bean should have been updated", "Rob Harrop", target.getName());
	}

	public void testSetReadOnlyAttribute() throws Exception {
		IJmxTestBean proxy = getProxy();
		try {
			proxy.setAge(900);
			fail("Should not be able to write to a read only attribute");
		}
		catch (InvalidInvocationException ex) {
			// success
		}
	}

	public void testInvokeNoArgs() throws Exception {
		IJmxTestBean proxy = getProxy();
		long result = proxy.myOperation();
		assertEquals("The operation should return 1", 1, result);
	}

	public void testInvokeArgs() throws Exception {
		IJmxTestBean proxy = getProxy();
		int result = proxy.add(1, 2);
		assertEquals("The operation should return 3", 3, result);
	}

	public void testInvokeUnexposedMethodWithException() throws Exception {
		IJmxTestBean bean = getProxy();
		try {
			bean.dontExposeMe();
			fail("Method dontExposeMe should throw an exception");
		}
		catch (InvalidInvocationException desired) {
			// success
		}
	}


	private static class ProxyTestAssembler extends AbstractReflectiveMBeanInfoAssembler {

		protected boolean includeReadAttribute(Method method, String beanKey) {
			return true;
		}

		protected boolean includeWriteAttribute(Method method, String beanKey) {
			if ("setAge".equals(method.getName())) {
				return false;
			}
			return true;
		}

		protected boolean includeOperation(Method method, String beanKey) {
			if ("dontExposeMe".equals(method.getName())) {
				return false;
			}
			return true;
		}

		protected String getOperationDescription(Method method) {
			return method.getName();
		}

		protected String getAttributeDescription(PropertyDescriptor propertyDescriptor) {
			return propertyDescriptor.getDisplayName();
		}

		protected void populateAttributeDescriptor(Descriptor descriptor, Method getter, Method setter) {

		}

		protected void populateOperationDescriptor(Descriptor descriptor, Method method) {

		}

		protected String getDescription(String beanKey, Class beanClass) {
			return "";
		}

		protected void populateMBeanDescriptor(Descriptor mbeanDescriptor, String beanKey, Class beanClass) {

		}
	}

}
