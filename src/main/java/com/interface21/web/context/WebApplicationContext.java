
package com.interface21.web.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.interface21.context.ApplicationContext;

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
public interface WebApplicationContext extends ApplicationContext {

	/** Context attribute to bind root WebApplicationContext to on successful startup
	 * Subclasses must bind themselves.
	 */
	String WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME = "com.interface21.framework.web.context.WEB_APPLICATION_CONTEXT";	
	
	/** Give this context access to the standard Servlet API ServletContext for this
	 * web application. This method amounts to an init method: implementations are
	 * responsible for loading their URL or other config, and reloading.
	 */
	void setServletContext(ServletContext servletContext) throws ServletException;
	
	
	/** Return the standard Servlet API ServletContext for
	 * this application
	 * @return the standard Servlet API ServletContext for
	 * this application
	 */
	ServletContext getServletContext();
	
}	// interface WebApplicationContext

