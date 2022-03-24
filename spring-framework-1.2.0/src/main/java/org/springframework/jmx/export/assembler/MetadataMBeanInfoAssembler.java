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

package org.springframework.jmx.export.assembler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import javax.management.Descriptor;
import javax.management.MBeanParameterInfo;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.metadata.InvalidMetadataException;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.export.metadata.ManagedAttribute;
import org.springframework.jmx.export.metadata.ManagedOperation;
import org.springframework.jmx.export.metadata.ManagedOperationParameter;
import org.springframework.jmx.export.metadata.ManagedResource;
import org.springframework.util.StringUtils;

/**
 * Implementation of <code>MBeanInfoAssembler</code> that reads the
 * management interface information from source level metadata.
 *
 * <p>Uses the <code>JmxAttributeSource</code> strategy interface, so that
 * metadata can be read using any supported implementation. Out of the box,
 * two strategies are included:
 * <ul>
 * <li><code>AttributesJmxAttributeSource</code>, for Commons Attributes
 * <li><code>AnnotationJmxAttributeSource</code>, for JDK 1.5+ annotations
 * </ul>
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setAttributeSource
 * @see org.springframework.jmx.export.metadata.AttributesJmxAttributeSource
 * @see org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource
 */
public class MetadataMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler
		implements AutodetectCapableMBeanInfoAssembler, InitializingBean {

	private JmxAttributeSource attributeSource;


	/**
	 * Set the <code>JmxAttributeSource</code> implementation to use for
	 * reading the metadata from the bean class.
	 * @see org.springframework.jmx.export.metadata.AttributesJmxAttributeSource
	 * @see org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource
	 */
	public void setAttributeSource(JmxAttributeSource attributeSource) {
		this.attributeSource = attributeSource;
	}

	public void afterPropertiesSet() {
		if (this.attributeSource == null) {
			throw new IllegalArgumentException("'attributeSource' is required");
		}
	}


	/**
	 * Throws an IllegalArgumentException if it encounters a JDK dynamic proxy.
	 * Metadata can only be read from target classes and CGLIB proxies!
	 */
	protected void checkManagedBean(Object managedBean) throws IllegalArgumentException {
		if (AopUtils.isJdkDynamicProxy(managedBean)) {
			throw new IllegalArgumentException(
					"MetadataMBeanInfoAssembler does not support JDK dynamic proxies - " +
					"export the target beans directly or use CGLIB proxies instead");
		}
	}

	/**
	 * Used for autodetection of beans. Checks to see if the bean's class has a
	 * <code>ManagedResource</code> attribute. If so it will add it list of included beans.
	 * @param beanClass the class of the bean
	 * @param beanName the name of the bean in the bean factory
	 */
	public boolean includeBean(Class beanClass, String beanName) {
		return (this.attributeSource.getManagedResource(getClassToExpose(beanClass)) != null);
	}

	/**
	 * Vote on the inclusion of an attribute accessor.
	 * @param method the accessor method
	 * @param beanKey the key associated with the MBean in the beans map
	 * @return whether the method has the appropriate metadata
	 */
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method);
	}

	/**
	 * Votes on the inclusion of an attribute mutator.
	 * @param method the mutator method
	 * @param beanKey the key associated with the MBean in the beans map
	 * @return whether the method has the appropriate metadata
	 */
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return hasManagedAttribute(method);
	}

	/**
	 * Votes on the inclusion of an operation.
	 * @param method the operation method
	 * @param beanKey the key associated with the MBean in the beans map
	 * @return whether the method has the appropriate metadata
	 */
	protected boolean includeOperation(Method method, String beanKey) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			return hasManagedAttribute(method);
		}
		else {
			return hasManagedOperation(method);
		}
	}

	/**
	 * Checks to see if the given Method has the <code>ManagedAttribute</code> attribute.
	 */
	private boolean hasManagedAttribute(Method method) {
		return (this.attributeSource.getManagedAttribute(method) != null);
	}

	/**
	 * Checks to see if the given Method has the <code>ManagedOperation</code> attribute.
	 * @param method the method to check
	 */
	private boolean hasManagedOperation(Method method) {
		return (this.attributeSource.getManagedOperation(method) != null);
	}


	/**
	 * Reads managed resource description from the source level metadata.
	 * Returns an empty <code>String</code> if no description can be found.
	 */
	protected String getDescription(Object managedBean, String beanKey) {
		ManagedResource mr = this.attributeSource.getManagedResource(getClassToExpose(managedBean));
		return (mr != null ? mr.getDescription() : "");
	}

	/**
	 * Creates a description for the attribute corresponding to this property
	 * descriptor. Attempts to create the description using metadata from either
	 * the getter or setter attributes, otherwise uses the property name.
	 */
	protected String getAttributeDescription(PropertyDescriptor propertyDescriptor, String beanKey) {
		Method readMethod = propertyDescriptor.getReadMethod();
		Method writeMethod = propertyDescriptor.getWriteMethod();

		ManagedAttribute getter =
				(readMethod != null) ? this.attributeSource.getManagedAttribute(readMethod) : null;
		ManagedAttribute setter =
				(writeMethod != null) ? this.attributeSource.getManagedAttribute(writeMethod) : null;

		if (getter != null && StringUtils.hasText(getter.getDescription())) {
			return getter.getDescription();
		}
		else if (setter != null && StringUtils.hasText(setter.getDescription())) {
			return setter.getDescription();
		}
		return propertyDescriptor.getDisplayName();
	}

	/**
	 * Retrieves the description for the supplied <code>Method</code> from the
	 * metadata. Uses the method name is no description is present in the metadata.
	 */
	protected String getOperationDescription(Method method, String beanKey) {
		PropertyDescriptor pd = BeanUtils.findPropertyForMethod(method);
		if (pd != null) {
			ManagedAttribute ma = this.attributeSource.getManagedAttribute(method);
			if (ma != null && StringUtils.hasText(ma.getDescription())) {
				return ma.getDescription();
			}
			return method.getName();
		}
		else {
			ManagedOperation mo = this.attributeSource.getManagedOperation(method);
			if (mo != null && StringUtils.hasText(mo.getDescription())) {
				return mo.getDescription();
			}
			return method.getName();
		}
	}

	/**
	 * Reads <code>MBeanParameterInfo</code> from the <code>ManagedOperationParameter</code>
	 * attributes attached to a method. Returns an empty array of <code>MBeanParameterInfo</code>
	 * if no attributes are found.
	 */
	protected MBeanParameterInfo[] getOperationParameters(Method method, String beanKey) {
		ManagedOperationParameter[] params = this.attributeSource.getManagedOperationParameters(method);
		if (params == null || params.length == 0) {
			return new MBeanParameterInfo[0];
		}

		MBeanParameterInfo[] parameterInfo = new MBeanParameterInfo[params.length];
		Class[] methodParameters = method.getParameterTypes();

		for (int i = 0; i < params.length; i++) {
			ManagedOperationParameter param = params[i];
			parameterInfo[i] =
					new MBeanParameterInfo(param.getName(), methodParameters[i].getName(), param.getDescription());
		}

		return parameterInfo;
	}


	/**
	 * Adds descriptor fields from the <code>ManagedResource</code> attribute
	 * to the MBean descriptor. Specifically, adds the <code>currencyTimeLimit</code>,
	 * <code>persistPolicy</code>, <code>persistPeriod</code>, <code>persistLocation</code>
	 * and <code>persistName</code> descriptor fields if they are present in the metadata.
	 */
	protected void populateMBeanDescriptor(Descriptor desc, Object managedBean, String beanKey) {
		ManagedResource mr = this.attributeSource.getManagedResource(getClassToExpose(managedBean));
		if (mr == null) {
			throw new InvalidMetadataException(
					"No ManagedResource attribute found for class: " + getClassToExpose(managedBean));
		}

		applyCurrencyTimeLimit(desc, mr.getCurrencyTimeLimit());

		// Do not use Boolean.toString(boolean) here, to preserve JDK 1.3 compatibility!
		desc.setField(FIELD_LOG, mr.isLog() ? "true" : "false");
		if (mr.getLogFile() != null) {
			desc.setField(FIELD_LOG_FILE, mr.getLogFile());
		}

		desc.setField(FIELD_PERSIST_POLICY, mr.getPersistPolicy());
		desc.setField(FIELD_PERSIST_PERIOD, Integer.toString(mr.getPersistPeriod()));
		desc.setField(FIELD_PERSIST_NAME, mr.getPersistName());
		desc.setField(FIELD_PERSIST_LOCATION, mr.getPersistLocation());
	}

	/**
	 * Adds descriptor fields from the <code>ManagedAttribute</code> attribute
	 * to the attribute descriptor. Specifically, adds the <code>currencyTimeLimit</code>,
	 * <code>default</code>, <code>persistPolicy</code> and <code>persistPeriod</code>
	 * descriptor fields if they are present in the metadata.
	 */
	protected void populateAttributeDescriptor(Descriptor desc, Method getter, Method setter, String beanKey) {
		ManagedAttribute gma =
				(getter == null) ? ManagedAttribute.EMPTY : this.attributeSource.getManagedAttribute(getter);
		ManagedAttribute sma =
				(setter == null) ? ManagedAttribute.EMPTY : this.attributeSource.getManagedAttribute(setter);

		applyCurrencyTimeLimit(desc,
				resolveIntDescriptor(gma.getCurrencyTimeLimit(), sma.getCurrencyTimeLimit()));

		Object defaultValue = resolveObjectDescriptor(gma.getDefaultValue(), sma.getDefaultValue());
		desc.setField(FIELD_DEFAULT, defaultValue);

		String persistPolicy = resolveStringDescriptor(
				gma.getPersistPolicy(), sma.getPersistPolicy(), PERSIST_POLICY_NEVER);
		desc.setField(FIELD_PERSIST_POLICY, persistPolicy);

		int persistPeriod = resolveIntDescriptor(gma.getPersistPeriod(), sma.getPersistPeriod());
		desc.setField(FIELD_PERSIST_PERIOD, Integer.toString(persistPeriod));
	}

	/**
	 * Adds descriptor fields from the <code>ManagedAttribute</code> attribute
	 * to the attribute descriptor. Specifically, adds the <code>currencyTimeLimit</code>
	 * descriptor field if it is present in the metadata.
	 */
	protected void populateOperationDescriptor(Descriptor desc, Method method, String beanKey) {
		ManagedOperation mo = this.attributeSource.getManagedOperation(method);
		if (mo != null) {
			applyCurrencyTimeLimit(desc, mo.getCurrencyTimeLimit());
		}
	}

	/**
	 * Determines which of two <code>int</code> values should be used as the value
	 * for an attribute descriptor. In general, only the getter or the setter will
	 * be have a non-zero value so we use that value. In the event that both values
	 * are non-zero we use the greater of the two. This method can be used to resolve
	 * any <code>int</code> valued descriptor where there are two possible values.
	 * @param getter the int value associated with the getter for this attribute.
	 * @param setter the int associated with the setter for this attribute.
	 */
	private int resolveIntDescriptor(int getter, int setter) {
		if (getter == 0 && setter != 0) {
			return setter;
		}
		else if (setter == 0 && getter != 0) {
			return getter;
		}
		return (getter >= setter) ? getter : setter;
	}

	/**
	 * Locates the value of a descriptor based on values attached
	 * to both the getter and setter methods. If both have values
	 * supplied then the value attached to the getter is preferred.
	 * @param getter the Object value associated with the get method.
	 * @param setter the Object value associated with the set method.
	 * @return the appropriate Object to use as the value for the descriptor
	 */
	private Object resolveObjectDescriptor(Object getter, Object setter) {
		if (getter != null) {
			return getter;
		}
		if (setter != null) {
			return setter;
		}
		return null;
	}

	/**
	 * Locates the value of a descriptor based on values attached
	 * to both the getter and setter methods. If both have values
	 * supplied then the value attached to the getter is preferred.
	 * The supplied default value is used to check to see if the value
	 * associated with the getter has changed from the default.
	 * @param getter the String value associated with the get method
	 * @param setter the String value associated with the set method
	 * @param defaultValue the String value default associated with this descriptor
	 * @return the appropriate String to use as the value for the descriptor
	 */
	private String resolveStringDescriptor(String getter, String setter, String defaultValue) {
		if (getter != null && !defaultValue.equals(getter)) {
			return getter;
		}
		if (setter != null) {
			return setter;
		}
		return null;
	}

}
