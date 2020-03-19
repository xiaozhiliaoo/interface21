
package com.interface21.beans.factory.support;

import com.interface21.beans.PropertyValues;

/**
 * A BeanDefinition describes a bean instance,
 * which has property values and further information supplied
 * by subclasses.
 * <br/>Once configuration is complete, a BeanFactory will be able
 * to return direct references to objects defined by
 * BeanDefinitions.
 * @author Rod Johnson
 */
public abstract interface BeanDefinition {
	
	PropertyValues getPropertyValues();
	
	/**
	 * Is this a <b>Singleton</b>, with a single, shared
	 * instance returned on all calls,
	 * or should be apply the <b>Prototype</b> design pattern,
	 * with each caller requesting an instance getting an independent
	 * instance?
	 * @return whether this is a Singleton
	 */
	boolean isSingleton();

}
