
package com.interface21.ejb.access;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.FatalBeanException;
import com.interface21.jndi.JndiServices;
import java14.java.util.logging.Level;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.naming.NamingException;

/*
 * Superclass of Locators exposing local SLSBs
 */
public abstract class AbstractLocalStatelessSessionBeanDefinition extends AbstractStatelessSessionBeanDefinition {
	
	/** 
	 * Name of no arg create() method required on EJB homes,
	 * but not part of EJBLocalHome
	 */
	private static final String CREATE_METHOD = "create";

	/** Cann never go stale */
	private EJBLocalHome home;
	
	/**
	 * BeanWrapper for the home interface.
	 * We need this because we need to invoke the no-arg create()
	 * method by name, as it is a home subclass-specific method,
	 * rather than a method on EJBLocalHome.
	 */
	private BeanWrapper homeBeanWrapper;
	
	
	/**
	 * @return the EJBLocalHome object cached in this class
	 */
	protected EJBLocalHome getHome() {
		return this.home;
	}
	
	/**
	 * Cache EJB home. Need to do this once only.
	 * This also means saving a bean wrapper.
	 */
	private void cacheEjbHome(String jndiName) throws NamingException {
		logger.fine("Looking up stateless session EJB local home with jndiName '" + jndiName + "'");
		
		// This helper will close JNDI context
		Object homeObject = new JndiServices().lookup(jndiName);
		
		logger.info("Caching stateless session EJB local home with jndiName '" + jndiName + "' OK: [" + homeObject + "]");
		
		// Don't need IIOP-style narrowing for local objects
		this.home = (EJBLocalHome) homeObject;
		
		// Cache a BeanWrapper so we can invoke the create()
		// method using reflection
		this.homeBeanWrapper = new BeanWrapperImpl(this.home);
	}
	
	/**
	 * Subclasses should call this to get a new session bean instance,
	 * which they can cast to the required type.
	 * <br/>Creates session bean using cached home and
	 * the BeanWrapper wrapping the home.
	 * Threadsafe.
	 * @return a new session bean instance
	 */
	protected EJBLocalObject newSessionBeanInstance() {
		if (logger.isLoggable(Level.FINE))
			logger.fine("Trying to create EJB");
		EJBLocalObject session = (EJBLocalObject) homeBeanWrapper.invoke(CREATE_METHOD, null);
		if (logger.isLoggable(Level.FINE))
			logger.fine("EJB created OK [" + session + "]");
		return session;
	}
	
	/**
	 * Invoked when home is available
	 */
	protected abstract BeanWrapper newBeanWrapperInternal();

	/**
	 * Invoked when a new wrapped object is created.
	 * Subclasses may override this, to create bean
	 * wrappers differently or perform custom preprocessing
	 */
	protected final BeanWrapper newBeanWrapper() {
		try {
			cacheEjbHome(getJndiName());		
			return newBeanWrapperInternal();
		}
		catch (NamingException ex) { 
			throw new FatalBeanException("Can't contact local EJB with jndiName '" + getJndiName() + "'", ex);
		}
	}

}
