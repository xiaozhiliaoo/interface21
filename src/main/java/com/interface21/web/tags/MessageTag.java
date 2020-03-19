package com.interface21.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Enclosing tag for all bind tags
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class MessageTag extends TagInWebApplicationContext {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	private String name;
	
	public void setCode(String name) {
		this.name = name;
	}


	public int doEndTag() throws JspException {
		try {
			pageContext.getOut().println(resolveMessage(name, name));
		}
		catch (IOException ex) {
			throw new JspTagException("Can't write message");
		}
		return EVAL_PAGE;
	}


}