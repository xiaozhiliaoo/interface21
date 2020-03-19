package com.interface21.web.servlet.view;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.web.servlet.View;


/**
 *
 * @author  Rod Johnson	
 * @version $Revision: 1.1.1.1 $
 */
public class RedirectView extends AbstractView {
	
	private String url;

	/** Creates new JSPView */
    public RedirectView() {
    }
	 
	public RedirectView(String url) {
		setUrl(url);
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() { 
		return url;
	}
	
	
	/**
	 * Subclasses can override this method to return name-value pairs for query strings,
	 * which will be URLEncoded and formatted by this class.
	 * This implementation tries to stringify all model elements.
	 */
	protected Properties queryProperties(Map model) {
		Properties p = new Properties();
		return p;
	}


	/**
	 * Renders the view given the specified model.  There can be many types of
	 * view.<br/>
	 * The first take will be preparing the request: this may include setting the model
	 * as an attribute, in the case of a JSP view.
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if (getUrl() == null)
			throw new ServletException("RedirectView is not configured: URL cannot be null");
		
		// LOOK AT MAVERICK 2.0 redirect view: copy params
		// *?
		
		// BUILD Query String from query attributes
		
		response.sendRedirect(response.encodeRedirectURL(getUrl()));
	}
	
}
