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

package org.springframework.beans.factory.support;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.ConstructorArgumentValues;

/** 
 * Root bean definitions are the most common type of bean definition.
 * They do not derive from a parent bean definition, and usually have a
 * class plus optionally constructor argument values and property values.
 *
 * <p>Note that root bean definitions do not have to specify a bean class:
 * This can be useful for deriving childs from such definitions, each with
 * its own bean class but inheriting common property values and other settings.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see org.springframework.beans.factory.config.AutowireCapableBeanFactory
 */
public class RootBeanDefinition extends AbstractBeanDefinition {

	/**
	 * Create a new RootBeanDefinition for a singleton.
	 * @param beanClass the class of the bean to instantiate
	 */
	public RootBeanDefinition(Class beanClass) {
		super();
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status.
	 * @param beanClass the class of the bean to instantiate
	 * @param singleton the singleton status of the bean
	 */
	public RootBeanDefinition(Class beanClass, boolean singleton) {
		super();
		setBeanClass(beanClass);
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 */
	public RootBeanDefinition(Class beanClass, int autowireMode) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * using the given autowire mode.
	 * @param beanClass the class of the bean to instantiate
	 * @param autowireMode by name or type, using the constants in this interface
	 * @param dependencyCheck whether to perform a dependency check for objects
	 * (not applicable to autowiring a constructor, thus ignored there)
	 */
	public RootBeanDefinition(Class beanClass, int autowireMode, boolean dependencyCheck) {
		super();
		setBeanClass(beanClass);
		setAutowireMode(autowireMode);
		if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
			setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
		}
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs) {
		super(null, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition with the given singleton status,
	 * providing property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param pvs the property values to apply
	 * @param singleton the singleton status of the bean
	 */
	public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs, boolean singleton) {
		super(null, pvs);
		setBeanClass(beanClass);
		setSingleton(singleton);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * @param beanClass the class of the bean to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(Class beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClass(beanClass);
	}

	/**
	 * Create a new RootBeanDefinition for a singleton,
	 * providing constructor arguments and property values.
	 * <p>Takes a bean class name to avoid eager loading of the bean class.
	 * @param beanClassName the name of the class to instantiate
	 * @param cargs the constructor argument values to apply
	 * @param pvs the property values to apply
	 */
	public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
		super(cargs, pvs);
		setBeanClassName(beanClassName);
	}

	/**
	 * Create a new RootBeanDefinition as deep copy of the given
	 * bean definition.
	 * @param original the original bean definition to copy from
	 */
	public RootBeanDefinition(RootBeanDefinition original) {
		super(original);
	}


	public void validate() throws BeanDefinitionValidationException {
		super.validate();
				
		if (hasBeanClass()) {
			if (FactoryBean.class.isAssignableFrom(getBeanClass()) && !isSingleton()) {
				throw new BeanDefinitionValidationException(
				    "FactoryBean must be defined as singleton - " +
				    "FactoryBeans themselves are not allowed to be prototypes");
			}
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("Root bean with class [");
		sb.append(getBeanClassName()).append(']');
		if (getResourceDescription() != null) {
			sb.append(" defined in ").append(getResourceDescription());
		}
		return sb.toString();
	}

}
