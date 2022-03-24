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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Extension of MethodOverride that represents an arbitrary
 * override of a method by the IoC container.
 *
 * <p>Any non-final method can be overridden, irrespective of its
 * parameters and return types.
 *
 * @author Rod Johnson
 * @since 1.1
 */
public class ReplaceOverride extends MethodOverride {
	
	private final String methodReplacerBeanName;
	
	/** 
	 * List of String. Identifying signatures.
	 */
	private List typeIdentifiers = new LinkedList();

	/**
	 * Construct a new ReplaceOverride.
	 * @param methodName the name of the method to override
	 * @param methodReplacerBeanName the bean name of the MethodReplacer
	 */
	public ReplaceOverride(String methodName, String methodReplacerBeanName) {
		super(methodName);
		this.methodReplacerBeanName = methodReplacerBeanName;
	}
	
	/**
	 * Add a fragment of a class string, like "Exception"
	 * or "java.lang.Exc", to identify a parameter type
	 * @param s substring of class FQN
	 */
	public void addTypeIdentifier(String s) {
		this.typeIdentifiers.add(s);
	}
	
	public boolean matches(Method method) {
		// TODO could cache result for efficiency
		if (!method.getName().equals(getMethodName())) {
			// it can't match
			return false;
		}
		
		if (!isOverloaded()) {
			// No overloaded: don't worry about arg type matching.
			return true;
		}
		
		// If we get to here, we need to insist on precise argument matching.
		if (this.typeIdentifiers.size() != method.getParameterTypes().length) {
			return false;
		}
		for (int i = 0; i < this.typeIdentifiers.size(); i++) {
			String identifier = (String) this.typeIdentifiers.get(i);
			if (method.getParameterTypes()[i].getName().indexOf(identifier) == -1) {
				// This parameter cannot match.
				return false;
			}
		}
		return true;			
	}
	
	/**
	 * Return the name of the bean implementing MethodReplacer.
	 */
	public String getMethodReplacerBeanName() {
		return methodReplacerBeanName;
	}

	public String toString() {
		return "Replace override for method '" + getMethodName() + "; will call bean '" +
				this.methodReplacerBeanName + "'";
	}

}
