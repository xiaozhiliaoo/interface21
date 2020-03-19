package com.interface21.web.servlet.view;

import java.util.HashMap;
import java.util.Locale;

import javax.servlet.ServletException;

import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextAware;
import com.interface21.context.ApplicationContextException;
import com.interface21.web.servlet.View;
import com.interface21.web.servlet.ViewResolver;
import java14.java.util.logging.Logger;

/**
 * Superclass for view resolvers.
 * Caches Views once resolved. This means that
 * view resolution won't be a performance problem, no matter how costly
 * initial view retrieval is. View retrieval is deferred to subclasses.
 * @author  Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractCachingViewResolver implements ViewResolver {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** View name --> View instance */
	private HashMap viewHash = new HashMap();

	/** Logging category for this object */
	protected final Logger logger = Logger.getLogger(getClass().getName());

	/** ApplicationContext for this ViewResolver */
	private ApplicationContext applicationContext;


	/** Whether we should cache views, once resolved */
	private boolean cache = true;

	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	/**
	 * Disable caching only for debugging purposes.
	 * It's SLLLLOOOOOW. Tests indicate that turning caching off
	 * reduces performance by at least 20%. Increased object churn
	 * probably eventually makes the problem even worse.
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	public boolean getCache() {
		return this.cache;
	}

	//---------------------------------------------------------------------
	// Implementation of ViewResolver
	//---------------------------------------------------------------------
	/** Look in BeanFactory for View config objects
	 * COULD CACHE THESE: they should be threadsafe!?
	 */
	public final View resolveViewname(String viewname, Locale locale) throws ServletException {

		View v = null;
		if (!cache) {
			logger.warning("View caching is SWITCHED OFF -- DEVELOPMENT SETTING ONLY: this will severely impair performance");
			v = loadAndConfigureView(viewname, locale);
		}
		else {
			// We're caching
			// Don't really need synchronization
			v = (View) viewHash.get(viewname);
			if (v == null) {
				// Ask the subclass to load the View
				v = loadAndConfigureView(viewname, locale);
			}
		}

		return v;
	}	// resolveViewname


	/** Configure the given View. Only invoked once per View.
	 * Configuration means giving the View its name, and 
	 * setting the ApplicationContext on the View if necessary
	 */
	private View loadAndConfigureView(String viewname, Locale locale) throws ServletException {

		// Ask the subclass to load the view
		View v = loadView(viewname, locale);
		if (v == null)
			throw new ServletException("Cannot resolve view name '" + viewname + "'");
			
		// Configure view
		v.setName(viewname);

		// Give the view access to the ApplicationContext
		// if it needs it
		if (v instanceof ApplicationContextAware) {
			try {
				((ApplicationContextAware) v).setApplicationContext(this.applicationContext);
			}
			catch (ApplicationContextException ex) {
				throw new ServletException("Error initializing View [" + v + "]: " + ex.getMessage(), ex);
			}

			logger.info("Cached view with name '" + viewname + "'");
			viewHash.put(viewname, v);
		}

		return v;
	} // loadAndConfigureView


	/** Subclasses must implement this method. There need be no concern for efficiency,
	 * as this class will cache views.
	 * @param viewname name of the view to retrieve
	 * @param locale Locale to retrieve the view for. Not all subclasses may support
	 * internationalization. A subclass that doesn't can ignore this parameter.
	 * @throws ServetException if there is an error trying to resolve the view
	 * @return the View if it can be resolved; otherwise null.
	 */
	protected abstract View loadView(String viewname, Locale locale) throws ServletException;

	//---------------------------------------------------------------------
	// Implementation of ApplicationContextAware
	//---------------------------------------------------------------------
	/** Set the ApplicationContext object used by this object
	 * @param ctx ApplicationContext object used by this object
	 * @param namespace namespace this object is in: null means default namespace
	 * @throws ApplicationContextException if initialization attempted by this object
	 * after it has access to the WebApplicatinContext fails
	 */
	public final void setApplicationContext(ApplicationContext applicationContext)
		throws ApplicationContextException {
		this.applicationContext = applicationContext;
	}

	public final ApplicationContext getApplicationContext() {
		return this.applicationContext;
	}

}