package com.interface21.beans.factory.support;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.List;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.BeansException;
import com.interface21.beans.PropertyValues;
import com.interface21.beans.factory.support.AbstractBeanDefinition.ListenerRegistration;

/** 
* Root bean definitions have a class and properties.
* @author Rod Johnson
*/
public abstract class AbstractRootBeanDefinition extends AbstractBeanDefinition implements RootBeanDefinition {

	/**
	 * Class of the wrapped object
	 */
	private Class clazz;

	/** Creates new AbstractRootBeanDefinition */
	public AbstractRootBeanDefinition(Class clazz, PropertyValues pvs, boolean singleton) {
		super(pvs, singleton);
		this.clazz = clazz;
	}
	
	protected AbstractRootBeanDefinition() {
	}
	
	protected void setBeanClass(Class clazz) {
		this.clazz = clazz;
	}
	
	public void setBeanClassName(String classname) throws ClassNotFoundException {
		this.clazz = Class.forName(classname);
	}

	// Omitted to make subclasses instantiate their own classes to avoid class loading problems
	//public RootBeanDefinition(String className, PropertyValues pvs) throws ReflectionException {
	//     super(pvs);
	//    this.clazz = clc.getClass(className);
	// }

	/**
	 * @return the class of the wrapped bean
	 */
	public final Class getBeanClass() {
		return this.clazz;
	}

	/**
	 * Subclasses must implement this, to create bean
	 * wrappers differently or perform custom preprocessing
	 * @return a BeanWrapper for the wrapped bean
	 */
	protected abstract BeanWrapper newBeanWrapper();


	/**
	 * Given a bean wrapper, add listeners
	*/
	public final BeanWrapper getBeanWrapperForNewInstance() throws BeansException {
		BeanWrapper bw = newBeanWrapper();

		// Add any listeners
		
		// GO IN CHILD ALSO!? promote?
		List listeners = getListeners();
		for (int i = 0; i < listeners.size(); i++) {
			ListenerRegistration lr = (ListenerRegistration) listeners.get(i);
			if (lr.getListener() instanceof VetoableChangeListener) {
				VetoableChangeListener l = (VetoableChangeListener) lr.getListener();
				if (lr.getPropertyName() == null)
					bw.addVetoableChangeListener(l); 
				else
					bw.addVetoableChangeListener(lr.getPropertyName(), l);
			}
			else if (lr.getListener() instanceof PropertyChangeListener) {
				PropertyChangeListener l = (PropertyChangeListener) lr.getListener();
				if (lr.getPropertyName() == null)
					bw.addPropertyChangeListener(l);
				else
					bw.addPropertyChangeListener(lr.getPropertyName(), l);
			}
		}
		return bw;
	} // getBeanWrapperForNewInstance


	public String toString() {
		return "RootBeanDefinition: class is " + getBeanClass();
	}
	
} // class RootBeanDefinition