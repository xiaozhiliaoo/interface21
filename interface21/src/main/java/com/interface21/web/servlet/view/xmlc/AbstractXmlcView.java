package com.interface21.web.servlet.view.xmlc;

import com.interface21.context.ApplicationContextException;
import com.interface21.web.servlet.view.AbstractView;
import org.enhydra.xml.xmlc.XMLObject;
import org.enhydra.xml.xmlc.servlet.XMLCContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;



/**
 * Convenient superclass for XMLC views
 * Requires the XMLCConfig servlet in this package to have
 * been run before any views can successfully be constructed.
 * @author Rod Johnson
 * @revision $RevisionId$
 */
public abstract class AbstractXmlcView extends AbstractView {
	
	private XMLCContext xmlcContext;
	
	/** This method is invoked after the ApplicationContext
	 * has been set.
	 * @throws ApplicationContextException if subclass initialization fails.
	 */
	protected void onSetContext() throws ApplicationContextException {
		this.xmlcContext = (XMLCContext) getWebApplicationContext().getServletContext().getAttribute(XMLCContext.CONTEXT_ATTRIBUTE);
		if (this.xmlcContext == null)
			throw new ApplicationContextException("No XMLCContext inited. Use XMLCConfigServlet with loadOnStartup");
	}


	/**
	 * @see AbstractView#renderMergedOutputModel(Map, HttpServletRequest, HttpServletResponse)
	 */
	protected final void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		XMLObject xo = createXMLObject(model, request, response, this.xmlcContext);
		response.setContentType(getContentType());
		
		// Do some logging?
		
		// Send HTML back to the client.
  		this.xmlcContext.writeDOM(request, response, xo);

	}
	
	protected abstract XMLObject createXMLObject(Map model, HttpServletRequest request, HttpServletResponse response, XMLCContext context) throws ServletException;

}

