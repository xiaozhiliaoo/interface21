
package com.interface21.web.servlet.mvc;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.interface21.context.ApplicationContextException;
import com.interface21.web.servlet.LastModified;
import com.interface21.web.servlet.ModelAndView;

/**
 * Convenient superclass for controller implementations.
 * Saves WebApplicationContext.
 * <br/>
 * Exposes the following bean properties:
 * supportedMethods: CSV of supported methods, such as "GET,POST".
 * Default allows GET and POST.
 * requireSession: boolean. Whether a session should be required, meaning
 * that subclasses can call request.getSession() in the assurance that there <b>is</b>
 * a session.
 * @author Rod Johnson
 */
public abstract class AbstractController 
	extends WebContentGenerator
	implements Controller {
	
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** HashMap of method (GET/POST etc.) --> Object or null depending on whether supported.
	 * GET and POST are supported by default.
	 */
	private Map	supportedMethods;
	
	private boolean requireSession;
	
	private int cacheSeconds = -1;
	
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	public AbstractController() {
		supportedMethods = new HashMap();
		supportedMethods.put("POST", Boolean.TRUE);
		supportedMethods.put("GET", Boolean.TRUE);
	}
	
	
	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	/**
	 * Set as CSV: automatic property editor will get the type right
	 * @param supportedMethodsArray
	 */
	public final void setSupportedMethods(String[] supportedMethodsArray) throws ApplicationContextException {
		if (supportedMethodsArray == null || supportedMethodsArray.length == 0)
			throw new ApplicationContextException("supportedMethodsCSV must include some methods");
		supportedMethods.clear();
		for (int i = 0; i < supportedMethodsArray.length; i++) {
			supportedMethods.put(supportedMethodsArray[i], Boolean.TRUE);
			logger.config("Supported request method '" + supportedMethodsArray[i] + "'");
		}
	}
	
	public final void setRequireSession(boolean requireSession) {
		this.requireSession = requireSession;
		logger.config("Requires session set to " + requireSession);
	}
	
	
	/**
	 * if 0 disable caching.
	 * default is no caching header generation
	 * Only if this is set to 0 (no cache) or a positive value (cache for this many
	 * seconds) will this class generate cache headers.
	 * They can be overwritten by subclasses anyway, before content is generated.
	 */
	public final void setCacheSeconds(int seconds) {
		this.cacheSeconds = seconds;
	}
	
	
	
	
	//---------------------------------------------------------------------
	// Implementation of Controller
	//---------------------------------------------------------------------
	/**
	 * @see Controller#handleRequest(HttpServletRequest, HttpServletResponse)
	 */
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		// Check whether we should support the request method
		String method = request.getMethod();
		if (supportedMethods.get(method) == null) {
			logger.info("Disallowed '" + method + "' request");
			throw new ServletException("This resource does not support request method '" + method + "'");
		}
		
		//SHOULD SESSION MANAGER BE CONFIGURABLE? for servlet?
		// allows cookie-based session manager?
		
		// Check whether session was required
		if (this.requireSession) {
			// Don't create a session if none exists
			HttpSession session = request.getSession(false);
			if (session == null) {
				throw new ServletException("This resource requires a pre-existing HttpSession: none was found"); 
			}
		}
		
		
		// Do declarative cache control
		if (this.cacheSeconds == 0) {
			preventCaching(response);
		}
		else if (this.cacheSeconds > 0) {
			// Revalidate only if we understand last modification
			boolean revalidate = this instanceof LastModified;
			cacheForSeconds(response, this.cacheSeconds, revalidate);
		}
		// Leave caching to the client otherwise
		
		// If everything's OK, leave subclass to do the business
		return handleRequestInternal(request, response);
	}
	
	
	/**
	 * Subclasses must implement this. The contract is
	 * the same as for handleRequest.
	 */
	protected abstract ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;


}	// class AbstractController
