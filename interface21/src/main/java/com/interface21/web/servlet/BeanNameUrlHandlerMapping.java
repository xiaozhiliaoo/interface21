package com.interface21.web.servlet;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.BeanFactory;
import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextAware;
import com.interface21.context.ApplicationContextException;
import com.interface21.util.StringUtils;
import java14.java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;


/**
 * Implementation of the HandlerMap interface to map from URLs
 * to beans. This is the default implmentation.
 * <br/>Supports CSV mappings:
 * e.g. "/foo.html,/bar.html"
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class BeanNameUrlHandlerMapping implements HandlerMapping {
	
	/** Delimiter between multiple URLs in mappings */
	public static final String MULTI_URL_DELIMITER = ",";
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private HashMap handlerMap;
	
	private ApplicationContext ctx;
	

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
	
	//---------------------------------------------------------------------
	// Implementation of HandlerMap
	//---------------------------------------------------------------------
	public void init() throws ApplicationContextException {
		this.handlerMap = new HashMap();
		logger.fine("Looking for URL mappings...");
		String[] urlmaps = ctx.getBeanDefinitionNames();
		
		// Take anything beginning with a / in the bean name
		for (int i = 0; i < urlmaps.length; i++) {
			if (urlmaps[i].startsWith("/")) {
				logger.fine("Found URL mapping [" + urlmaps[i] + "]"); 
				initHandler(ctx, urlmaps[i]);
			}
			else {
				logger.fine("Rejected bean name '" + urlmaps[i] + "'");
			}
		}
	}	// init
	
	/**
	 * Return a handler for this request
	 * @return null if no match was found
	 * @throws ServletException if there is an internal error
	 */
	public Object getHandler(HttpServletRequest request) throws ServletException {
		return handlerMap.get(request.getServletPath()); 
	}
	
	
	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------
	/** Init the CommandGenerator object with this name in the bean factory.
	 * This will include setting the Context and namespace if the CommandGenerator is context aware.
	 */
	private void initHandler(BeanFactory bf, String beanName) throws ApplicationContextException {
		try {
			// WHAT ABOUT DIRECTORIES!? look at my old code
			// DIRECTORY BROWSE BEAN!>
			
			Object handler = bf.getBean(beanName);
			//log4jCategory.info("Command servlet '" + getServletName() + "': Mapping from [" + beanName + "] to " + handler);
			
			if (handler instanceof UrlAwareHandler) {
				((UrlAwareHandler) handler).setUrlMapping(beanName);
			}
			
			if (handler instanceof ApplicationContextAware) {
				((ApplicationContextAware) handler).setApplicationContext(this.ctx);
				//log4jCategory.debug("Command servlet '" + getServletName() + "': CommandGenerator " + cp + " is WebApplicationContextAware: set context");
			}
			
			// Create a mapping to each part of the path
			String[] mappedUrls = StringUtils.delimitedListToStringArray(beanName, MULTI_URL_DELIMITER);
			for (int i = 0; i < mappedUrls.length; i++) {
				handlerMap.put(mappedUrls[i], handler);
				logger.config("Mapped url [" + mappedUrls[i] + "] onto handler [" + handler + "]");
			}
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

