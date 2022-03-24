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

package org.springframework.context.support;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;

/**
 * ApplicationContext that allows concrete registration of beans and
 * messages in code, rather than from external configuration sources.
 * Mainly useful for testing.
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #registerSingleton
 * @see #registerPrototype
 * @see #registerBeanDefinition
 * @see #refresh
 */
public class StaticApplicationContext extends GenericApplicationContext {

	private final StaticMessageSource messageSource;

	/**
	 * Create a new StaticApplicationContext.
	 * @see #registerSingleton
	 * @see #registerPrototype
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public StaticApplicationContext() throws BeansException {
		this(null);
	}

	/**
	 * Create a new StaticApplicationContext with the given parent.
	 * @see #registerSingleton
	 * @see #registerPrototype
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public StaticApplicationContext(ApplicationContext parent) throws BeansException {
		super(parent);

		// initialize and register StaticMessageSource
		this.messageSource = new StaticMessageSource();
		getBeanFactory().registerSingleton(MESSAGE_SOURCE_BEAN_NAME, this.messageSource);
	}

	/**
	 * Return the internal StaticMessageSource used by this context.
	 * Can be used to register messages on it.
	 * @see #addMessage
	 */
	public StaticMessageSource getStaticMessageSource() {
		return messageSource;
	}

	/**
	 * Register a singleton bean with the underlying bean factory.
	 * <p>For more advanced needs, register with the underlying BeanFactory directly.
	 * @see #getDefaultListableBeanFactory
	 */
	public void registerSingleton(String name, Class clazz) throws BeansException {
		getDefaultListableBeanFactory().registerBeanDefinition(name, new RootBeanDefinition(clazz));
	}

	/**
	 * Register a singleton bean with the underlying bean factory.
	 * <p>For more advanced needs, register with the underlying BeanFactory directly.
	 * @see #getDefaultListableBeanFactory
	 */
	public void registerSingleton(String name, Class clazz, MutablePropertyValues pvs) throws BeansException {
		getDefaultListableBeanFactory().registerBeanDefinition(name, new RootBeanDefinition(clazz, pvs));
	}

	/**
	 * Register a prototype bean with the underlying bean factory.
	 * <p>For more advanced needs, register with the underlying BeanFactory directly.
	 * @see #getDefaultListableBeanFactory
	 */
	public void registerPrototype(String name, Class clazz) throws BeansException {
		getDefaultListableBeanFactory().registerBeanDefinition(name, new RootBeanDefinition(clazz, false));
	}

	/**
	 * Register a prototype bean with the underlying bean factory.
	 * <p>For more advanced needs, register with the underlying BeanFactory directly.
	 * @see #getDefaultListableBeanFactory
	 */
	public void registerPrototype(String name, Class clazz, MutablePropertyValues pvs) throws BeansException {
		getDefaultListableBeanFactory().registerBeanDefinition(name, new RootBeanDefinition(clazz, pvs, false));
	}

	/**
	 * Associate the given message with the given code.
	 * @param code lookup code
	 * @param locale locale message should be found within
	 * @param defaultMessage message associated with this lookup code
	 * @see #getStaticMessageSource
	 */
	public void addMessage(String code, Locale locale, String defaultMessage) {
		getStaticMessageSource().addMessage(code, locale, defaultMessage);
	}

}
