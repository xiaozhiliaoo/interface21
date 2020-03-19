
package com.wrox.expertj2ee.ticket.framework.ejb;

import com.wrox.expertj2ee.ticket.exceptions.FatalException;

public class EjbConnectionFailure extends FatalException {

	/**
	 * Constructor for EjbConnectionFailure.
	 * @param s
	 * @param ex
	 */
	public EjbConnectionFailure(String s, Throwable ex) {
		super(s, ex);
	}


}
