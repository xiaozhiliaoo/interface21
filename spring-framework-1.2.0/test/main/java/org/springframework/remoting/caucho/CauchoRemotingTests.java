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

package org.springframework.remoting.caucho;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.burlap.client.BurlapProxyFactory;
import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.remoting.RemoteAccessException;

/**
 * @author Juergen Hoeller
 * @since 16.05.2003
 */
public class CauchoRemotingTests extends TestCase {

	public void testHessianProxyFactoryBeanWithAccessError() throws Exception {
		HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
		try {
			factory.setServiceInterface(TestBean.class);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.afterPropertiesSet();

		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();

		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testHessianProxyFactoryBeanWithAuthenticationAndAccessError() throws Exception {
		HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
		try {
			factory.setServiceInterface(TestBean.class);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.setUsername("test");
		factory.setPassword("bean");
		factory.setOverloadEnabled(true);
		factory.afterPropertiesSet();

		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();

		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testHessianProxyFactoryBeanWithCustomProxyFactory() throws Exception {
		TestHessianProxyFactory proxyFactory = new TestHessianProxyFactory();
		HessianProxyFactoryBean factory = new HessianProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.setUsername("test");
		factory.setPassword("bean");
		factory.setProxyFactory(proxyFactory);
		factory.setOverloadEnabled(true);
		factory.afterPropertiesSet();
		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();

		assertEquals(proxyFactory.user, "test");
		assertEquals(proxyFactory.password, "bean");
		assertTrue(proxyFactory.overloadEnabled);

		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testBurlapProxyFactoryBeanWithAccessError() throws Exception {
		BurlapProxyFactoryBean factory = new BurlapProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.afterPropertiesSet();

		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();

		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testBurlapProxyFactoryBeanWithAuthenticationAndAccessError() throws Exception {
		BurlapProxyFactoryBean factory = new BurlapProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.setUsername("test");
		factory.setPassword("bean");
		factory.setOverloadEnabled(true);
		factory.afterPropertiesSet();

		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();

		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}

	public void testBurlapProxyFactoryBeanWithCustomProxyFactory() throws Exception {
		TestBurlapProxyFactory proxyFactory = new TestBurlapProxyFactory();
		BurlapProxyFactoryBean factory = new BurlapProxyFactoryBean();
		factory.setServiceInterface(ITestBean.class);
		factory.setServiceUrl("http://localhosta/testbean");
		factory.setUsername("test");
		factory.setPassword("bean");
		factory.setProxyFactory(proxyFactory);
		factory.setOverloadEnabled(true);
		factory.afterPropertiesSet();

		assertTrue("Correct singleton value", factory.isSingleton());
		assertTrue(factory.getObject() instanceof ITestBean);
		ITestBean bean = (ITestBean) factory.getObject();

		assertEquals(proxyFactory.user, "test");
		assertEquals(proxyFactory.password, "bean");
		assertTrue(proxyFactory.overloadEnabled);

		try {
			bean.setName("test");
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
		}
	}


	private static class TestHessianProxyFactory extends HessianProxyFactory {

		private String user;
		private String password;
		private boolean overloadEnabled;

		public void setUser(String user) {
			this.user = user;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setOverloadEnabled(boolean overloadEnabled) {
			this.overloadEnabled = overloadEnabled;
		}
	}


	private static class TestBurlapProxyFactory extends BurlapProxyFactory {

		private String user;
		private String password;
		private boolean overloadEnabled;

		public void setUser(String user) {
			this.user = user;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public void setOverloadEnabled(boolean overloadEnabled) {
			this.overloadEnabled = overloadEnabled;
		}
	}

}
