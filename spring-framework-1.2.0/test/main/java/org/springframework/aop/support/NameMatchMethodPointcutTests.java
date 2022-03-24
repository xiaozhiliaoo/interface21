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

import junit.framework.TestCase;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.aop.interceptor.SerializableNopInterceptor;
import org.springframework.beans.Person;
import org.springframework.beans.SerializablePerson;
import org.springframework.util.SerializationTestUtils;

/**
 * 
 * @author Rod Johnson
 */
public class NameMatchMethodPointcutTests extends TestCase {
	
	protected NameMatchMethodPointcut pc;
	
	protected Person proxied;
	
	protected SerializableNopInterceptor nop;
	
	public NameMatchMethodPointcutTests(String s) {
		super(s);
	}
	
	/**
	 * Create an empty pointcut, populating instance variables.
	 * @see TestCase#setUp()
	 */
	protected void setUp() {
		ProxyFactory pf = new ProxyFactory(new SerializablePerson());
		nop = new SerializableNopInterceptor();
		pc = new NameMatchMethodPointcut();
		pf.addAdvisor(new DefaultPointcutAdvisor(pc, nop));
		proxied = (Person) pf.getProxy();
	}
	
	public void testMatchingOnly() {
		// Can't do exact matching through isMatch
		assertTrue(pc.isMatch("echo", "ech*"));
		assertTrue(pc.isMatch("setName", "setN*"));
		assertTrue(pc.isMatch("setName", "set*"));
		assertFalse(pc.isMatch("getName", "set*"));
		assertFalse(pc.isMatch("setName", "set"));
		assertTrue(pc.isMatch("testing", "*ing"));
	}
		
	public void testEmpty() throws Throwable {
		assertEquals(0, nop.getCount());
		proxied.getName();
		proxied.setName("");
		proxied.echo(null);
		assertEquals(0, nop.getCount());
	}
	
	
	public void testMatchOneMethod() throws Throwable {
		pc.addMethodName("echo");
		pc.addMethodName("set*");
		assertEquals(0, nop.getCount());
		proxied.getName();
		proxied.getName();
		assertEquals(0, nop.getCount());
		proxied.echo(null);
		assertEquals(1, nop.getCount());
		
		proxied.setName("");
		assertEquals(2, nop.getCount());
		proxied.setAge(25);
		assertEquals(25, proxied.getAge());
		assertEquals(3, nop.getCount());
	}

	public void testSets() throws Throwable {
		pc.setMappedNames(new String[] { "set*", "echo" });
		assertEquals(0, nop.getCount());
		proxied.getName();
		proxied.setName("");
		assertEquals(1, nop.getCount());
		proxied.echo(null);
		assertEquals(2, nop.getCount());
	}
	
	public void testSerializable() throws Throwable {
		testSets();
		// Count is now 2
		Person p2 = (Person) SerializationTestUtils.serializeAndDeserialize(proxied);
		NopInterceptor nop2 = (NopInterceptor) ((Advised) p2).getAdvisors()[0].getAdvice();
		p2.getName();
		assertEquals(2, nop2.getCount());
		p2.echo(null);
		assertEquals(3, nop2.getCount());
	}
}
