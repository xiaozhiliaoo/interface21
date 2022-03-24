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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Subclass of <code>AbstractReflectiveMBeanInfoAssembler</code> that allows for
 * the management interface of a bean to be defined using arbitrary interfaces.
 * Any methods or properties that are defined in those interfaces are exposed
 * as MBean operations and attributes.
 *
 * <p>By default, this class votes on the inclusion of each operation or attribute
 * based on the interfaces implemented by the bean class. However, you can supply an
 * array of interfaces via the <code>managedInterfaces</code> property that will be
 * used instead. If you have multiple beans and you wish each bean to use a different
 * set of interfaces, then you can map bean keys (that is the name used to pass the
 * bean to the <code>MBeanExporter</code>) to a list of interface names using the
 * <code>interfaceMappings</code> property.
 *
 * <p>If you specify values for both <code>interfaceMappings</code> and
 * <code>managedInterfaces</code>, Spring will attempt to find interfaces in the
 * mappings first. If no interfaces for the bean are found, it will use the
 * interfaces defined by <code>managedInterfaces</code>.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.2
 * @see #setManagedInterfaces
 * @see #setInterfaceMappings
 * @see MethodNameBasedMBeanInfoAssembler
 * @see SimpleReflectiveMBeanInfoAssembler
 * @see org.springframework.jmx.export.MBeanExporter
 */
public class InterfaceBasedMBeanInfoAssembler extends AbstractReflectiveMBeanInfoAssembler {

	/**
	 * Stores the array of interfaces to use for creating the management interface.
	 */
	private Class[] managedInterfaces;

	/**
	 * Stores the mappings of bean keys to an array of <code>Class</code>es.
	 */
	private Map interfaceMappings;


	/**
	 * Set the array of interfaces to use for creating the management info.
	 * These interfaces will be used for a bean if no entry corresponding to
	 * that bean is found in the <code>interfaceMappings</code> property.
	 * @param managedInterfaces an array of classes indicating the interfaces to use.
	 * Each entry <strong>MUST</strong> be an interface.
	 * @see #setInterfaceMappings
	 */
	public void setManagedInterfaces(Class[] managedInterfaces) {
		if (managedInterfaces != null) {
			for (int x = 0; x < managedInterfaces.length; x++) {
				if (!managedInterfaces[x].isInterface()) {
					throw new IllegalArgumentException(
							"Management interface [" + managedInterfaces[x].getName() + "] is not an interface");
				}
			}
		}
		this.managedInterfaces = managedInterfaces;
	}

	/**
	 * Set the mappings of bean keys to a comma-separated list of interface names.
	 * The property key should match the bean key and the property value should match
	 * the list of interface names. When searching for interfaces for a bean, Spring
	 * will check these mappings first.
	 * @param mappings the mappins of bean keys to interface names
	 */
	public void setInterfaceMappings(Properties mappings) {
		this.interfaceMappings = new HashMap(mappings.size());
		for (Enumeration en = mappings.keys(); en.hasMoreElements();) {
			String beanKey = (String) en.nextElement();
			String[] classNames = StringUtils.commaDelimitedListToStringArray(mappings.getProperty(beanKey));
			Class[] classes = convertToClasses(classNames, beanKey);
			this.interfaceMappings.put(beanKey, classes);
		}
	}

	private Class[] convertToClasses(String[] classNames, String beanKey) {
		Class[] classes = new Class[classNames.length];
		for (int x = 0; x < classes.length; x++) {
			try {
				Class cls = ClassUtils.forName(classNames[x].trim());
				if (!cls.isInterface()) {
					throw new IllegalArgumentException("Class [" + classNames[x] + "] mapped to bean key [" +
							beanKey + "] is not an interface");
				}
				classes[x] = cls;
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Class [" + classNames[x] + "] mapped to bean key [" +
						beanKey + "] cannot be found: " + ex.getMessage());
			}
		}
		return classes;
	}


	/**
	 * Check to see if the <code>Method</code> is declared in
	 * one of the configured interfaces and that it is public.
	 * @param method the accessor <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 * configured interfaces, otherwise <code>false</code>.
	 */
	protected boolean includeReadAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * Check to see if the <code>Method</code> is declared in
	 * one of the configured interfaces and that it is public.
	 * @param method the mutator <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 * configured interfaces, otherwise <code>false</code>.
	 */
	protected boolean includeWriteAttribute(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * Check to see if the <code>Method</code> is declared in
	 * one of the configured interfaces and that it is public.
	 * @param method the operation <code>Method</code>.
	 * @param beanKey the key associated with the MBean in the
	 * <code>beans</code> <code>Map</code>.
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 * configured interfaces, otherwise <code>false</code>.
	 */
	protected boolean includeOperation(Method method, String beanKey) {
		return isPublicInInterface(method, beanKey);
	}

	/**
	 * Check to see if the <code>Method</code> is both public and declared in
	 * one of the configured interfaces.
	 * @param method the <code>Method</code> to check.
	 * @param beanKey the key associated with the MBean in the beans map
	 * @return <code>true</code> if the <code>Method</code> is declared in one of the
	 * configured interfaces and is public, otherwise <code>false</code>.
	 */
	private boolean isPublicInInterface(Method method, String beanKey) {
		return isPublic(method) && isDeclaredInInterface(method, beanKey);
	}

	/**
	 * Checks to see if the given method is public.
	 */
	private boolean isPublic(Method method) {
		return (method.getModifiers() & Modifier.PUBLIC) > 0;
	}

	/**
	 * Checks to see if the given method is declared in a managed
	 * interface for the given bean.
	 */
	private boolean isDeclaredInInterface(Method method, String beanKey) {
		Class[] ifaces = null;

		if (this.interfaceMappings != null) {
			ifaces = (Class[]) this.interfaceMappings.get(beanKey);
		}

		if (ifaces == null) {
			ifaces = this.managedInterfaces;
			if (ifaces == null) {
				ifaces = ClassUtils.getAllInterfacesForClass(method.getDeclaringClass());
			}
		}

		if (ifaces != null) {
			for (int i = 0; i < ifaces.length; i++) {
				Method[] methods = ifaces[i].getDeclaredMethods();
				for (int j = 0; j < methods.length; j++) {
					Method ifaceMethod = methods[j];
					if (ifaceMethod.getName().equals(method.getName()) &&
							Arrays.equals(ifaceMethod.getParameterTypes(), method.getParameterTypes())) {
						return true;
					}
				}
			}
		}

		return false;
	}

}
