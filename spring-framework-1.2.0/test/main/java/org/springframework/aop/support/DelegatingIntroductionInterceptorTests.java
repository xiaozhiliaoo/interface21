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

package org.springframework.aop.support;

import java.io.Serializable;

import junit.framework.TestCase;
import org.aopalliance.intercept.MethodInterceptor;
import org.easymock.MockControl;

import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.IntroductionInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.framework.TimeStamped;
import org.springframework.aop.interceptor.SerializableNopInterceptor;
import org.springframework.beans.INestedTestBean;
import org.springframework.beans.ITestBean;
import org.springframework.beans.NestedTestBean;
import org.springframework.beans.Person;
import org.springframework.beans.SerializablePerson;
import org.springframework.beans.TestBean;
import org.springframework.util.SerializationTestUtils;

/**
 * @author Rod Johnson
 * @since 13.05.2003
 */
public class DelegatingIntroductionInterceptorTests extends TestCase {

	public void testNullTarget() throws Exception {
		try {
			IntroductionInterceptor ii = new DelegatingIntroductionInterceptor(null);
			fail("Shouldn't accept null target");
		}
		catch (IllegalArgumentException ex) {
			// OK
		}
	}
	
	public void testIntroductionInterceptorWithDelegation() throws Exception {
		TestBean raw = new TestBean();
		assertTrue(! (raw instanceof TimeStamped));
		ProxyFactory factory = new ProxyFactory(raw);
	
		MockControl tsControl = MockControl.createControl(TimeStamped.class);
		TimeStamped ts = (TimeStamped) tsControl.getMock();
		ts.getTimeStamp();
		long timestamp = 111L;
		tsControl.setReturnValue(timestamp, 1);
		tsControl.replay();

		factory.addAdvisor(0, new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(ts)));
		
		TimeStamped tsp = (TimeStamped) factory.getProxy();
		assertTrue(tsp.getTimeStamp() == timestamp);
	
		tsControl.verify();
	}
	
	public void testIntroductionInterceptorWithInterfaceHierarchy() throws Exception {
		TestBean raw = new TestBean();
		assertTrue(! (raw instanceof SubTimeStamped));
		ProxyFactory factory = new ProxyFactory(raw);

		MockControl tsControl = MockControl.createControl(SubTimeStamped.class);
		SubTimeStamped ts = (SubTimeStamped) tsControl.getMock();
		ts.getTimeStamp();
		long timestamp = 111L;
		tsControl.setReturnValue(timestamp, 1);
		tsControl.replay();

		factory.addAdvisor(0, new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(ts), SubTimeStamped.class));

		SubTimeStamped tsp = (SubTimeStamped) factory.getProxy();
		assertTrue(tsp.getTimeStamp() == timestamp);

		tsControl.verify();
	}

	public void testIntroductionInterceptorWithSuperInterface() throws Exception {
		TestBean raw = new TestBean();
		assertTrue(! (raw instanceof TimeStamped));
		ProxyFactory factory = new ProxyFactory(raw);

		MockControl tsControl = MockControl.createControl(SubTimeStamped.class);
		SubTimeStamped ts = (SubTimeStamped) tsControl.getMock();
		ts.getTimeStamp();
		long timestamp = 111L;
		tsControl.setReturnValue(timestamp, 1);
		tsControl.replay();

		factory.addAdvisor(0, new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(ts), TimeStamped.class));

		TimeStamped tsp = (TimeStamped) factory.getProxy();
		assertTrue(!(tsp instanceof SubTimeStamped));
		assertTrue(tsp.getTimeStamp() == timestamp);

		tsControl.verify();
	}

	public void testAutomaticInterfaceRecognitionInDelegate() throws Exception {
		final long t = 1001L;
		class Test implements TimeStamped, ITest {
			public void foo() throws Exception {
			}
			public long getTimeStamp() {
				return t;
			}
		}
		
		DelegatingIntroductionInterceptor ii = new DelegatingIntroductionInterceptor(new Test());
		
		TestBean target = new TestBean();
		
		ProxyFactory pf = new ProxyFactory(target);
		pf.addAdvisor(0, new DefaultIntroductionAdvisor(ii));
		
		//assertTrue(Arrays.binarySearch(pf.getProxiedInterfaces(), TimeStamped.class) != -1);
		TimeStamped ts = (TimeStamped) pf.getProxy();
		
		assertTrue(ts.getTimeStamp() == t);
		((ITest) ts).foo();
		
		((ITestBean) ts).getAge();
	}
	
	
	public void testAutomaticInterfaceRecognitionInSubclass() throws Exception {
		final long t = 1001L;
		class TestII extends DelegatingIntroductionInterceptor implements TimeStamped, ITest {
			public void foo() throws Exception {
			}
			public long getTimeStamp() {
				return t;
			}
		}
		
		DelegatingIntroductionInterceptor ii = new TestII();
		
		TestBean target = new TestBean();
		
		ProxyFactory pf = new ProxyFactory(target);
		IntroductionAdvisor ia = new DefaultIntroductionAdvisor(ii);
		assertTrue(ia.isPerInstance());
		pf.addAdvisor(0, ia);
		
		//assertTrue(Arrays.binarySearch(pf.getProxiedInterfaces(), TimeStamped.class) != -1);
		TimeStamped ts = (TimeStamped) pf.getProxy();
		
		assertTrue(ts instanceof TimeStamped);
		// Shoulnd't proxy framework interfaces
		assertTrue(!(ts instanceof MethodInterceptor));
		assertTrue(!(ts instanceof IntroductionInterceptor));
		
		assertTrue(ts.getTimeStamp() == t);
		((ITest) ts).foo();
		((ITestBean) ts).getAge();
		
		// Test removal
		ii.suppressInterface(TimeStamped.class);
		// Note that we need to construct a new proxy factory,
		// or suppress the interface on the proxy factory
		pf = new ProxyFactory(target);
		pf.addAdvisor(0, new DefaultIntroductionAdvisor(ii));
		Object o = pf.getProxy();
		assertTrue(!(o instanceof TimeStamped));
	}
	
	public void testIntroductionInterceptorDoesntReplaceToString() throws Exception {
		TestBean raw = new TestBean();
		assertTrue(! (raw instanceof TimeStamped));
		ProxyFactory factory = new ProxyFactory(raw);
	
		TimeStamped ts = new SerializableTimeStamped(0);

		factory.addAdvisor(0, new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(ts) {
			public String toString() {
				throw new UnsupportedOperationException("Shouldn't be invoked");
			}
		}));
		
		TimeStamped tsp = (TimeStamped) factory.getProxy();
		assertEquals(0, tsp.getTimeStamp());
	
		assertEquals(raw.toString(), tsp.toString());
	}
	
	public void testDelegateReturnsThisIsMassagedToReturnProxy() {
	    NestedTestBean target = new NestedTestBean();
	    String company = "Interface21";
	    target.setCompany(company);
	    TestBean delegate = new TestBean() {
            public ITestBean getSpouse() {
                return this;
            }
	    };
	    ProxyFactory pf = new ProxyFactory(target);
	    pf.addAdvice(new DelegatingIntroductionInterceptor(delegate));
	    INestedTestBean proxy = (INestedTestBean) pf.getProxy();
	    
	    assertEquals(company, proxy.getCompany());
	    ITestBean introduction = (ITestBean) proxy;
	    assertSame("Introduced method returning delegate returns proxy", introduction, introduction.getSpouse());
	    assertTrue("Introduced method returning delegate returns proxy", AopUtils.isAopProxy(introduction.getSpouse()));
	}
	
	public void testSerializableDelegatingIntroductionInterceptorSerializable() throws Exception {
		SerializablePerson serializableTarget = new SerializablePerson();
		String name = "Tony";
		serializableTarget.setName("Tony");
		
		ProxyFactory factory = new ProxyFactory(serializableTarget);
		factory.addInterface(Person.class);
		long time = 1000;
		TimeStamped ts = new SerializableTimeStamped(time);
	
		factory.addAdvisor(new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(ts)));
		factory.addAdvice(new SerializableNopInterceptor());
		
		Person p = (Person) factory.getProxy();
		
		assertEquals(name, p.getName());
		assertEquals(time, ((TimeStamped) p).getTimeStamp());
		
		Person p1 = (Person) SerializationTestUtils.serializeAndDeserialize(p);
		assertEquals(name, p1.getName());
		assertEquals(time, ((TimeStamped) p1).getTimeStamp());
	}
	
//	public void testDelegatingIntroductionInterceptorDoesntMakeNonserializableSerializable() throws Exception {
//		// Target is NOT serialiable
//		TestBean raw = new TestBean();
//		ProxyFactory factory = new ProxyFactory(raw);
//		factory.addInterface(Person.class);
//		long time = 1000;
//		TimeStamped ts = new SerializableTimeStamped(time);
//	
//		factory.addAdvisor(new DefaultIntroductionAdvisor(new DelegatingIntroductionInterceptor(ts)));
//		Object proxy = factory.getProxy();
//		
//		assertFalse(proxy instanceof Serializable);
//	}

	// Test when target implements the interface: should get interceptor by preference.
	public void testIntroductionMasksTargetImplementation() throws Exception {
		final long t = 1001L;
		class TestII extends DelegatingIntroductionInterceptor implements TimeStamped {
			public long getTimeStamp() {
				return t;
			}
		}
		
		DelegatingIntroductionInterceptor ii = new TestII();
	
		// != t
		TestBean target = new TargetClass(t + 1);
	
		ProxyFactory pf = new ProxyFactory(target);
		pf.addAdvisor(0, new DefaultIntroductionAdvisor(ii));
	
		TimeStamped ts = (TimeStamped) pf.getProxy();
		// From introduction interceptor, not target
		assertTrue(ts.getTimeStamp() == t);
	}


	private static class SerializableTimeStamped implements TimeStamped, Serializable {

		private final long ts;

		public SerializableTimeStamped(long ts) {
			this.ts = ts;
		}

		public long getTimeStamp() {
			return ts;
		}
	}


	public static class TargetClass extends TestBean implements TimeStamped {

		long t;

		public TargetClass(long t) {
			this.t = t;
		}

		public long getTimeStamp() {
			return t;
		}
	}


	public interface ITest {

		void foo() throws Exception;
	}


	private static interface SubTimeStamped extends TimeStamped {
	}

}
