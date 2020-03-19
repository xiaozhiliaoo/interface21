
package com.wrox.expertj2ee.ticket.exceptions;

import com.interface21.core.ErrorCoded;
import com.interface21.core.NestedCheckedException;

public abstract class ApplicationException extends NestedCheckedException implements ErrorCoded {

	/**
	 * Constructor for ApplicationException.
	 * @param s
	 */
	public ApplicationException(String s) {
		super(s);
	}

	/**
	 * Constructor for ApplicationException.
	 * @param s
	 * @param ex
	 */
	public ApplicationException(String s, Throwable ex) {
		super(s, ex);
	}


}
