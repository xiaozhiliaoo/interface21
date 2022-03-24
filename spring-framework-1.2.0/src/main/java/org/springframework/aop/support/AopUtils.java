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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aopalliance.aop.AspectException;

import org.springframework.aop.Advisor;
import org.springframework.aop.IntroductionAdvisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.util.ClassUtils;

/**
 * Utility methods used by the AOP framework and by AOP proxy factories.
 * Not intended to be used directly by applications.
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public abstract class AopUtils {

	/**
	 * Return whether the given object is either a J2SE dynamic
	 * proxy or a CGLIB proxy.
	 * @param object the object to check
	 * @see #isJdkDynamicProxy
	 * @see #isCglibProxy
	 */
	public static boolean isAopProxy(Object object) {
		return isJdkDynamicProxy(object) || isCglibProxy(object);
	}

	/**
	 * Return whether the given object is a J2SE dynamic proxy.
	 * @param object the object to check
	 * @see Proxy#isProxyClass
	 */
	public static boolean isJdkDynamicProxy(Object object) {
		return (object != null && Proxy.isProxyClass(object.getClass()));
	}

	/**
	 * Return whether the given object is a CGLIB proxy.
	 * @param object the object to check
	 */
	public static boolean isCglibProxy(Object object) {
		return (object != null && isCglibProxyClass(object.getClass()));
	}
    
	/**
	 * Return whether the specified class is a CGLIB-generated class.
	 * @param clazz the class to check
	 */
	public static boolean isCglibProxyClass(Class clazz) {
		return (clazz != null && clazz.getName().indexOf("$$") != -1);
	}

	/**
	 * Return whether the given method is an "equals" method.
	 * @see Object#equals
	 */
	public static boolean isEqualsMethod(Method method) {
		return (method != null && method.getName().equals("equals") &&
				method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == Object.class);
	}

	/**
	 * Return whether the given method is a "hashCode" method.
	 * @see Object#hashCode
	 */
	public static boolean isHashCodeMethod(Method method) {
		return (method != null && method.getName().equals("hashCode") &&
				method.getParameterTypes().length == 0);
	}

	/**
	 * Return whether the given method is an "toString" method.
	 * @see Object#toString
	 */
	public static boolean isToStringMethod(Method method) {
		return (method != null && method.getName().equals("toString") &&
				method.getParameterTypes().length == 0);
	}

	/**
	 * Given a method, which may come from an interface, and a targetClass
	 * used in the current AOP invocation, find the most specific method
	 * if there is one. E.g. the method may be IFoo.bar() and the target
	 * class may be DefaultFoo. In this case, the method may be
	 * DefaultFoo.bar(). This enables attributes on that method to be found.
	 * @param method method to be invoked, which may come from an interface
	 * @param targetClass target class for the curren invocation. May
	 * be null or may not even implement the method.
	 * @return the more specific method, or the original method if the
	 * targetClass doesn't specialize it or implement it or is null
	 */
	public static Method getMostSpecificMethod(Method method, Class targetClass) {
		if (method != null && targetClass != null) {
			try {
				method = targetClass.getMethod(method.getName(), method.getParameterTypes());
			}
			catch (NoSuchMethodException ex) {
				// Perhaps the target class doesn't implement this method:
				// that's fine, just use the original method
			}
		}
		return method;
	}
	
	/**
	 * Convenience method to convert a string array of interface names
	 * to a class array.
	 * @throws IllegalArgumentException if any of the classes is not an interface
	 * @throws ClassNotFoundException if any of the classes can't be loaded
	 * @return an array of interface classes
	 */
	public static Class[] toInterfaceArray(String[] interfaceNames)
	    throws IllegalArgumentException, ClassNotFoundException {

		Class interfaces[] = new Class[interfaceNames.length];
		for (int i = 0; i < interfaceNames.length; i++) {
			interfaces[i] = Class.forName(
					interfaceNames[i].trim(), true, Thread.currentThread().getContextClassLoader());
			// Check it's an interface.
			if (!interfaces[i].isInterface()) {
				throw new IllegalArgumentException(
						"Can proxy only interfaces: [" + interfaces[i].getName() + "] is a class");
			}
		}
		return interfaces;
	}

	/**
	 * Build a String that consists of the names of the interfaces
	 * in the given collection.
	 * @param interfaces collection of Class objects that represent interfaces.
	 * @return a string of form com.foo.Bar,com.foo.Baz
	 */
	public static String interfacesString(Collection interfaces) {
		StringBuffer sb = new StringBuffer();
		int i = 0;
		for (Iterator itr = interfaces.iterator(); itr.hasNext(); ) {
			Class intf = (Class) itr.next();
			if (i++ > 0) {
				sb.append(",");
			}
			sb.append(intf.getName());
		}
		return sb.toString();
	}

	/**
	 * Return all interfaces that the given object implements as array,
	 * including ones implemented by superclasses.
	 * @param object the object to analyse for interfaces
	 * @return all interfaces that the given object implements as array
	 * @deprecated in favor of <code>ClassUtils.getAllInterfaces</code>
	 * @see ClassUtils#getAllInterfaces(Object)
	 */
	public static Class[] getAllInterfaces(Object object) {
		return getAllInterfacesForClass(object.getClass());
	}

	/**
	 * Return all interfaces that the given class implements as array,
	 * including ones implemented by superclasses.
	 * @param clazz the class to analyse for interfaces
	 * @return all interfaces that the given object implements as array
	 * @deprecated in favor of <code>ClassUtils.getAllInterfacesForClass</code>
	 * @see ClassUtils#getAllInterfacesForClass(Class)
	 */
	public static Class[] getAllInterfacesForClass(Class clazz) {
		Set interfaces = ClassUtils.getAllInterfacesForClassAsSet(clazz);
		return (Class[]) interfaces.toArray(new Class[interfaces.size()]);
	}

	/**
	 * Return all interfaces that the given object implements as List,
	 * including ones implemented by superclasses.
	 * @param object the object to analyse for interfaces
	 * @return all interfaces that the given object implements as List
	 * @deprecated in favor of <code>ClassUtils.getAllInterfacesAsSet</code>
	 * @see ClassUtils#getAllInterfacesAsSet(Object)
	 */
	public static List getAllInterfacesAsList(Object object) {
		return getAllInterfacesForClassAsList(object.getClass());
	}

	/**
	 * Return all interfaces that the given class implements as List,
	 * including ones implemented by superclasses.
	 * @param clazz the class to analyse for interfaces
	 * @return all interfaces that the given object implements as List
	 * @deprecated in favor of <code>ClassUtils.getAllInterfacesForClassAsSet</code>
	 * @see ClassUtils#getAllInterfacesForClassAsSet(Class)
	 */
	public static List getAllInterfacesForClassAsList(Class clazz) {
		return new ArrayList(ClassUtils.getAllInterfacesForClassAsSet(clazz));
	}

	/**
	 * Can the given pointcut apply at all on the given class?
	 * This is an important test as it can be used to optimize
	 * out a pointcut for a class.
	 * @param pc pc static or dynamic pointcut to check
	 * @param targetClass class we're testing
	 * @return whether the pointcut can apply on any method
	 */
	public static boolean canApply(Pointcut pc, Class targetClass) {
		if (!pc.getClassFilter().matches(targetClass)) {
			return false;
		}
		
		Set classes = ClassUtils.getAllInterfacesForClassAsSet(targetClass);
		classes.add(targetClass);
		for (Iterator it = classes.iterator(); it.hasNext();) {
			Class clazz = (Class) it.next();
			Method[] methods = clazz.getMethods();
			for (int j = 0; j < methods.length; j++) {
				if (pc.getMethodMatcher().matches(methods[j], targetClass)) {
					return true;
				}
			}
		}

		return false;
	}
	
	/**
	 * Can the given advisor apply at all on the given class?
	 * This is an important test as it can be used to optimize
	 * out a advisor for a class.
	 * @param advisor the advisor to check
	 * @param targetClass class we're testing
	 * @return whether the pointcut can apply on any method
	 */
	public static boolean canApply(Advisor advisor, Class targetClass) {
		if (advisor instanceof IntroductionAdvisor) {
			return ((IntroductionAdvisor) advisor).getClassFilter().matches(targetClass);
		}
		else if (advisor instanceof PointcutAdvisor) {
			PointcutAdvisor pca = (PointcutAdvisor) advisor;
			return canApply(pca.getPointcut(), targetClass);
		}
		else {
			// It doesn't have a pointcut so we assume it applies
			return true;
		}
	}

	/**
	 * Invoke the target directly via reflection.
	 * @param target the target object
	 * @param method the method to invoke
	 * @param args the arguments for the method
	 * @throws Throwable if thrown by the target method
	 * @throws AspectException if encountering
	 * a reflection error
	 */
	public static Object invokeJoinpointUsingReflection(Object target, Method method, Object[] args)
	    throws Throwable {

		// Use reflection to invoke the method.
		try {
		 return method.invoke(target, args);
		}
		catch (InvocationTargetException ex) {
			// Invoked method threw a checked exception.
			// We must rethrow it. The client won't see the interceptor.
			throw ex.getTargetException();
		}
		catch (IllegalArgumentException ex) {
			throw new AspectException("AOP configuration seems to be invalid: tried calling " +
			    method + " on [" + target + "]: ", ex);
		}
		catch (IllegalAccessException ex) {
			throw new AspectException("Couldn't access method " + method, ex);
		}
	}

}
