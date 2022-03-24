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

package org.springframework.aop.framework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;

/**
 * 
 * @author Rod Johnson
 */
public class AopProxyUtilsTests extends TestCase {
	
	public void testCompleteProxiedInterfacesWorksWithNull() {
		AdvisedSupport as = new AdvisedSupport();
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(1, completedInterfaces.length);
		assertEquals(Advised.class, completedInterfaces[0]);
	}
	
	public void testCompleteProxiedInterfacesWorksWithNullOpaque() {
		AdvisedSupport as = new AdvisedSupport();
		as.setOpaque(true);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(0, completedInterfaces.length);
	}
	
	public void testCompleteProxiedInterfacesAdvisedNotIncluded() {
		AdvisedSupport as = new AdvisedSupport();
		as.addInterface(ITestBean.class);
		as.addInterface(Comparable.class);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(3, completedInterfaces.length);
		
		
		// Can't assume ordering for others, so use a list
		List l = Arrays.asList(completedInterfaces);
		assertTrue(l.contains(Advised.class));
		assertTrue(l.contains(ITestBean.class));
		assertTrue(l.contains(Comparable.class));
	}
	
	public void testCompleteProxiedInterfacesAdvisedIncluded() {
		AdvisedSupport as = new AdvisedSupport();
		as.addInterface(ITestBean.class);
		as.addInterface(Comparable.class);
		as.addInterface(Advised.class);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(3, completedInterfaces.length);
		
		// Can't assume ordering for others, so use a list
		List l = Arrays.asList(completedInterfaces);
		assertTrue(l.contains(Advised.class));
		assertTrue(l.contains(ITestBean.class));
		assertTrue(l.contains(Comparable.class));
	}
	
	public void testCompleteProxiedInterfacesAdvisedNotIncludedOpaque() {
		AdvisedSupport as = new AdvisedSupport();
		as.setOpaque(true);
		as.addInterface(ITestBean.class);
		as.addInterface(Comparable.class);
		Class[] completedInterfaces = AopProxyUtils.completeProxiedInterfaces(as);
		assertEquals(2, completedInterfaces.length);
		
		// Can't assume ordering for others, so use a list
		List l = Arrays.asList(completedInterfaces);
		assertFalse(l.contains(Advised.class));
		assertTrue(l.contains(ITestBean.class));
		assertTrue(l.contains(Comparable.class));
	}

	public void testProxiedUserInterfacesWithSingleInterface() {
		ProxyFactory pf = new ProxyFactory();
		pf.setTarget(new TestBean());
		pf.addInterface(ITestBean.class);
		Object proxy = pf.getProxy();
		Class[] userInterfaces = AopProxyUtils.proxiedUserInterfaces(proxy);
		assertEquals(1, userInterfaces.length);
		assertEquals(ITestBean.class, userInterfaces[0]);
	}

	public void testProxiedUserInterfacesWithMultipleInterfaces() {
		ProxyFactory pf = new ProxyFactory();
		pf.setTarget(new TestBean());
		pf.addInterface(ITestBean.class);
		pf.addInterface(Comparable.class);
		Object proxy = pf.getProxy();
		Class[] userInterfaces = AopProxyUtils.proxiedUserInterfaces(proxy);
		assertEquals(2, userInterfaces.length);
		assertEquals(ITestBean.class, userInterfaces[0]);
		assertEquals(Comparable.class, userInterfaces[1]);
	}

	public void testProxiedUserInterfacesWithNoInterface() {
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[0],
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return null;
					}
				});
		try {
			Class[] userInterfaces = AopProxyUtils.proxiedUserInterfaces(proxy);
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

}