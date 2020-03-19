
package com.wrox.expertj2ee.ticket.exceptions;

import com.interface21.core.NestedRuntimeException;


/**
 * Superclass for fatal ticket exceptions that shouldn't happen
 */
public abstract class FatalException extends NestedRuntimeException {


	/**
	 * Constructor for FatalException.
	 * @param s
	 * @param ex
	 */
	public FatalException(String s, Throwable ex) {
		super(s, ex);
	}


	/**
	 * Constructor for FatalException.
	 * @param arg0
	 */
	public FatalException(String arg0) {
		super(arg0);
	}

}
