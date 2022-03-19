package com.interface21.web.tags;

import java.io.IOException;
import java14.java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.BeansException;
import com.interface21.validation.*;
import com.interface21.validation.BindException;
import com.interface21.validation.FieldError;
import com.interface21.web.context.WebApplicationContext;
import com.interface21.web.context.support.WebApplicationContextUtils;
import com.interface21.web.servlet.ControllerServlet;

/** 
 * Can't model as a tag superclass as we need to use in body tags and
 * normal tags
 * @author Rod Johnson
 * @version $RevisionId$
 */
class BindTagHelper {
	
	private static final String BW_PREFIX = "com.interface21.web.tags.BindTagHelper_BeanWrapper_";
	
	public static final int FROM_BEAN = 0;
	
	public static final int FROM_ERROR = 1;
	
	public static final int BLANK = 2;
	
	/** Handy to display sources */
	public static final String[] SRC_NAMES = { "from bean", "from error", "blank" };

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	private String name;
	
	private String property;
	
	private BeanWrapper beanWrapper;
	
	private Errors errors;
	
	private PageContext pageContext;
	
	/** Cached for efficiency */
	private String displayValue;
	
	/** One of the 3 constants defined above */
	private int source;


	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	public BindTagHelper(PageContext pageContext, String name, String property) throws JspException {
		this.pageContext = pageContext;
	
		this.property = property;
		this.name = name;

		logger.fine("Looking for error attribute with name '" + BindException.EXCEPTION_KEY + "'");
		this.errors = (Errors) pageContext.getAttribute(BindException.EXCEPTION_KEY, PageContext.REQUEST_SCOPE);
		if (errors == null) {
			String bwRequestAttributeName = BW_PREFIX + name;
			logger.fine("No Errors found: looking for cached BeanWrapper with name '" + bwRequestAttributeName + "'");
			
			// Looked for cached beanwrapper
			this.beanWrapper = (BeanWrapper) pageContext.getAttribute(bwRequestAttributeName, PageContext.REQUEST_SCOPE);
			if (beanWrapper == null) {
				
				// Create a bean wrapper and cache it
				Object obj = pageContext.getAttribute(name, PageContext.REQUEST_SCOPE);
				if (obj == null) {
					//throw new JspTagException("Can't find object with name '" + 
					// ALL EMPTY
					logger.info("No Errors found, and no object in scope. Using the empty String");
				}
				else {
					// We have an object. Create a bean wrapper and cache it
					this.beanWrapper = new BeanWrapperImpl(obj);
					pageContext.setAttribute(bwRequestAttributeName, beanWrapper, PageContext.REQUEST_SCOPE);
					logger.fine("Created new BeanWrapper and cached in request with name '" + bwRequestAttributeName + "'");
				}
			}
		}	// if no errors		
		cacheDisplayValue();
	} 	// setModel
	
	
	

	protected boolean hasErrors() {
		return errors != null && errors.hasErrors();
	}

	protected boolean hasError() {
		return hasErrors() && errors.getError(this.name, this.property) != null;
	}
	

	

	/** Value or rejected update
	 */
	public String getDisplayValue() throws JspException {
		return this.displayValue;
	}
	
	/**
	 * One of the constants in this class
	 */
	public int getSource() {
		return this.source;
	}
	
	private void cacheDisplayValue() throws JspException {
		Object val = null;
		if (this.errors != null) {
			val = this.errors.getPropertyValueOrRejectedUpdate(this.name, this.property);
			this.source = hasError() ? FROM_ERROR : FROM_BEAN;
		}
		else if (this.beanWrapper != null) {
			// Get from BeanWrapper
			val = this.beanWrapper.getPropertyValue(this.property);
			this.source = FROM_BEAN;
		}
		else {
			this.source = BLANK;
			logger.warning("No object in scope: using default value");
		}
		
		// Use placeholder for null
		if (val == null)
			val = "";
		
		this.displayValue = val.toString();
	}
	
	

	/** Exception or null */
	protected FieldError getError() {
		if (errors == null)
			return null;
		// May still return null...
		return errors.getError(this.name, this.property);
	}

}