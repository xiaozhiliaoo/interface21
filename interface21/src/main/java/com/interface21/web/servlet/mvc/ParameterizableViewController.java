
package com.interface21.web.servlet.mvc;

import com.interface21.beans.factory.InitializingBean;
import com.interface21.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ParameterizableViewController extends AbstractController implements InitializingBean{
	
	private String successView;
	
	private String failureViewName;
	
	private String resubmitViewName;


	/* Requirre only success view name */
	public ParameterizableViewController() {
	}
	
	//public ParameterizableViewController(boolean requireFailureView, boolean requireErrorView) {
	//}
	
	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	/**
	 * Gets the viewName.
	 * Used in this class: other properties are for subclasses only.
	 * @return Returns a String
	 */
	public String getSuccessViewName() {
		return successView;
	}

	/**
	 * Sets the viewName.
	 * @param viewName The viewName to set
	 */
	public void setSuccessViewName(String viewName) {
		this.successView = viewName;
	}
	
	/**
	 * Gets the failureViewName.
	 * @return Returns a String
	 */
	public String getFailureViewName() {
		// We validate this property, like resubmitViewName,
		// only if asked for it by a subclass
		// NB: will produce warning message in log files...
		//if (failureViewName == null)
		//	throw new RuntimeException("failureViewName bean property must be set in " + getClass().getName());
		return failureViewName;
	}

	/**
	 * Sets the failureViewName.
	 * @param failureViewName The failureViewName to set
	 */
	public void setFailureViewName(String failureViewName) {
		this.failureViewName = failureViewName;
	}

	/**
	 * Gets the resubmitViewName.
	 * @return Returns a String
	 */
	public String getResubmitViewName() {
		//if (resubmitViewName == null)
		//	throw new RuntimeException("resubmitViewName bean property must be set in " + getClass().getName());
		return resubmitViewName;
	}

	/**
	 * Sets the resubmitViewName.
	 * @param resubmitViewName The resubmitViewName to set
	 */
	public void setResubmitViewName(String resubmitViewName) {
		this.resubmitViewName = resubmitViewName;
	}



	/**
	 * @see SimpleController#handleRequest(HttpServletRequest, HttpServletResponse)
	 */
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return new ModelAndView(this.successView);
	}
	
	/**
	 * Ensure at least successView is set!?
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (successView == null)
			throw new ServletException("viewName bean property must be set in " + getClass().getName());
	}

}
