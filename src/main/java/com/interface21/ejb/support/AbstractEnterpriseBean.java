
package com.interface21.ejb.support;

import java14.java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ejb.EnterpriseBean;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.ListableBeanFactory;
import com.interface21.jndi.JndiBeanFactory;


/** 
 * Superclass for all EJBs.
 * Provides BeanFactory and logger support
 * @author Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractEnterpriseBean implements EnterpriseBean {
	
	
	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
	/**
	 * Logger, available to subclasses
	 */
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	
	/** BeanFactory available to subclasses.
	 * Lazy loaded for efficiency.
	 */ 
	private ListableBeanFactory		beanFactory;
	
	
	//-------------------------------------------------------------------------
	// BeanFactory methods
	//-------------------------------------------------------------------------	
	/**
	 * Get the BeanFactory for this EJB
	 * Uses lazy loading for efficiency. (We don't
	 * need to consider thread safety in an EJB).
	 */
	protected final ListableBeanFactory getBeanFactory() {		
		if (this.beanFactory == null) {
			loadBeanFactory();
		}
		return this.beanFactory;
	}
	
	
	private void loadBeanFactory() {
		logger.info("Loading bean factory");
		try {
			this.beanFactory = new JndiBeanFactory("java:comp/env");
		}
		catch (BeansException ex) {
			throw new EJBException("Cannot create bean factory", ex);
		}
	}
	
} 	// class AbstractEnterpriseBean
