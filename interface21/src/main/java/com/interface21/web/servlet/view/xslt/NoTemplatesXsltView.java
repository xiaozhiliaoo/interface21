package com.interface21.web.servlet.view.xslt;

import com.interface21.context.ApplicationContextException;
import com.interface21.web.servlet.view.AbstractView;
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



/**
 * View using an XSLT stylesheet.
 * Uses Domify to convert to an XML document if necessary
 * Depends on domify.jar.
 * @author  Rod Johnson
 * @version $RevisionId$
 */
public class NoTemplatesXsltView extends AbstractView {


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	private TransformerFactory transformerFactory;

	private String 	url;

	/**
	 * Is this threadsafe!?
	 */
	private DOMAdapter domAdapter;

	private String root;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates new VelocityView */
    public NoTemplatesXsltView() {
    	this.domAdapter = new DOMAdapter();
    }


	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	/** Set the URL of the XSLT stylesheet
	 * @param url the URL of the XSLT stylesheet
	 */
	public void setStylesheet(String url) {
		this.url = url;
	}

	/** Document root element name. Only
	 * used if we're not passed a Node. OR HAVE MULTIPLE?
	 * @param root document root element name
	 */
	public void setRoot(String root) {
		this.root = root;
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
	protected void onSetContext() throws ApplicationContextException {

		this.transformerFactory = TransformerFactory.newInstance();
		
		System.out.println("URL IN VIEW IS " + url);
	}
	
	private Transformer getTransformer() throws ServletException, TransformerConfigurationException {
		if (url != null && !"".equals(url)) {

			// Shouldn't use this: it's not guaranteed
			// TODO
	 		String realpath = getWebApplicationContext().getServletContext().getRealPath(url);
			if (realpath == null) {
				throw new ServletException("Can't resolve real path for XSLT stylesheet at '" + url + "'");
			}
			logger.info("Realpath is '" + realpath + "'");


			Source s = new StreamSource(new File(realpath));												
				return transformerFactory.newTransformer(s);

		}	
		return transformerFactory.newTransformer();		
	}	// onSetContext



	//---------------------------------------------------------------------
	// Implementation of View
	//---------------------------------------------------------------------
	/**
	 * Renders the view given the specified model.  There can be many types of
	 * view.<br/>
	 * The first take will be preparing the request: this may include setting the model
	 * as an attribute, in the case of a JSP view.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		
			// Normal case
			response.setContentType(getContentType());

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
		if (singleModel != null && (singleModel instanceof Node) ) {
			// Don't domify if the model is already an XML node
			// We don't need to worry about model name, either:
			// we leave the Node along
			dom = (Node) singleModel;
		}
		else {
			if (this.root == null && docRoot == null)
				throw new ServletException("Cannot domify multiple non-Node objects without a root element name in XSLT view with name='" + getName() + "'");

			// docRoot local variable takes precedence
			try {
				dom = domAdapter.adapt(model, (docRoot == null) ? this.root : docRoot);
			} 
			catch (RuntimeException rex) {
				throw new ServletException("Error domifying model in XSLT view with name='" + getName() + "'", rex);
			}
		}

		// Now do transform
		try {
			Transformer trans = getTransformer();	// just a copy

			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			trans.transform(new DOMSource(dom), new StreamResult(new BufferedOutputStream(response.getOutputStream())));

			logger.fine("XSLT transformed OK with stylesheet '" + url + "'");

		}
		catch (TransformerConfigurationException ex) {
			//Category.getInstance(getClass()).error("Couldn't dispatch to JSP with url '" + getUrl() + "' defined in view with name '" + cr.getViewName() + "': " + ex, ex);
			throw new ServletException("Couldn't create XSLT transformer for stylesheet '" + url + "' in XSLT view with name='" + getName() + "'", ex);
		}
		catch (TransformerException ex) {
			//Category.getInstance(getClass()).error("Couldn't dispatch to JSP with url '" + getUrl() + "' defined in view with name '" + cr.getViewName() + "': " + ex, ex);
			throw new ServletException("Couldn't perform transform with stylesheet '" + url + "' in XSLT view with name='" + getName() + "'", ex);
		}
	}	// render

}	// XSLTView
