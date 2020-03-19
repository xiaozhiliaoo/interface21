package com.interface21.web.servlet;

import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.BeanFactory;
import com.interface21.beans.factory.ListableBeanFactory;
import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextAware;
import com.interface21.context.ApplicationContextException;
import com.interface21.util.StringUtils;

import java14.java.util.logging.Logger;


/**
 * Implementation of the HandlerMap interface to map from URLs
 * to beans. This is the default implmentation.
 * <br/>Supports CSV mappings:
 * e.g. "/foo.html,/bar.html"
 * Prepends / if not start of url
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class UrlHandlerMapping implements HandlerMapping {		
	
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private HashMap handlerMap;
	
	private ApplicationContext ctx;
	
	private Properties mappingProperties;
	
	/** Default handler. May be null. */
	private Object defaultHandler;
	

	//---------------------------------------------------------------------
	// Implementation of ApplicationContextAware
	//---------------------------------------------------------------------
	/** Set the ApplicationContext object used by this object
	 * @param ctx ApplicationContext object used by this object
	 * @param namespace namespace this object is in: null means default namespace
	 * @throws ApplicationContextException if initialization attempted by this object
	 * after it has access to the WebApplicatinContext fails
	 */
	public void setApplicationContext(ApplicationContext ctx) throws ApplicationContextException {
		this.ctx = ctx;
	}
	
	/** Return the ApplicationContext used by this object.
	 * @return the ApplicationContext used by this object.
	 * Returns null if setApplicationContext() has not yet been called:
	 * this can be useful to check whether or not the object has been initialized.
	 */
	public ApplicationContext getApplicationContext() {
		return this.ctx;
	}
	
	
	
	/**
	 * Return a handler for this request
	 * @return null if no match was found
	 * @throws ServletException if there is an internal error
	 */
	public Object getHandler(HttpServletRequest request) throws ServletException {
		Object handler = handlerMap.get(request.getServletPath()); 
		if (handler == null)
			handler = this.defaultHandler;
		return handler;
	}
	
	
	
	public void setMappings(Properties mappingProperties) {
		
		System.out.println("Property editor for Properties is " + PropertyEditorManager.findEditor(Properties.class));
		
		logger.info("Set properties to [" + mappingProperties + "]");
		this.mappingProperties = mappingProperties;
	}
	
	
	//---------------------------------------------------------------------
	// Implementation of HandlerMap
	//---------------------------------------------------------------------
	public void init() throws ApplicationContextException {
		this.handlerMap = new HashMap();
		
		if (!mappingProperties.isEmpty()) {
			Iterator itr = mappingProperties.keySet().iterator();
			while (itr.hasNext()) {
				String url = (String) itr.next();
				String beanName = mappingProperties.getProperty(url);
				
				System.out.println("Controller mapping from URL '" + url + "' to '" +  beanName + "'");
				
				if ("*".equals(url)) {
					
					this.defaultHandler = initHandler(url, beanName);
					logger.warning("Default mapping is to controller [" + defaultHandler + "]");
				}
				else {
					// Prepend with / if it's not present
					if (!url.startsWith("/"))
						url = "/" + url;					
					initHandler(url, beanName);
				}
			}
		}
		else {
			logger.warning("No mappings in " + getClass().getName());
		}
	}	// init
	
	
	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------
	/** Init the CommandGenerator object with this name in the bean factory.
	 * This will include setting the Context and namespace if the CommandGenerator is context aware.
	 * @return the handler
	 */
	private Object initHandler(String url, String beanName) throws ApplicationContextException {
		try {
			// WHAT ABOUT DIRECTORIES!? look at my old code
			// DIRECTORY BROWSE BEAN!>
			
			Object handler = ctx.getBean(beanName);
			//log4jCategory.info("Command servlet '" + getServletName() + "': Mapping from [" + beanName + "] to " + handler);
			
			if (handler instanceof UrlAwareHandler) {
				((UrlAwareHandler) handler).setUrlMapping(beanName);
			}
			
			if (handler instanceof ApplicationContextAware) {
				((ApplicationContextAware) handler).setApplicationContext(this.ctx);
				//log4jCategory.debug("Command servlet '" + getServletName() + "': CommandGenerator " + cp + " is WebApplicationContextAware: set context");
			}
			
			// Create a mapping to each part of the path
			
			handlerMap.put(url, handler);
			logger.config("Mapped url [" + url + "] onto handler [" + handler + "]");
			return handler;
		}
		catch (BeansException ex) {
			// We don't need to worry about NoSuchBeanDefinitionException:
			// we got the name from the bean factory.
			throw new ApplicationContextException("Error initializing handler bean for URLMapping '" + beanName + "': " + ex, ex);
		}
		catch (ApplicationContextException ex) {
			throw new ApplicationContextException("Can't set ApplicationContext on handler bean for URLMapping '" + beanName + "': " + ex, ex.getRootCause());
		}
	}	// initHandler
	
}	// class URLHandlerMap

