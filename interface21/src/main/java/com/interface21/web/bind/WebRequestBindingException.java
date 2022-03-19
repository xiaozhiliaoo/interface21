
package com.interface21.web.bind;

import javax.servlet.ServletException;


/**
 * Fatal binding exception
 */
public class WebRequestBindingException extends ServletException {

	/**
	 * Constructor for WebRequestBindingException.
	 * @param arg0
	 */
	public WebRequestBindingException(String s) {
		super(s);
	}

	/**
	 * Constructor for WebRequestBindingException.
	 * @param arg0
	 * @param arg1
	 */
	public WebRequestBindingException(String s, Throwable t) {
		super(s, t);
	}


}
