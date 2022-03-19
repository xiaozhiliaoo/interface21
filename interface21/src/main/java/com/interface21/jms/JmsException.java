
package com.interface21.jms;

import com.interface21.core.NestedRuntimeException;

public class JmsException extends NestedRuntimeException {

	

	/**
	 * Constructor for JmsException.
	 * @param s
	 * @param ex
	 */
	public JmsException(String s, Throwable ex) {
		super(s, ex);
	}


}
