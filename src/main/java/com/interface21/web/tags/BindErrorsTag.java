package com.interface21.web.tags;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.TagSupport;

import com.interface21.validation.BindException;
import com.interface21.validation.Errors;

/** 
 * Evaluates content if there are errors
 * @author Rod Johnson
 * @version $RevisionId$
 */
public class BindErrorsTag extends BodyTagSupport {

	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------

	public int doStartTag() throws JspException {

		Errors errors = (Errors) pageContext.getAttribute(BindException.EXCEPTION_KEY, PageContext.REQUEST_SCOPE);

		if (errors != null && errors.hasErrors()) {

			Integer count = new Integer(errors.getErrorCount());
			pageContext.setAttribute("count", count, PageContext.REQUEST_SCOPE);
			
			//TODO: ********* Add collection of errors:
			// ORDERING!??? must take it from ordering
			// of get properties on the page...
			
			return EVAL_BODY_BUFFERED;
			//return EVAL_BODY_TAG;
		}
		else {
			//logger.fine("Found no error for property '" + property + "': skipping tag body");
			return SKIP_BODY;
		}
	}

	public int doEndTag() throws JspException {
		try {
			BodyContent bodyContent = getBodyContent();
			if (bodyContent != null) {
				bodyContent.writeOut(bodyContent.getEnclosingWriter());
			}
		}
		catch (IOException ex) {
			// Old constructor
			throw new JspException("IO Error: " + ex);
		}
		return EVAL_PAGE;
	} // doEndTag

}