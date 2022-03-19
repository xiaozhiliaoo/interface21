package com.interface21.web.context;

import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to 
 * bootstrap WebApplicationContext object.
 * <br/>This servlet requires a single config parameter to be provided in the
 * web.xml deployment descriptor:
 * <li>contextClass: the class name of the WebApplicationContext implementation to provide
 * a context for this application. Note that this class must have a public no arg constructor.
 * WebApplicationContext implementations are responsible for loading their own config parameters
 * from the application's ServletContext: this servlet merely instantiates the class and 
 * provides it with the current ServletContext object.
 * <p/>
 * <b>Usage note:</b> Look in Log4J log, not server log, for details of
 * any startup problems.
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class ContextLoaderServlet extends HttpServlet {
	
	/** Config param to this servlet for log4j properties location */
	public static final String LOG4J_PROPERTIES_PARAM = "log4jPropertiesUrl";
	
	/** Config param to this servlet for the WebApplicationContext
	 * implementation class to use
	 */
	public static final String CONTEXT_CLASS_PARAM = "contextClass";
	
	/** URL within the WAR of this servlet's status page
	 */
	public static final String STATUS_URL = "_context/status.jsp";
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected Logger logger = Logger.getLogger(getClass().getName());
	
	/** Object we reflectively instantiate providing this application's
	 * context
	 */
	private WebApplicationContext webApplicationContext;
	
	/** Identification String for this servlet */
	private String identifier;
	
	
	//---------------------------------------------------------------------
	// Overridden methods
	//---------------------------------------------------------------------
	/**
	 * Configure the log4j system and instantiate and bind the WebApplicationContext
	 * implementation as a ServletContext attribute
	 * @throws ServletException if startup fails
	 */
	public void init() throws ServletException {
		this.identifier = "ContextLoaderServlet with name '" + getServletConfig().getServletName() + "' ";
		
		String contextClass = getServletConfig().getInitParameter(CONTEXT_CLASS_PARAM);
		if (contextClass == null)
			throw new ServletException("Cannot load context class param '" + CONTEXT_CLASS_PARAM + "'");
		
		
		// Now we must load the WebApplicationContext. 
		// It configures itself: all we need to do is construct the class with a no-arg
		// constructor, and invoke setServletContext 
		try {
			Class clazz = Class.forName(contextClass); 			
			this.webApplicationContext = (WebApplicationContext) clazz.newInstance();						
			webApplicationContext.setServletContext(getServletContext());						
		}
		catch (ClassNotFoundException ex) {
			String mesg = getIdentifier() + "Failed to load config class '" + contextClass + "'";
			logger.logp(Level.SEVERE, getClass().getName(), "init", mesg, ex);
			throw new ServletException(mesg + ": " + ex);
		}
		catch (InstantiationException ex) {
			String mesg = getIdentifier() + "Failed to instantiate config class '" + contextClass + "': does it have a public no arg constructor?";
			logger.logp(Level.SEVERE, getClass().getName(), "init", mesg, ex);
			throw new ServletException(mesg + ": " + ex);
		}
		catch (IllegalAccessException ex) {
			String mesg = getIdentifier() + "Failed with IllegalAccess to find or instantiate config class '" + contextClass + "': does it have a public no arg constructor?";
			logger.logp(Level.SEVERE, getClass().getName(), "init", mesg, ex);
			throw new ServletException(mesg + ": " + ex);
		}
		catch (Throwable t) {
			String mesg = getIdentifier() + "Unexpected error loading config: " + t;
			logger.logp(Level.SEVERE, getClass().getName(), "init", mesg, t);			
			throw new ServletException(mesg, t);
		}	
	}	// init
	
	
	/** Return a description of this servlet
	 * @return a description of this servlet
	 */
	public String getServletInfo() {
		return "ContextLoaderServlet v 1.0";
	}
	
	
	//---------------------------------------------------------------------
	// Interface methods
	//---------------------------------------------------------------------
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//logger.info("Showing status at " + STATUS_URL);
		//request.setAttribute(WebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME, webApplicationContext);
		//request.getRequestDispatcher(STATUS_URL).forward(request, response);
		
		logger.config("!!!!!!!!!!!! RELOADING CONFIG");
		init();
		response.getOutputStream().println("RELOADED CONTEXT");
		
	}

	
	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------
	/** Return a String identifing this Servlet
	 * @return a String identifing this Servlet
	 */
	private String getIdentifier() {
		return identifier;
	}

}	// class ContextLoaderServlet
