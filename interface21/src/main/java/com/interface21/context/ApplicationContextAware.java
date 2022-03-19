
package com.interface21.context;

/**
 * Interface to be implemented by any object that wishes to access the
 * directory and utility services of a WebApplicationManager.
 * @author  Rod Johnson
 * @since 13 May 2001
 * @version $RevisionId$
 */
public interface ApplicationContextAware {
	
	/** Set the ApplicationContext used by this object.
	 * Normally this call will be used to initialize the object.
	 * @param ctx ApplicationContext object used by this object
	 * @throws ApplicationContextException if initialization attempted by this object
	 * after it has access to the WebApplicatinContext fails
	 */
	void setApplicationContext(ApplicationContext ctx) throws ApplicationContextException;
	
	/** 
	 * Return the ApplicationContext used by this object.
	 * @return the ApplicationContext used by this object.
	 * <b>Must</b> return null if setApplicationContext() has not yet been called:
	 * this is used by the ApplicationContext to check whether or not the object has been initialized.
	 */
	ApplicationContext getApplicationContext();

}

