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

package org.springframework.context.access;

import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocatorTests;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * @author Colin Sampaleanu
 */
public class ContextSingletonBeanFactoryLocatorTests extends SingletonBeanFactoryLocatorTests {

	public void testBaseBeanFactoryDefs() {
		// just test the base BeanFactory/AppContext defs we are going to work
		// with in other tests
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] {"/org/springframework/beans/factory/access/beans1.xml",
				              "/org/springframework/beans/factory/access/beans2.xml"});
	}

	public void testBasicFunctionality() {
		
		// just use definition file from the SingletonBeanFactoryLocator test, since it's
		// completely valid
		ContextSingletonBeanFactoryLocator facLoc = new ContextSingletonBeanFactoryLocator(
				"classpath*:" + ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));
		
		basicFunctionalityTest(facLoc);
	}
	
	// this test can run multiple times, but due to static keyed lookup of the locators,
	// 2nd and subsequent calls will actuall get back same locator instance. This is not
	// an issue really, since the contained beanfactories will still be loaded and released
	public void testGetInstance() {
		
        // try with and without 'classpath*:' prefix, and with 'classpath:' prefix
		BeanFactoryLocator facLoc = ContextSingletonBeanFactoryLocator.getInstance(
				ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));
		getInstanceTest1(facLoc);
		
		facLoc = ContextSingletonBeanFactoryLocator.getInstance(
				"classpath*:" + ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));
		getInstanceTest2(facLoc);

		// this will actually get another locator instance, as the key is the resource name
		facLoc = ContextSingletonBeanFactoryLocator.getInstance(
				"classpath:" + ClassUtils.addResourcePathToPackagePath(
				SingletonBeanFactoryLocatorTests.class, "ref1.xml"));
		getInstanceTest3(facLoc);
	}
}
