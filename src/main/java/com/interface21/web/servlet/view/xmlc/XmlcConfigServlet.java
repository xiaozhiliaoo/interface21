package com.interface21.web.servlet.view.xmlc;

import com.interface21.web.servlet.HttpServletBean;
import java14.java.util.logging.Logger;
import org.enhydra.xml.xmlc.servlet.XMLCContext;

import javax.servlet.ServletException;

/**
 * Servlet to initialize Velocity
 * @author Rod Johnson
 * @version $RevisionId$
 * Must be a servlet, as the getContext() method requires
 * a servlet.
 * See documentation of XMLCContext class for servlet context-wide
 * parameters that configure XMLC.
 */
public class XmlcConfigServlet extends HttpServletBean {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	//---------------------------------------------------------------------
	// Overridden methods
	//---------------------------------------------------------------------
	/**
	 * Configure the log4j system and instantiate and bind the WebApplicationContext
	 * implementation as a ServletContext attribute
	 * @throws ServletException if startup fails
	 */
	protected void initServletBean() throws ServletException {

		// XMLCCContext will be added to ServletContext
		XMLCContext xmlcc = XMLCContext.getContext(this);

		logger.config("Added XMLCContext");
	} // init

	/** Return a description of this servlet
	 * @return a description of this servlet
	 */
	public String getServletInfo() {
		return "XMLCConfigServlet v 1.0";
	}

	//---------------------------------------------------------------------
	// Interface methods
	//---------------------------------------------------------------------
	//protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	//	log4jCategory.info("Showing status at " + STATUS_URL);
	//	request.setAttribute(WebApplicationContext.WEB_APPLICATION_CONTEXT_ATTRIBUTE_NAME, webApplicationContext);
	//	request.getRequestDispatcher(STATUS_URL).forward(request, response);
	//}

} // class ContextLoaderServlet