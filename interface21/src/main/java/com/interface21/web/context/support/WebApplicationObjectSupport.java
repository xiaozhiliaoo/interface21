
package com.interface21.web.context.support;

import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextAware;
import com.interface21.context.ApplicationContextException;
import com.interface21.web.context.WebApplicationContext;
import java14.java.util.logging.Logger;

/**
 * Convenient superclass for web application objects such as
 * controllers.
 * @author Rod Johnson
 */
public abstract class WebApplicationObjectSupport implements ApplicationContextAware {
	
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private WebApplicationContext webApplicationContext;
	
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	public WebApplicationObjectSupport() {
	}
	
	
	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	
	
	//---------------------------------------------------------------------
	// Implementation of WebApplicationContextAware
	//---------------------------------------------------------------------

	/**
	 * @see ApplicationContextAware#setApplicationContext(ApplicationContext)
	 */
	public final void setApplicationContext(ApplicationContext ctx) throws ApplicationContextException {
		logger.entering(getClass().getName(), "setApplicationContext");
		this.webApplicationContext = (WebApplicationContext) ctx;
		init();
	}
	
	/**
	 * @see ApplicationContextAware#getApplicationContext()
	 */
	public final ApplicationContext getApplicationContext() {
		return webApplicationContext;
	}
	
	
	/**
	 * Subclasses can override this
	 */
	protected void init() throws ApplicationContextException {
		logger.fine("NOP init()");
	}

	/**
	 * Convenience method for subclasses
	 */
	protected final WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}
	
}	// class WebApplicationObjectSupport
