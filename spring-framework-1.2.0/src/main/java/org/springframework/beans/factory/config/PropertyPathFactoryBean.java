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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;

/**
 * FactoryBean that evaluates a property path on a given target object.
 * The target object can be specified directly or via a bean name.
 *
 * <p>Usage examples:
 *
 * <pre>
 * // target bean to be referenced by name
 * &lt;bean id="tb" class="org.springframework.beans.TestBean" singleton="false"&gt;
 *   &lt;property name="age"&gt;&lt;value&gt;10&lt;/value&gt;&lt;/property&gt;
 *   &lt;property name="spouse"&gt;
 *     &lt;bean class="org.springframework.beans.TestBean"&gt;
 *       &lt;property name="age"&gt;&lt;value&gt;11&lt;/value&gt;&lt;/property&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 * &lt;/bean&gt;
 *
 * // will result in 12, which is the value of property 'age' of the inner bean
 * &lt;bean id="propertyPath1" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"&gt;
 *   &lt;property name="targetObject"&gt;
 *     &lt;bean class="org.springframework.beans.TestBean"&gt;
 *       &lt;property name="age"&gt;&lt;value&gt;12&lt;/value&gt;&lt;/property&gt;
 *     &lt;/bean&gt;
 *   &lt;/property&gt;
 *   &lt;property name="propertyPath"&gt;&lt;value&gt;age&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 *
 * // will result in 11, which is the value of property 'spouse.age' of bean 'tb'
 * &lt;bean id="propertyPath2" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"&gt;
 *   &lt;property name="targetBeanName"&gt;&lt;value&gt;tb&lt;/value&gt;&lt;/property&gt;
 *   &lt;property name="propertyPath"&gt;&lt;value&gt;spouse.age&lt;/value&gt;&lt;/property&gt;
 * &lt;/bean&gt;
 *
 * // will result in 10, which is the value of property 'age' of bean 'tb'
 * &lt;bean id="tb.age" class="org.springframework.beans.factory.config.PropertyPathFactoryBean"/&gt;</pre>
 *
 * Thanks to Matthias Ernst for the suggestion and initial prototype!
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see #setTargetObject
 * @see #setTargetBeanName
 * @see #setPropertyPath
 */
public class PropertyPathFactoryBean implements FactoryBean, BeanNameAware, BeanFactoryAware {

	private BeanWrapper targetBeanWrapper;

	private String targetBeanName;

	private String propertyPath;

	private Class resultType;

	private String beanName;

	private BeanFactory beanFactory;


	/**
	 * Specify a target object to apply the property path to.
	 * Alternatively, specify a target bean name.
	 * @param targetObject a target object, for example a bean reference
	 * or an inner bean
	 * @see #setTargetBeanName
	 */
	public void setTargetObject(Object targetObject) {
		this.targetBeanWrapper = new BeanWrapperImpl(targetObject);
	}

	/**
	 * Specify the name of a target bean to apply the property path to.
	 * Alternatively, specify a target object directly.
	 * @param targetBeanName the bean name to be looked up in the
	 * containing bean factory (e.g. "testBean")
	 * @see #setTargetObject
	 */
	public void setTargetBeanName(String targetBeanName) {
		this.targetBeanName = targetBeanName;
	}

	/**
	 * Specify the property path to apply to the target.
	 * @param propertyPath the property path, potentially nested
	 * (e.g. "age" or "spouse.age")
	 */
	public void setPropertyPath(String propertyPath) {
		this.propertyPath = propertyPath;
	}

	/**
	 * Specify the type of the result from evaluating the property path.
	 * <p>Note: This is not necessary for directly specified target objects
	 * or singleton target beans, where the type can be determined through
	 * introspection. Just specify this in case of a prototype target,
	 * provided that you need matching by type (for example, for autowiring).
	 * @param resultType the result type, for example "java.lang.Integer"
	 */
	public void setResultType(Class resultType) {
		this.resultType = resultType;
	}

	/**
	 * The bean name of this PropertyPathFactoryBean will be interpreted
	 * as "beanName.property" pattern, if neither "targetObject" nor
	 * "targetBeanName" nor "propertyPath" have been specified.
	 * This allows for concise bean definitions with just an id/name.
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}


	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;

		if (this.targetBeanWrapper != null && this.targetBeanName != null) {
			throw new IllegalArgumentException("Specify either targetObject or targetBeanName, not both");
		}

		if (this.targetBeanWrapper == null && this.targetBeanName == null) {
			if (this.propertyPath != null) {
				throw new IllegalArgumentException(
				    "Specify targetObject or targetBeanName in combination with propertyPath");
			}

			// No other properties specified: check bean name.
			int dotIndex = this.beanName.indexOf('.');
			if (dotIndex == -1) {
				throw new IllegalArgumentException(
				    "Neither targetObject nor targetBeanName specified, and PropertyPathFactoryBean " +
				    "bean name '" + this.beanName + "' does not follow 'beanName.property' syntax");
			}
			this.targetBeanName = this.beanName.substring(0, dotIndex);
			this.propertyPath = this.beanName.substring(dotIndex + 1);
		}

		else if (this.propertyPath == null) {
			// either targetObject or targetBeanName specified
			throw new IllegalArgumentException("propertyPath is required");
		}

		if (this.targetBeanWrapper == null && this.beanFactory.isSingleton(this.targetBeanName)) {
			// Eagerly fetch singleton target bean, and determine result type.
			this.targetBeanWrapper = new BeanWrapperImpl(this.beanFactory.getBean(this.targetBeanName));
			this.resultType = this.targetBeanWrapper.getPropertyType(this.propertyPath);
		}
	}

	public Object getObject() throws BeansException {
		BeanWrapper target = this.targetBeanWrapper;
		if (target == null) {
			// fetch prototype target bean
			target = new BeanWrapperImpl(this.beanFactory.getBean(this.targetBeanName));
		}

		Object value = target.getPropertyValue(this.propertyPath);
		if (value == null) {
			throw new FatalBeanException("PropertyPathFactoryBean is not allowed to return null, " +
			    "but property value for path '" + this.propertyPath + "' is null");
		}
		return value;
	}

	public Class getObjectType() {
		return this.resultType;
	}

	/**
	 * While this FactoryBean will often be used for singleton targets,
	 * the invoked getters for the property path might return a new object
	 * for each call, so we have to assume that we're not returning the
	 * same object for each getObject() call.
	 */
	public boolean isSingleton() {
		return false;
	}

}
