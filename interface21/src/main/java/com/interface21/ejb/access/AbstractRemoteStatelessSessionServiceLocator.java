package com.interface21.ejb.access;

import com.interface21.beans.FatalBeanException;
import com.interface21.jndi.AbstractJndiLocator;

import javax.ejb.EJBHome;
 
/** 
* Superclass for business delegate objects 
* or service locators that contact a single
* local SLSB.
* Exposes JNDI name property and loads it when initialized
* by a BeanFactory.
* It's safe to cache a local home.
* Subclasses must implement all methods themselves and call
* a session bean instance as necessary;
* this is not a proxy.
* @author Rod Johnson
*/
public abstract class AbstractRemoteStatelessSessionServiceLocator 
	extends AbstractJndiLocator  {
	
	
	public AbstractRemoteStatelessSessionServiceLocator() {
	}
	
	public AbstractRemoteStatelessSessionServiceLocator(String jndiName) {
		super(jndiName);
	}
	
	
	/**
	 * Subclasses should implement this to do whatever they need to to
	 * after an EJB home is available. They must themselves
	 * create session bean instances as necessary.
	 */
	protected abstract void setEjbHome(EJBHome home);


	/**
	 * @see AbstractJndiLocator#located(Object)
	 */
	protected void located(Object o) {
		if (!(o instanceof EJBHome))
			throw new FatalBeanException("Located object with JNDI name '" + getJndiName() + "' must be an EJBHome object", null);
		setEjbHome((EJBHome) o);
	}

} 	// class LocalStatelessSessionServiceLocator