
package com.interface21.web.servlet;

import javax.servlet.http.HttpServletRequest;


/**
 * Same contract as for Servlet
 * Any resource can implement this. 
 */
public interface LastModified {
	
	/**
	 * Same contract as for Servlet.getLastModified
	 * Invoked <b>before</b> request processing.
	 */
	long getLastModified(HttpServletRequest request);
	

}
