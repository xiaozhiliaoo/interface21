package com.interface21.web.servlet.mvc.multiaction;

import javax.servlet.http.HttpServletRequest;

import java.util.Properties;
import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import com.interface21.beans.factory.InitializingBean;
import com.interface21.util.StringUtils;

//TODO : must support default a la UrlHandlerMapping
// can inherit?

/**
* Maps /foo/bar/baz.html to foo_bar_baz
* Dsn't support wildcards
* @author Rod Johnson
 */
public class PropertiesMethodNameResolver implements MethodNameResolver, InitializingBean {
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	
	private Properties props;
	
	public PropertiesMethodNameResolver() {
	}
	
	public PropertiesMethodNameResolver(Properties props) {
		this.props = props;
	}
	
	public void setMappings(Properties props) {
		this.props = props;
		// WHAT ABOUT /!?
	}
	
	/**
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (props == null)
			throw new Exception("Must set 'mappings' property on PropertiesMethodNameResolver");
	}
	
	/**
	* @return a method name: never null
	*/
	public String getHandlerMethodName(HttpServletRequest request) throws NoSuchRequestHandlingMethodException {
		String servletPath = request.getServletPath();
		// forward slash prepend?
		String name = props.getProperty(servletPath);
		if (name == null)
			throw new NoSuchRequestHandlingMethodException(request);
			
		if (logger.isLoggable(Level.FINE))
			logger.fine("Returning MultiActionController method name '" + name + "' for servlet path '" + servletPath + "'");
		return name;
	}


}