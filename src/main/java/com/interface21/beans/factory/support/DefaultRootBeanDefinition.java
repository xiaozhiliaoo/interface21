package com.interface21.beans.factory.support;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.List;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.BeansException;
import com.interface21.beans.PropertyValues;


/** 
* Root bean definitions have a class and properties.
*/
public class DefaultRootBeanDefinition extends AbstractRootBeanDefinition  {


	/** Creates new BeanDefinition */
	public DefaultRootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton) {
		super(clazz, pvs, singleton);
	}
	
	protected DefaultRootBeanDefinition() {
	}
	

	/**
	 * Subclasses may override this, to create bean
	 * wrappers differently or perform custom preprocessing.
	 * This implementation wraps the bean class directly.
	 * @return a new BeanWrapper wrapper the target object
	 */
	protected BeanWrapper newBeanWrapper() {
		return new BeanWrapperImpl(getBeanClass());
	}

	public String toString() {
		return "RootBeanDefinition: class is " + getBeanClass();
	}
	
} // class RootBeanDefinition