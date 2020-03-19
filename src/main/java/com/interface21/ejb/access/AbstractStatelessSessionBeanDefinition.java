package com.interface21.ejb.access;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.factory.support.DefaultRootBeanDefinition;
 
/** 
* Root bean definitions have a class and properties.
*  ISN:T IT A PROBLEM THAT WE CACHE EJB OBJECT, NOT HOME?
* DIRECTLY MAKES BEAn available
*/
public abstract class AbstractStatelessSessionBeanDefinition extends DefaultRootBeanDefinition {
	
	private static String PREFIX = "java:comp/env/";
	
	private String jndiName;

	/*
	 * public void setProxyInterface(String intfName) throws Exception {
		setBeanClassName(intfName);
		// Check it's an interface
		if (!getBeanClass().isInterface())
			throw new Exception("Can proxy only interfaces: " + getBeanClass() + " is a class");
	}
	*/
	
	public void setJndiName(String jndiName) throws Exception {
		if (!jndiName.startsWith(PREFIX))
			jndiName = PREFIX + jndiName;
		this.jndiName = jndiName;
	}
	
	public String getJndiName() {
		return this.jndiName;
	}
	
	/**
	 * This will be the business methods interface
	 */
	public void setBusinessInterface(String intfName) throws Exception {
		setBeanClassName(intfName);
		// Check it's an interface
		if (!getBeanClass().isInterface())
			throw new Exception("Must be an interface: " + getBeanClass() + " is a class");
	}
	
	protected abstract BeanWrapper newBeanWrapper();
	

	//public String toString() {
	//	return "TestRootBeanDefinition: classname is " + getBeanClass().getName();
	//}
	
} // class RootBeanDefinition