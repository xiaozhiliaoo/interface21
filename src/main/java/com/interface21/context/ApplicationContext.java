
package com.interface21.context;

import com.interface21.beans.factory.ListableBeanFactory;


/** Interface to provide configuration for a web application. This is
 * read-only while the application is running, but may be reloaded if the
 * implementation supports this.
 * <p/>Can construct with a no arg constructor, then setServletContext()
 * <p/>The configuration consists of:
 * <ul>A set of WebApplicationListeners defined
 * <ul>A set of config objects, identified by name. This will
 * avoid the need for applications to use singletons.
 * <ul>Access to the BeanFactory in which listeners and config objects
 * are defined. This allows particular application objects to look for
 * their own well-known objects by name.
 * <li>
 * </ul>
 * @author Rod Johnson
 * @since January 19, 2001
 * @version $Revision: 1.1.1.1 $
 */
public interface ApplicationContext extends MessageSource, ListableBeanFactory {
	
	/** Name of options bean. If none is supplied, DEFAULT_OPTIONS will be used
	 */
	String OPTIONS_BEAN_NAME = "ApplicationContext.options";
	
	/** Return the parent context, or null if there is no parent,
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or null if there is no parent
	 */
	ApplicationContext getParent();
	
	/** Friendly name for context
	 * @return a display name for the context
	*/
	String getDisplayName();


	/** Notify all listeners registered with this application of 
	 * a web application event. Events may be framework events (such as RequestHandledEvent)
	 * or application-specific events.
	 * @param e event to publish
	 */
	void publishEvent(ApplicationEvent e);
	
	/** Load or refresh the persistent representation of the configuration, which
	 * might for example be an XML file, properties file or relational database schema.
	 * @param servletConfig if the config is invalid
	 * @throws IOException if the config cannot be loaded
	 */
	void refresh() throws ApplicationContextException;
	
	
	/** Return the timestamp when this context was first loaded
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();
	
	
	/**
	 * Return context options. These control reloading etc.
	 * ApplicationContext implementations may subclass ContextOptions to
	 * add additional properties. It must always be a bean.
	 * @return context options. Never returns null. Context may be
	 * supplied as a bean in the default bean factory. If it is
	 * not supplied, the ApplicationContext implementation must supply
	 * a context.
	 */
	ContextOptions getOptions();
	
	/**
	 * Put an object available for sharing. Note that this
	 * method is not synchronized. As with Java 2 collections,
	 * it's up to calling code to ensure thread safety.
	 * Also, this doesn't work in a cluster. It's
	 * analogous to putting something in a ServletContext.
	 * @param key object key
	 * @param o object to put
	 */
	void shareObject(String key, Object o);
	
	/**
	 * Retrieve a shared object added with a call to shareObject().
	 * @return the object, or null if no object is known under
	 * this name (this is not an error).
	 */
	Object sharedObject(String key);
	
	/** 
	 * Remove a shared object added with a call to shareObject().
	 * Does nothing if the object was null.
	 * @param key the object was added with
	 * @return the object if it was found; or null.
	 */
	Object removeSharedObject(String key);
		
}	// interface ApplicationContext

