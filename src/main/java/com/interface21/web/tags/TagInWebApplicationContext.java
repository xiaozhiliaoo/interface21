package com.interface21.web.tags;

import java.io.IOException;
import java14.java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import com.interface21.context.NoSuchMessageException;
import com.interface21.web.context.WebApplicationContext;
import com.interface21.web.context.support.WebApplicationContextUtils;

/**
 * Superclass for all tags that require an application context
 * @author Rod Johnson
 * @version $RevisionId$
 */
public abstract class TagInWebApplicationContext extends TagSupport {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	protected WebApplicationContext webApplicationContext;

	/**
	 * For message resolution
	 * MessageSource!?
	 * TODO: Shouldn't depend on it...
	 */
	public WebApplicationContext getWebApplicationContext() {
		return webApplicationContext;
	}

	public int doStartTag() throws JspException {
		try {
			this.webApplicationContext =
				WebApplicationContextUtils.getWebApplicationContext(pageContext.getRequest(), pageContext.getServletContext());
		}
		catch (ServletException ex) {
			throw new JspTagException("Can't find application context in BindTag");
		}
		return EVAL_BODY_INCLUDE;
	}

	public String resolveMessage(String name, String defaultVal) throws JspException {
		return webApplicationContext.getMessage(name, pageContext.getRequest().getLocale(), defaultVal);

	}

}