package com.interface21.web.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.interface21.util.StringUtils;


public class DeletionTag extends BodyTagSupport {
	
	private String victims = "\n\t";
	
	public void setVictims(String victims) {
		this.victims = victims;
	}

	public int doEndTag() throws JspException {
		try {
			BodyContent bodyContent = getBodyContent();
			if (bodyContent != null) {
				//bodyContent.writeOut(bodyContent.getEnclosingWriter());
				String content = bodyContent.getString();
				String cleaned = StringUtils.deleteAny(content, this.victims);
				
				bodyContent.getEnclosingWriter().write(cleaned);
			}
		}
		catch (IOException ex) {
			// Old constructor
			throw new JspException("IO Error: " + ex);
		}
		return EVAL_PAGE;
	} // doEndTag

}