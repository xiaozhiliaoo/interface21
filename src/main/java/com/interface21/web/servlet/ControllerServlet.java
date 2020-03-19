
package com.interface21.web.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java14.java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.NoSuchBeanDefinitionException;
import com.interface21.context.ApplicationContextException;
import com.interface21.web.servlet.mvc.SimpleControllerHandlerAdapter;
import com.interface21.web.servlet.view.ResourceBundleViewResolver;


/**
 * Front Controller for use within the application framework.
 * This servlet is very flexible: it can be used with just about any
 * workflow conceivable, with the installation of the appropriate adapter classes.
 * <br/>This servlet offers the following functionality that distinguishes
 * it from other MVC frameworks:
 * <li>It is based around a Java beans configuration mechanism.
 * <li>It can use any HandlerMapping implementation--whether standard,
 * or provided as part of an application--to control the routing of requests
 * to handler objects. (请求到处理对象-HandlerMapping)
 * Additional HandlerMap objects can be added through defining beans in the
 * servlet's application context that implement the HandlerMap interface in this package.
 * HandlerMaps can be given any bean name (they are tested
 * by type). However, the bean name is significant, as <b>
 * maps will be applied in alphabetical order by bean name</b>.
 * <li>It can use any HandlerAdapter
 * * Additional HandlerAdapter objects can be added through BF(Bean Factory???)
 * <li>It publishes RequestHandled events through the ApplicationContext
 * <p/>A web application can use any number of controller servlets.
 * Each servlet will operate in its own namespace. Only the default name space,
 * and any config objects set for the application as a whole, will be shared.
 * <br/>A ControllerServlet cannot be successfully instantiated unless
 * a WebApplicationContext object has been successfully initialized and placed in
 * the application's ServletContext. However, if this requirement is satisfied,
 * individual controller servlets may success or fail to start up independently.
 * <p/>Because this extends HttpServletBean, rather than HttpServlet directly,
 * bean properties are mapped onto it.
 * @see com.interface21.beans.factory.BeanFactory
 * @author Rod Johnson
 * @version $Revision: 1.2 $
 */
public class ControllerServlet extends FrameworkServlet {
	
	public static final String HEADER_IFMODSINCE = "If-Modified-Since";
	
	private static final String HEADER_LASTMOD = "Last-Modified";



	/** Well-known name for the ViewResolver object in the bean factory for
	 * this namespace 
	 */
	public static final String VIEW_RESOLVER_BEAN_NAME = "viewResolver";
	
	/** Request attribute to hold current web application context.
	 * Otherwise only the global web app context is obtainable by tags etc.
	 * Allows message resolution.
	 */
	public static final String SERVLET_WEB_APPLICATION_CONTEXT_ATTRIBUTE = "com.interface21.web.servlet.ControllerServlet.SERVLET_WEB_APPLICATION_CONTEXT";

	/** Will be added to a request before processing begins if we're in debug mode */
	public static final String HANDLER_REQUEST_ATTRIBUTE = "com.interface21.web.servlet.ControllerServlet.HANDLER";

	/** Will be added to a request before processing begins if we're in debug mode */
	public static final String HANDLER_ADAPTER_REQUEST_ATTRIBUTE = "com.interface21.web.servlet.ControllerServlet.HANDLER_ADAPTER";

	/** Request attribute to hold viewName if we're running in debug mode */
	public static final String VIEW_NAME_ATTRIBUTE = "com.interface21.web.servlet.ControllerServlet.VIEW_NAME";

	/** Synthetic, bean name for the default HandlerMap, if no HandlerMap objects are
	 * provided in the BeanFactory for this servlet's namespace.
	 */
	public static final String DEFAULT_HANDLER_MAP = "defaultHandlerMap";

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** Set of HandlerMaps */
	private List handlerMaps;

	/** Command type --> CommandWorkflow */
	private List handlerAdapters;


	/** ViewResolver used by this servlet */
	private ViewResolver	viewResolver;


	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------

	// No bean properties are currently added by this class


	//---------------------------------------------------------------------
	// Overridden methods of AbstractFrameworkServlet
	//---------------------------------------------------------------------
	/**
	 * Overridden method of HttpServletBean, invoked after any bean properties
	 * have been set and the WebApplicationContext and BeanFactory for this
	 * namespace is available.
	 * <br/>Load HandlerMap and HandlerAdapter objects, and configure
	 * a ViewResolver.
	 */
	protected void initFrameworkServlet() throws ServletException {
		
		// We will keep handler maps in sorted order
		this.handlerMaps = new LinkedList();

		// Find all HandlerMappings in the ApplicationContext
		String[] maps = getWebApplicationContext().getBeanDefinitionNames(HandlerMapping.class);
		
		// Sort so that we can ensure that rules are applied on order
		Arrays.sort(maps);
		
		for (int i = 0; i < maps.length; i++) {
			try {
				HandlerMapping hm = (HandlerMapping) getWebApplicationContext().getBean(maps[i]);
				registerHandlerMap(hm, maps[i]);
			}
			catch (BeansException ex) {
				throw new ServletException("Cannot initialize handler map '" + maps[i] + "'");
			}
		}
		// Ensure we have at least one handler, by registering
		// a URL map if no other handlers are found.
		if (handlerMaps.size() == 0) {
			logger.config("No HandlerMaps found in servlet '" + getServletName() + "': using default");
			
			// Add default handler mapping to end of list
			HandlerMapping hm = new BeanNameUrlHandlerMapping();
			registerHandlerMap(hm, DEFAULT_HANDLER_MAP);
		}

		this.handlerAdapters = new LinkedList();
		String[] handlers = getWebApplicationContext().getBeanDefinitionNames(HandlerAdapter.class);
		for (int i = 0; i < handlers.length; i++) {
			initHandlerAdapter(handlers[i]);
		}
		// Always add RequestProcessor adapter!?
		HandlerAdapter ha = new SimpleControllerHandlerAdapter();
		initHandlerAdapter(ha, "defaultHandlerAdapter");

		initViewResolver();
	}	// initFrameworkServlet


	/**
	 * Initialize the ViewResolver used by this class.
	 * This may come from the BeanFactory. If no bean is defined
	 * with the given name in the BeanFactory for this namespace,
	 * we default to a ResourceBundleViewResolver.
	 * This should be suitable for most requirements.
	 */
	private void initViewResolver() throws ServletException {
		try {
			this.viewResolver = (ViewResolver) getWebApplicationContext().getBean(VIEW_RESOLVER_BEAN_NAME);
			logger.config("Loaded viewResolver [" + viewResolver + "]");
		}
		catch (NoSuchBeanDefinitionException ex) {
			// We need to use the default
			this.viewResolver = new ResourceBundleViewResolver();
			logger.config("Unable to load view resolver: bean with name '" + VIEW_RESOLVER_BEAN_NAME + "': using default ViewResolver [" + this.viewResolver + "]");
		}
		catch (BeansException ex) {
			// We tried and failed to load the ViewResolver specified by a bean
			throw new ServletException("Fatal error loading view resolver: bean with name '" + VIEW_RESOLVER_BEAN_NAME + "' is required in servlet '" + getServletName()  + "': using default", ex);
		}

		 // Wherever we got our ViewResolver, initialize it
		try {
			viewResolver.setApplicationContext(getWebApplicationContext());
		}
		catch (ApplicationContextException ex) {
			throw new ServletException("Fatal error initializing ViewResolver with bean name '" + VIEW_RESOLVER_BEAN_NAME + "' in namespace '" + getServletName()  + "'", ex);
		}
	}	// initViewResolver


	/** Register and initialize the given HandlerMap object
	 * This will include setting the Context and namespace if the CommandGenerator is context aware.
	 * @param hm HandlerMap object to init
	 * @param name bean name or other friendly name of this object
	 * @param lbf BeanFactory used in this servlet's namespace
	 */
	private void registerHandlerMap(HandlerMapping hm, String name) throws ServletException {
		try {
			//log4jCategory.info("ControllerServlet '" + getServletName() + "': Mapping from [" + beanName + "] to " + rh);
			hm.setApplicationContext(getWebApplicationContext());

			// HandlerMaps want to know about the current BeanFactory
			hm.init();
			handlerMaps.add(hm);
		}
		catch (ApplicationContextException ex) {
			throw new ServletException("Can't set ApplicationContext on HandlerMap with name '" + name + "': " + ex, ex);
		}
	}	// initHandlerMap



	/** Initialize the handler bean with the given name in the bean factory
	 * This will include setting the Context and namespace if the object is context aware.
	 * @throws ServletException if there is an error trying to instantiate and initialize the handler bean
	 */
	private void initHandlerAdapter(String beanName) throws ServletException {
		try {
			HandlerAdapter ha = (HandlerAdapter) getWebApplicationContext().getBean(beanName);
			// Invoke the overloaded method to complete the process
			initHandlerAdapter(ha, beanName);
		}
		catch (BeansException ex) {
			// We don't need to catch NoSuchBeanDefinitionException:
			// we got the name from the bean factory.
			throw new ServletException("Error initializing HandlerAdapter bean '" + beanName + "': " + ex, ex);
		}
	}	// initHandlerAdapter


	/**
	 * Initialize the given HandlerAdapter instance.
	 * @param ha HandlerAdapter object to initialize
	 * @param beanName bean factory name or other friendly name given to this object
	 */
	private void initHandlerAdapter(HandlerAdapter ha, String beanName) throws ServletException {
		try {
			logger.config("Command servlet '" + getServletName() + "': Initializing HandlerAdapter object with name [" + beanName + "]: " + ha);
			ha.setApplicationContext(getWebApplicationContext());
			handlerAdapters.add(ha);
			//	log4jCategory.info("Command servlet '" + getServletName() + "': CommandWorkflow object with name '" + beanName + "': is handler for command type " + commandType.getName() + ": " + cw);
		}
		catch (ApplicationContextException ex) {
			throw new ServletException("Can't set ApplicationContext on HandlerAdapter bean '" + beanName + "': " + ex, ex);
		}
	}	// initHandlerAdapter



	/**
	 * Implementation of protected abstract method.
	 * Obtain and use the handler for this method.
	 * The handler will be obtained by applying the servlet's HandlerMaps
	 * in order.
	 * The HandlerAdapter will be obtained by querying the servlet's
	 * installed HandlerAdapters to find the first that supports the Handler class.
	 * Both doGet() and doPost() are handled by this method.
	 * It's up to HandlerAdapters to decide which methods
	 * are acceptable.
	 */
	protected void doService(HttpServletRequest request, HttpServletResponse response, boolean debugMode) throws ServletException, IOException {

		logger.fine("ControllerServlet with name '" + getServletName() + "' received request for [" + request.getServletPath() + "]");

		// Make web application context available
		request.setAttribute(SERVLET_WEB_APPLICATION_CONTEXT_ATTRIBUTE, getWebApplicationContext());

		Object mappedHandler = getHandler(request);

		if (mappedHandler == null) {
			// If we didn't find a handler
			logger.severe("No mapping for [" + request.getServletPath() + "] in ControllerServlet with name '" + getServletName() + "'");
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			//****TODO : 	CHECK THIS WORKS PROPERLY WITH SUPERCLASS
			return;
		}

		if (debugMode) {
			request.setAttribute(HANDLER_REQUEST_ATTRIBUTE, mappedHandler);
		}

		// This will throw an exception if no handler is found
		HandlerAdapter ha = getHandlerAdapter(mappedHandler);

		if (debugMode) {
			request.setAttribute(HANDLER_ADAPTER_REQUEST_ATTRIBUTE, ha);
		}
		
		if (wasRevalidated(request, response, ha, mappedHandler)) {
			return;
		}


		ModelAndView mv = ha.handle(request, response, mappedHandler);
		if (mv != null) {
			if (logger.isLoggable(Level.FINE))
				logger.fine("Will render model in ControllerServlet with name '" + getServletName() + "'");
			render(mv, request, response);
		}
		else {
			logger.fine("Null ModelAndView returned to ControllerServlet with name '" + getServletName() + "': assuming HandlerAdapter completed request handling");
		}

			/*
			if (debugMode && failureCause != null) {
				// Output verbose information about failure, showing
				// how far we got (some of the variables output will be null
				log4jCategory.error("url='" + request.getServletPath() + "': Failure (" + failureCause + "); " +
				"handler=[" + mappedHandler + "]; handlerAdapter=[" + ha + "]", failureCause);
			}
			*/
	}	// doService
	
	
	/**
	 * Was the request successfully revalidated?
	 * In this case we can return
	 */
	private boolean wasRevalidated(HttpServletRequest request, HttpServletResponse response, HandlerAdapter ha, Object mappedHandler) {
		// Based on code from javax.servlet.HttpServlet from Apache Servlet API
		// HttpServlet checks getLastModified() before calling doGet(), so we need to
		// leave the default implementation of getLastModified() to return -1, before
		// handling unmodified requests here.
		if (!"GET".equals(request.getMethod())) 
			return false;
			
		// Apply last modified rule
		long ifModifiedSince = request.getDateHeader(HEADER_IFMODSINCE);
	    if (ifModifiedSince == -1) 
	    	return false;
	    	
	    logger.fine("GET request: " + HEADER_IFMODSINCE + " request header present");
	    // We have an if modified since header (value is -1 if we don't)
	    long lastModified = ha.getLastModified(request, mappedHandler);
		if (ifModifiedSince < (lastModified / 1000 * 1000)) {
			// Lastmodified was -1, indicating it
			// wasn't understood, or was earlier than the servlet mod
			// date. We need to regenerate content
			logger.fine("GET request: " + HEADER_IFMODSINCE + " request header contains an earlier date than lastModified date. Will regenerate content and reset lastModified header." );
			setLastModifiedIfNecessary(response, lastModified);
			return false;
	    }
	    else {
	    	// If mod header present and shows a later date than last modified date on the resource
	    	logger.fine("GET request: " + HEADER_IFMODSINCE + " request header contains a later date than lastModified date, or revalidation isn't supported. Indicating unmodified.");
	    	response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
	    	return true;
	    }
	}
	

  	/*
     * Sets the Last-Modified entity header field, if it has not
     * already been set and if the value is meaningful.  Called before
     * doGet, to ensure that headers are set before response data is
     * written.  A subclass might have set this header already, so we
     * check.
     */
    private void setLastModifiedIfNecessary(HttpServletResponse response, long lastModified) {
    	// Based on code from javax.servlet.HttpServlet
		if (!response.containsHeader(HEADER_LASTMOD) && lastModified >= 0)
	   		response.setDateHeader(HEADER_LASTMOD, lastModified);
    }


	/**
	 * Return the HandlerMap for this request.
	 * Try all handler maps in order.
	 * @return null if no HandlerMap could be found
	 * @throws ServletException
	 */
	private Object getHandler(HttpServletRequest request) throws ServletException {
		// Guaranteed to get in alphabetical order
		Iterator itr = handlerMaps.iterator();
		while (itr.hasNext()) {
			HandlerMapping hm = (HandlerMapping) itr.next();
			if (logger.isLoggable(Level.FINE))
				logger.fine("Testing handler map [" + hm  + "] in ControllerServlet with name '" + getServletName() + "'");
			Object handler = hm.getHandler(request);
			if (handler != null)
				return handler;
		}
		return null;
	}	// getHandler


	/**
	 * Return the HandlerAdapter for this handler class
	 * @throws ServletException if no hHndlerAdapter can be found for the handler.
	 * This is a fatal error.
	 */
	private HandlerAdapter getHandlerAdapter(Object h) throws ServletException {
		for (int i = 0; i < handlerAdapters.size(); i++) {
			HandlerAdapter ha = (HandlerAdapter) handlerAdapters.get(i);
			if (logger.isLoggable(Level.FINE))
				logger.fine("Testing handler adapter [" + ha + "]");
			if (ha.supports(h))
				return ha;
		}
		throw new ServletException("No adapter for handler " + h);
	}


	/**
	 * Render the given ModelAndView. This is the last stage in handling a request.
	 * It may involve resolving the view by name.
	 * @throws IOException if there's a problem rendering the view
	 * @throws ServletException if the view cannot be resolved.
	 */
	 private void render(ModelAndView mv, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		View v = null;
		if (mv.isReference()) {
			// We need to resolve this viewname
			String viewName = mv.getViewname();
			if (isDebugMode(request)) {
				// If we're in debug mode, show the view name
				logger.fine("Debug request attribute set: setting viewName of '" + viewName + "' as a request attribute");
				request.setAttribute(VIEW_NAME_ATTRIBUTE, viewName);
			}
			// A ViewResolver should respect the current locale
			v = viewResolver.resolveViewname(viewName, request.getLocale());
		}
		else {
			// No need to lookup the viewname: the ModelAndView
			// object contains the actual view
			v = mv.getView();
		}

		if (v == null)
			throw new ServletException("Error in ModelAndView object or View resolution encountered by servlet with name '" + getServletName() + "'. View cannot be null in render with ModelAndView=[" + mv + "]");
		v.render(mv.getModel(), request, response);
	}	// render

}	// ControllerServlet
