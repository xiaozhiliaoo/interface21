
package com.interface21.web.tags;

import com.interface21.util.StringUtils;
import com.interface21.web.servlet.view.xslt.XsltView;
import org.infohazard.domify2.DOMAdapter;
import org.w3c.dom.Node;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

public class DomifyTag extends TagSupport {

	private Object model;
	
	private DOMAdapter domAdapter;
	
	private String root;
	
	public DomifyTag() {
		this.domAdapter = new DOMAdapter();
	}
	
	public void setModel(Object model) {
		this.model = model;
	}
	
	public void setRoot(String root) {
		this.root = root;
	}
	
	// CSV string
	public void setExcludedProperties(String excluded) throws ClassNotFoundException {
		String[] excludes = StringUtils.commaDelimitedListToStringArray(excluded);
		XsltView.setExcludedProperties(excludes, this.domAdapter);
	}

	/**
	 * @see Tag#doStartTag()
	 */
	public int doStartTag() throws JspException {
		
		Node node = domAdapter.adapt(this.model, this.root);
		pageContext.setAttribute("node", node, PageContext.REQUEST_SCOPE);
		
		return EVAL_BODY_INCLUDE;
	}

}
