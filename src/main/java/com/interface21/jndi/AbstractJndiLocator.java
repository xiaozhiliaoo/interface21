package com.interface21.jndi;

import com.interface21.beans.factory.InitializingBean;
import java14.java.util.logging.Logger;

import javax.naming.NamingException;
 
/** 
* 
* this is not a proxy.
* @author Rod Johnson
*/
public abstract class AbstractJndiLocator implements InitializingBean {
	
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private static String PREFIX = "java:comp/env/";
	
	private String jndiName;
	
	public AbstractJndiLocator() {
	}
	
	public AbstractJndiLocator(String jndiName) {
		setJndiName(jndiName);
	}

	
	/**
	 * Set the JNDI name. If it doesn't begin java:comp/env/
	 * we add this prefix
	 * @param jndiName JNDI name of bean to look up
	 */
	public final void setJndiName(String jndiName) {
		if (!jndiName.startsWith(PREFIX))
			jndiName = PREFIX + jndiName;
		this.jndiName = jndiName;
	}
	
	public final String getJndiName() {
		return jndiName;
	}
	
	
	//---------------------------------------------------------------------
	// Implementation of InitializingBean
	//---------------------------------------------------------------------
	/**
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public final void afterPropertiesSet() throws Exception {
		if (this.jndiName == null || this.jndiName.equals(""))
			throw new Exception("Property 'jndiName' must be set on " + getClass().getName());
		Object o = lookup(jndiName);
		located(o);
	}
	
	protected abstract void located(Object o);
	
	
	private Object lookup(String jndiName) throws NamingException {
		logger.info("Looking up object with jndiName '" + jndiName + "'");
		
		// This helper will close JNDI context
		Object o = new JndiServices().lookup(jndiName);
		
		logger.fine("Looked up objet with jndiName '" + jndiName + "' OK: [" + o + "]");
		return o;
	}
	
} 	// class AbstractServiceLocator