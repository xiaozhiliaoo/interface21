/*
 * ErrorCoded.java
 *
 * Created on 2 September 2001, 19:22
 */

package com.interface21.core;

/**
 * Interface that can be implemented by exceptions etc. that
 * are error coded. The error code is a String, rather than a number,
 * so it can be given user-readable values, such as "object.failureDescription".
 * @author  Rod Johnson
 * @version $Id: ErrorCoded.java,v 1.1.1.1 2002/08/01 12:52:03 Rod Johnson Exp $
 */
public interface ErrorCoded {
	
	/** Constant to indicate that this failure isn't coded */
	public static final String UNCODED = "uncoded";
	
	/** Return the error code associated with this failure. 
	 * The GUI can render this anyway it pleases, allowing for Int8ln etc.
	 * @return a String error code associated with this failure
	 */
	String getErrorCode();

}

