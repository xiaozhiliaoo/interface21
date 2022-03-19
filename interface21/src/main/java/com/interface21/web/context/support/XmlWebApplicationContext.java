
package com.interface21.web.context.support;

import com.interface21.context.ApplicationContext;
import com.interface21.context.support.AbstractXmlApplicationContext;
import com.interface21.web.context.WebApplicationContext;
import com.interface21.web.util.WebUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;


/**
 * Root WebApplicationContext: for an entire web application,
 * associated with a ServletContext.
 * @author  Rod Johnson
 * @version $Revision: 1.1.1.1 $
 */
public class XmlWebApplicationContext extends AbstractXmlApplicationContext implements WebApplicationContext {

	public static final String CONFIG_URL = "configUrl";

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** URL from which the configuration was loaded */
	private String url;

	private ServletContext servletContext;
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	public XmlWebApplicationContext() {
		setDisplayName("Root WebApplicationContext");
	}
	
	/** Create a new child WebApplicationContext
	 */
	public XmlWebApplicationContext(ApplicationContext parent, String namespace) {
		super(parent);
		this.url = namespace;
		setDisplayName("WebApplicationContext for namespace '" + namespace + "'");
	}

	//---------------------------------------------------------------------
	// Implementation of WebApplicationConfig
	//---------------------------------------------------------------------
	public void setServletContext(ServletContext servletContext) throws ServletException {
		this.servletContext = servletContext;
		if (this.getParent() == null) {
			String configURL = servletContext.getInitParameter(CONFIG_URL);
			if (configURL == null) {
				throw new ServletException("Cannot initialize context of " + getClass() + ": missing required context param with name '" + CONFIG_URL + "'");
			}
			this.url = configURL;
		}
		else {
			this.url =  "/WEB-INF/" + url + ".xml";
		}

		//try {
			refresh();
			if (this.getParent() == null) {
				WebApplicationContextUtils.configureConfigObjects(this);
				// Expose as a ServletContext object
				WebApplicationContextUtils.setAsContextAttribute(this);
			}
		//}
		//catch (ApplicationContextException ex) {
			// TODO nest properly
		//	throw new ServletException("Can't start application context at '" + this.url + "': "  + ex, ex);
		//}		
	}	// setServletContext
	

	public final ServletContext getServletContext() {
		return servletContext;
	}


	protected String getURL() {
		return url;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer( super.toString() + "; ");
		sb.append("config URL='" + url + "'; ");
		return sb.toString();
	}


	//---------------------------------------------------------------------
	// Implementation of superclass abstract methods
	//---------------------------------------------------------------------
	/**
	 * Open and return the input stream for the bean factory for this namespace. 
	 * If namespace is null, return the input stream for the default bean factory.
	 * @throw IOException if the required XML document isn't found
	 */
	protected InputStream getInputStreamForBeanFactory() throws IOException {
		String xmlFile = getURL();
		return WebUtils.getResourceInputStream(xmlFile, getServletContext());
	}

}	// class XmlWebApplicationContext
