package com.interface21.web.servlet.view.xslt;

import com.interface21.context.ApplicationContextException;
import com.interface21.web.servlet.view.AbstractView;
import com.interface21.web.servlet.view.RequestInfo;
import org.infohazard.domify2.DOMAdapter;
import org.w3c.dom.Node;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;

// URI Resolver!? bean reference? to allow app to set in applicationContext.xml?

//  VIEWS can SEE BEAN FACTORY (if they implemt ACA, like Abstract superclass
// SEPARATE

// each model object is its own document, lazily instantiated?

/**
 * View using an XSLT stylesheet.
 * Uses Domify to convert to an XML document if necessary
 * Depends on domify.jar.
 * <li>Properties:
 * <li>stylesheet: no transform is null
 * <li>uriResolver:
 * <li>cache (optional, default=false): debug setting only. Setting this
 * to true will cause the templates object to be reloaded for each rendering.
 * This is useful during development, but will seriously affect performance in
 * production and isn't threadsafe.
 * <li>excludedProperties (optional): 
 * root
 * @author  Rod Johnson
 * @version $RevisionId$
 */
public class XsltView extends AbstractView {
	
	public static final String REQUEST_INFO_KEY = "request-info"; 


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	 * XSLT Template
	 */
	private Templates templates;

	private TransformerFactory transformerFactory;

	/** Custom URIResolver, set by subclass or as bean property */
	private URIResolver uriResolver;

	/**
	 * URL of stylesheet
	 */
	private String url;

	private String root;
	
	private boolean cache = true;
	
	private DOMAdapter domAdapter;
	

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates new XsltView */
	public XsltView() {
		this.domAdapter = new DOMAdapter();
	}

	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	/** Set the URL of the XSLT stylesheet
	 * @param url the URL of the XSLT stylesheet
	 */
	public final void setStylesheet(String url) {
		this.url = url;
	}

	/** Document root element name. Only
	 * used if we're not passed a Node. OR HAVE MULTIPLE?
	 * @param root document root element name
	 */
	public final void setRoot(String root) {
		this.root = root;
	}

	/**
	 * Set the URIResolver used in the transform. The
	 * URIResolver handles calls to the XSLT document()
	 * function.
	 * This method can be used by subclasses or as a bean property
	 * @param uriResolver URIResolver to set. No URIResolver
	 * will be set if this is null (this is the default).
	 */
	public final void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}
	
	/**
	 * Gets the cache.
	 * @return Returns a boolean
	 */
	public final boolean getCache() {
		return cache;
	}

	/**
	 * Sets the cache.
	 * @param cache The cache to set
	 */
	public final void setCache(boolean cache) {
		this.cache = cache;
	}
	
	
	/**
	 * Public static to allow use by other classes
	 */
	public static void setExcludedProperties(String[] excluded, DOMAdapter pDomAdapter) throws ClassNotFoundException {
		for (int i = 0; i < excluded.length; i++) {
			int lastDotPos = excluded[i].lastIndexOf(".");
			String className = excluded[i].substring(0, lastDotPos);
			String property = excluded[i].substring(lastDotPos + 1);
			//logger.info("Will exclude property '" + property + "' on class '" + className + "'");
			pDomAdapter.ignoreProperty(Class.forName(className), property);
		}
	}
	
		// PRovides list-based exclusion
	//com.interface21.MyClass.age,XXXX
	public final void setExcludedProperties(String[] excluded) throws ClassNotFoundException {
		setExcludedProperties(excluded, this.domAdapter);
	}
	

	//---------------------------------------------------------------------
	// Implementation of ApplicationContextAware
	//---------------------------------------------------------------------
	/** Set the ApplicationContext object used by this object.
	 * Here is also where we load our template, as we need the
	 * Application Context to do it
	 * @param ctx ApplicationContext object used by this object
	 * @param namespace namespace this object is in: null means default namespace
	 * @throws ApplicationContextException if initialization attempted by this object
	 * after it has access to the WebApplicatinContext fails
	 */
	protected final void onSetContext() throws ApplicationContextException {
		this.transformerFactory = TransformerFactory.newInstance();
		
		if (this.uriResolver != null) {
			logger.config("Using custom URIResolver [" + this.uriResolver + "] in XsltView with name '" + getName() + "'");
			this.transformerFactory.setURIResolver(this.uriResolver);
		}
		logger.fine("Url in view is " + url);
		cacheTemplates();
		configureDomAdapter(this.domAdapter);
	}	
	

	
	/**
	 * Subclasses can override this to eliminate cycles etc.
	 */
	protected void configureDomAdapter(DOMAdapter domAdapter) {
		// Empty
	}
	
	private void cacheTemplates() {
		if (url != null && !"".equals(url)) {
			Source s = getStylesheetSource(url);
			try {
				this.templates = transformerFactory.newTemplates(s);
				logger.config("Loaded templates " + templates + " in XsltView with name '" + getName() + "'");
			}
			catch (TransformerConfigurationException ex) {
				throw new ApplicationContextException(
					"Can't load stylesheet at '" + url + "' in XsltView with name '" + getName() + "'",
					ex);
			}
		}
	}	// onSetContext

	/** 
	 * Load the stylesheet.
	 * This implementation uses getRealPath().
	 * Subclasses can override this method to avoid any container
	 * restrictions on use of this slightly questionable method.
	 * However, when it does work it's efficient and convenient.
	 */
	protected Source getStylesheetSource(String url) throws ApplicationContextException {
		// Shouldn't use this: it's not guaranteed
		// QUESTIONABLE: Servlet 2.2 idea!?
		// TODO
		logger.info("Loading XSLT stylesheet '" + url + "' from filesystem using getRealPath");
		String realpath = getWebApplicationContext().getServletContext().getRealPath(url);
		if (realpath == null)
			throw new ApplicationContextException(
				"Can't resolve real path for XSLT stylesheet at '" + url + "'; probably results from container restriction: override XsltView.getStylesheetSource() to use an alternative approach to getRealPath()");
		logger.config("Realpath is '" + realpath + "'");

		Source s = new StreamSource(new File(realpath));
		return s;
	}	// getStylesheetSource
	
	

	//---------------------------------------------------------------------
	// Implementation of protected abstract methods
	//---------------------------------------------------------------------
	/**
	 * Renders the view given the specified model.  There can be many types of
	 * view.<br/>
	 * The first take will be preparing the request: this may include setting the model
	 * as an attribute, in the case of a JSP view.
	 */
	protected final void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		if (!this.cache) {
			logger.warning("DEBUG SETTING: NOT THREADSAFE AND WILL IMPAIR PERFORMANCE: template will be refreshed");
			cacheTemplates();
		}

		if (this.templates == null) {
			if (this.transformerFactory == null)
				throw new ServletException("View is incorrectly configured. Templates AND TransformerFactory are null");

			logger.warning("XSLTView is not configured: will copy XML input");
			response.setContentType("text/xml; charset=ISO-8859-1");
		}
		else {
			// Normal case
			response.setContentType(getContentType());
		}

		if (model == null)
			throw new ServletException("Cannot do XSLT transform on null model");

		Node dom = null;
		String docRoot = null;

		//System.out.println("INitializing XML: view with hc=" + hashCode());
		// Value of a single element in the map, if there is one
		Object singleModel = null;

		if (model.size() == 1) {
			docRoot = (String) model.keySet().iterator().next();
			singleModel = model.get(docRoot);
		}

		// Handle special case when we have a single node
		if (singleModel != null && (singleModel instanceof Node)) {
			// Don't domify if the model is already an XML node
			// We don't need to worry about model name, either:
			// we leave the Node alone
			logger.fine("No need to domify: was passed an XML node");
			dom = (Node) singleModel;
		}
		else {
			if (this.root == null && docRoot == null)
				throw new ServletException(
					"Cannot domify multiple non-Node objects without a root element name in XSLT view with name='" + getName() + "'");

			// docRoot local variable takes precedence
			try {
				addRequestInfoToModel(model, request);
				dom = createDomNodeToTransform(model, (docRoot == null) ? this.root : docRoot);
			}
			catch (RuntimeException rex) {
				// Domify throws unchecked exceptions
				throw new ServletException("Error domifying model in XSLT view with name='" + getName() + "'", rex);
			}
		}

		doTransform(response, dom);
	}   // renderMergedOutputModel
	
	
	/**
	 * Return the XML node to transform.
	 * Subclasses can override this. The default implementation uses
	 * Domify
	 */
	protected Node createDomNodeToTransform(Map model, String root) {
		return this.domAdapter.adapt(model, root);
	}
	 
	protected void addRequestInfoToModel(Map model, HttpServletRequest request) {
		model.put(REQUEST_INFO_KEY, new RequestInfo(request));
	}
	

	/**
	 * Use TrAX to perform the transform
	 */
	protected void doTransform(HttpServletResponse response, Node dom) throws IllegalArgumentException, IOException, ServletException {
		try {
			Transformer trans = (this.templates != null) ? this.templates.newTransformer() : // we have a stylesheet
						transformerFactory.newTransformer(); // just a copy
		
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			trans.transform(new DOMSource(dom), new StreamResult(new BufferedOutputStream(response.getOutputStream())));
		
			logger.fine("XSLT transformed OK with stylesheet '" + url + "'");
		}
		catch (TransformerConfigurationException ex) {
			//Category.getInstance(getClass()).error("Couldn't dispatch to JSP with url '" + getUrl() + "' defined in view with name '" + cr.getViewName() + "': " + ex, ex);
			throw new ServletException(
				"Couldn't create XSLT transformer for stylesheet '" + url + "' in XSLT view with name='" + getName() + "'",
				ex);
		}
		catch (TransformerException ex) {
			//Category.getInstance(getClass()).error("Couldn't dispatch to JSP with url '" + getUrl() + "' defined in view with name '" + cr.getViewName() + "': " + ex, ex);
			throw new ServletException(
				"Couldn't perform transform with stylesheet '" + url + "' in XSLT view with name='" + getName() + "'",
				ex);
		}
	} 

} 	// XsltView