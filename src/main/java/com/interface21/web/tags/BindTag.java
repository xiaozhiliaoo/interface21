package com.interface21.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import com.interface21.validation.FieldError;

/** 
 * Enclosing tag for all bind tags
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class BindTag extends TagInWebApplicationContext {
	

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	private String name;
	
	private String property;
	
	private BindTagHelper helper;
	


	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------

	public void setValue(String value) throws JspTagException {
		int dotpos = value.indexOf(".");
		if (dotpos == -1)
			throw new JspTagException("Invalid value: no . in value '" + value + "'-- cannot identify bound object. " +
				"Value attribute must be of form <name>.<property>.");
		this.name = value.substring(0, dotpos);
		this.property = value.substring(dotpos + 1);
	}

	/**
	 * @see Tag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		super.doStartTag();
		this.helper = new BindTagHelper(pageContext, name, property);
		//pageContext.setAttribute("value", helper.getDisplayValue());
		
		// reassemble value
		String path = name + "." + property;
		
		BindStatus status = null;
		if (helper.hasError()) {
			FieldError fe = helper.getError();
			String resolvedMessage = resolveMessage(fe.getErrorCode(), fe.getMessage());
			status = new BindStatus(helper.getDisplayValue(), fe.getErrorCode(), resolvedMessage, path, helper.getSource());
		}
		else {
			status = new BindStatus(helper.getDisplayValue(),path, helper.getSource());
		}
		
		pageContext.setAttribute("bind", status);
		return EVAL_BODY_INCLUDE;
	}
	
	
	public int doEndTag() throws JspException {
		return EVAL_PAGE; 
	}


	
	protected BindTagHelper getHelper() {
		return this.helper;
	}

}