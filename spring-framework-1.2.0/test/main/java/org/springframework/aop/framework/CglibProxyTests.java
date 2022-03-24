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

import net.sf.cglib.core.CodeGenerationException;
import org.aopalliance.aop.AspectException;
import org.aopalliance.intercept.MethodInterceptor;

import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.ITestBean;
import org.springframework.beans.TestBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Additional and overridden tests for the CGLIB proxy.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 */
public class CglibProxyTests extends AbstractAopProxyTests {

	protected Object createProxy(AdvisedSupport as) {
		as.setProxyTargetClass(true);
		Object proxy = as.createAopProxy().getProxy();
		assertTrue(AopUtils.isCglibProxy(proxy));
		return proxy;
	}

	protected AopProxy createAopProxy(AdvisedSupport as) {
		as.setProxyTargetClass(true);
		return new Cglib2AopProxy(as);
	}

	protected boolean requiresTarget() {
		return true;
	}

	public void testNullConfig() {
		try {
			AopProxy aop = new Cglib2AopProxy(null);
			aop.getProxy();
			fail("Shouldn't allow null interceptors");
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}

	public void testNoTarget() {
		AdvisedSupport pc = new AdvisedSupport(new Class[]{ITestBean.class});
		pc.addAdvice(new NopInterceptor());
		try {
			AopProxy aop = createAopProxy(pc);
			aop.getProxy();
			fail("Shouldn't allow no target with CGLIB proxy");
		}
		catch (AopConfigException ex) {
			// Ok
		}
	}

	public void testProtectedMethodInvocation() throws Throwable {
		ProtectedMethodTestBean bean = new ProtectedMethodTestBean();
		mockTargetSource.setTarget(bean);

		AdvisedSupport as = new AdvisedSupport(new Class[]{});
		as.setTargetSource(mockTargetSource);
		as.addAdvice(new NopInterceptor());
		AopProxy aop = new Cglib2AopProxy(as);

		Object proxy = aop.getProxy();

		assertTrue("CGLIB proxy not generated", AopUtils.isCglibProxy(proxy));
	}

	public void testProxyCanBeClassNotInterface() throws Throwable {
		TestBean raw = new TestBean();
		raw.setAge(32);
		mockTargetSource.setTarget(raw);
		AdvisedSupport pc = new AdvisedSupport(new Class[]{});
		pc.setTargetSource(mockTargetSource);
		AopProxy aop = new Cglib2AopProxy(pc);

		Object proxy = aop.getProxy();
		assertTrue("Proxy is CGLIB enhanced", AopUtils.isCglibProxy(proxy));
		assertTrue(proxy instanceof ITestBean);
		assertTrue(proxy instanceof TestBean);
		TestBean tb = (TestBean) proxy;
		assertEquals("Correct age", 32, tb.getAge());
	}

	public void testCglibProxyingGivesMeaningfulExceptionIfAskedToProxyNonvisibleClass() {
		class YouCantSeeThis {

			void hidden() {
			}
		}
		YouCantSeeThis mine = new YouCantSeeThis();
		try {
			ProxyFactory pf = new ProxyFactory(mine);
			pf.getProxy();
			fail("Shouldn't be able to proxy non-visible class with CGLIB");
		}
		catch (AspectException ex) {
			// Check that stack trace is preserved
			// FIX: CGLIB will throw an IllegalArgumentException when trying to
			// create a proxy
			// of a class where the constructor is not visible - Rob Harrop
			assertTrue((ex.getCause() instanceof CodeGenerationException)
					|| (ex.getCause() instanceof IllegalArgumentException));

			// Check that error message is helpful

			// TODO check why these methods fail with NPE on AOP Alliance code
			//ex.printStackTrace();
			//assertTrue(ex.getMessage().indexOf("final") != -1);
			//assertTrue(ex.getMessage().indexOf("visible") != -1);
		}
	}

	public void testMethodInvocationDuringConstructor() {
		CglibTestBean bean = new CglibTestBean();
		bean.setName("Rob Harrop");

		AdvisedSupport as = new AdvisedSupport(new Class[]{});
		as.setTarget(bean);
		as.addAdvice(new NopInterceptor());
		AopProxy aop = new Cglib2AopProxy(as);

		CglibTestBean proxy = (CglibTestBean) aop.getProxy();

		assertEquals("The name property has been overwritten by the constructor",
				"Rob Harrop", proxy.getName());
	}

	public void testUnadvisedProxyCreationWithCallDuringConstructor() throws Exception {
		CglibTestBean target = new CglibTestBean();
		target.setName("Rob Harrop");

		AdvisedSupport pc = new AdvisedSupport(new Class[]{});
		pc.setFrozen(true);
		pc.setTarget(target);

		Cglib2AopProxy aop = new Cglib2AopProxy(pc);

		CglibTestBean proxy = (CglibTestBean) aop.getProxy();

		assertNotNull("Proxy should not be null", proxy);
		assertEquals("Constructor overrode the value of name", "Rob Harrop", proxy.getName());

	}

	public void testMultipleProxies() {

		TestBean target = new TestBean();
		target.setAge(20);
		TestBean target2 = new TestBean();
		target2.setAge(21);

		ITestBean proxy1 = getAdvisedProxy(target);
		ITestBean proxy2 = getAdvisedProxy(target2);
		assertTrue(proxy1.getClass() == proxy2.getClass());
		assertEquals(target.getAge(), proxy1.getAge());
		assertEquals(target2.getAge(), proxy2.getAge());
	}

	private ITestBean getAdvisedProxy(TestBean target) {
		ProxyFactory pf = new ProxyFactory(new Class[]{ITestBean.class});
		pf.setProxyTargetClass(true);

		MethodInterceptor static1 = new NopInterceptor();
		pf.addAdvice(static1);

		pf.setTarget(target);
		pf.setFrozen(true);
		pf.setExposeProxy(false);

		return (ITestBean) pf.getProxy();
	}

	public void testWithNoArgConstructor() {
		NoArgCtorTestBean target = new NoArgCtorTestBean("b", 1);
		target.reset();

		mockTargetSource.setTarget(target);
		AdvisedSupport pc = new AdvisedSupport(new Class[]{});
		pc.setTargetSource(mockTargetSource);
		Cglib2AopProxy aop = new Cglib2AopProxy(pc);
		aop.setConstructorArguments(new Object[]{"Rob Harrop", new Integer(22)},
				new Class[]{String.class, int.class});

		NoArgCtorTestBean proxy = (NoArgCtorTestBean) aop.getProxy();
		proxy = (NoArgCtorTestBean) aop.getProxy();

		assertNotNull("Proxy should be null", proxy);
	}

	public void testProxyAProxy() {
		ITestBean target = new TestBean();

		mockTargetSource.setTarget(target);
		AdvisedSupport as = new AdvisedSupport(new Class[]{});
		as.setTargetSource(mockTargetSource);
		as.addAdvice(new NopInterceptor());
		Cglib2AopProxy cglib = new Cglib2AopProxy(as);

		ITestBean proxy1 = (ITestBean) cglib.getProxy();

		mockTargetSource.setTarget(proxy1);
		as = new AdvisedSupport(new Class[]{});
		as.setTargetSource(mockTargetSource);
		as.addAdvice(new NopInterceptor());
		cglib = new Cglib2AopProxy(as);

		ITestBean proxy2 = (ITestBean) cglib.getProxy();
	}

	public void testExceptionHandling() {
		ExceptionThrower bean = new ExceptionThrower();

		mockTargetSource.setTarget(bean);

		AdvisedSupport as = new AdvisedSupport(new Class[]{});
		as.setTargetSource(mockTargetSource);
		as.addAdvice(new NopInterceptor());
		AopProxy aop = new Cglib2AopProxy(as);

		ExceptionThrower proxy = (ExceptionThrower) aop.getProxy();

		try {
			proxy.doTest();
		}
		catch (Exception ex) {
			assertTrue("Invalid exception class", ex instanceof ApplicationContextException);
		}

		assertTrue("Catch was not invoked", proxy.isCatchInvoked());
		assertTrue("Finally was not invoked", proxy.isFinallyInvoked());
	}

	public void testWithDependencyChecking() {
		ApplicationContext ctx =
				new ClassPathXmlApplicationContext("org/springframework/aop/framework/withDependencyChecking.xml");
		ctx.getBean("testBean");
	}


	public static class ExceptionThrower {

		private boolean catchInvoked;

		private boolean finallyInvoked;

		public boolean isCatchInvoked() {
			return catchInvoked;
		}

		public boolean isFinallyInvoked() {
			return finallyInvoked;
		}

		public void doTest() throws Exception {
			try {
				throw new ApplicationContextException("foo");
			}
			catch (Exception ex) {
				catchInvoked = true;
				throw ex;
			}
			finally {
				finallyInvoked = true;
			}
		}
	}

}
