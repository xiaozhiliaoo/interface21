package com.interface21.web.servlet.view;

import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextAware;
import com.interface21.context.ApplicationContextException;
import com.interface21.web.context.WebApplicationContext;
import com.interface21.web.servlet.View;
import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;



/**
 * Abstract view superclass.
 * Implements ApplicationContextAware, which will be helpful
 * to some views. This means that the ApplicationContext
 * will be set by the framework during initialization.
 * <b/>Handles static attributes, and merging static with
 * dynamic attributes.
 * <br/>It's recommended that subclasses <b>don't</b> cache
 * anything, in the quest for efficiency. This class offers
 * caching. However, it's possible
 * to disable this class's caching, which is useful during development.
 * <br/>Also provides a log4j logging category.
 * @author  Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractView implements View, ApplicationContextAware {

	/** Log4j Category for this object */
	protected final Logger logger = Logger.getLogger(getClass().getName());

	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** Map of static attributes, keyed by attribute name (String) */
	private HashMap	staticAttributes = new HashMap();

	/** The ApplicationContext passed to this object */
	private WebApplicationContext webApplicationContext;

	
	private String contentType = "text/html; charset=ISO-8859-1";
	
	/** The name by which this View is known */
	private String name;



	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	
	/** 
	 * 
	 * Format is attname0={value1},attname1={value1}
	 */
	public final void setAttributesCSV(String s) throws IllegalArgumentException {
		StringTokenizer st = new StringTokenizer(s, ",");
		while (st.hasMoreTokens()) {
			String tok = st.nextToken();
			int eqindx = tok.indexOf("=");
			if (eqindx == -1)
				throw new IllegalArgumentException("Expected = in View string '" + s + "'");
			// LENGTH CHECKS
			String name = tok.substring(0, eqindx);
			String val = tok.substring(eqindx + 1);
			
			// Delete first and last characters of value: { and }
			val = val.substring(1);
			val = val.substring(0, val.length() - 1);

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Set static attribute with name '" + name + "' and value [" + val + "] on view");//with name '" + viewname + "'");
			}
			addStaticAttribute(name, val);
		}
	}	// setAttributesCSV
	
	
	/**
	 * Relies on registration of properties PropertyEditor
	 */
	public final void setAttributes(Properties p) throws IllegalArgumentException {
		if (p != null) {
			Iterator itr = p.keySet().iterator();
			while (itr.hasNext()) {
				String name = (String) itr.next();
				String val = p.getProperty(name);
				if (logger.isLoggable(Level.FINE)) {
					logger.info("Set static attribute with name '" + name + "' and value [" + val + "] on view");//with name '" + viewname + "'");
				}
				addStaticAttribute(name, val);
			}
		}
	}	// setAttributes
	
	public final void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public final String getContentType() {
		return this.contentType;
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContextAware
	//---------------------------------------------------------------------
	/** Set the ApplicationContext object used by this object.
	 * @param ctx ApplicationContext object used by this object.
	 * This must be of type WebApplicatinContext
	 * @throws ApplicationContextException if the ApplicationContext
	 * isn't of type WebApplicatinContext.
	 */
	public final void setApplicationContext(ApplicationContext applicationContext) throws ApplicationContextException {
		
		if (this.webApplicationContext != null)
			throw new RuntimeException("Assertion failed: View with name '" + name + "' (set previously) cannot be initialized twice");
		
		if (!(applicationContext instanceof WebApplicationContext))
			throw new ApplicationContextException("AbstractView requires a WebApplicationContext. " + applicationContext + " is not acceptable");
		this.webApplicationContext = (WebApplicationContext) applicationContext;
		
		// Call subclass initialization
		onSetContext();
	}
	
	/** Subclasses may implement this to perform their own
	 * initialization. It's invoked after the ApplicationContext
	 * has been set.
	 * <br/>This implementation does nothing.
	 * @throws ApplicationContextException if subclass initialization fails.
	 */
	protected void onSetContext() throws ApplicationContextException {
	}


	public final ApplicationContext getApplicationContext() {
		return this.webApplicationContext;
	}
	
	protected final WebApplicationContext getWebApplicationContext() {
		return this.webApplicationContext;
	}
	
	

	//---------------------------------------------------------------------
	// Implementation of View
	//---------------------------------------------------------------------
	/** Add static data to this view, exposed in each view.
	 * <br/>Must be invoked before any calls to render().
	 * @param name name of attribute to expose
	 * @param o object to expose
	 */
	public final void addStaticAttribute(String name, Object o) {
		this.staticAttributes.put(name, o);
	}

	/** Handy for testing. 
	 * @return the static attributes in this view
	 */
	public final Map getStaticAttributes() {
		return Collections.unmodifiableMap(this.staticAttributes);
	}

	/**
	 * Set the view's name. Helpful for traceability.
	 * Framework code must call this when constructing views.
	 * @param name the view's name. May not be null.
	 * Views should use this for log messages.
	 */
	public final void setName(String name) {
		this.name = name;
	}
	
	/** Return the view's name. Should
	 * never be null, if the view was correctly configured.
	 * @return the view's name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Renders the view given the specified model.  There can be many types of
	 * view.<br/>
	 * The first take will be preparing the request: this may include setting the model
	 * as an attribute, in the case of a JSP view.
	 */
	public final void render(Map pModel, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isLoggable(Level.FINE))
			logger.fine("Rendering view with name '" + this.name + " with model={" + pModel + 
				"} and static attributes={" + this.staticAttributes + "}");
		
		// Consolidate static and dynamic model attributes
		Map model = new HashMap(this.staticAttributes);
		model.putAll(pModel);
		
		renderMergedOutputModel(model, request, response);
	}	// render
	
	
	/** Subclasses must implement this method. 
	 * Render the view given the model to output.
	 * @param model combined output Map, with dynamic values
	 * taking precedence over static attributes
	 * @param request HttpServetRequest
	 * @param response HttpServletResponse
	 * @throws IOException if there is an IO exception trying to obtain
	 * or render the view
	 * @throws ServletException if there is any other error
	 */
	protected abstract void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}	// AbstractView
