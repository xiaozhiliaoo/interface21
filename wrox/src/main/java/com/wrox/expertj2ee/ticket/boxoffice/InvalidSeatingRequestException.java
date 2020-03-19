
package com.wrox.expertj2ee.ticket.boxoffice;

import com.wrox.expertj2ee.ticket.exceptions.ApplicationException;


public class InvalidSeatingRequestException extends ApplicationException {

	/**
	 * Constructor for InvalidSeatingRequestException.
	 * @param s
	 */
	public InvalidSeatingRequestException(String s) {
		super(s);
	}

	/**
	 * Constructor for InvalidSeatingRequestException.
	 * @param s
	 * @param ex
	 */
	public InvalidSeatingRequestException(String s, Throwable ex) {
		super(s, ex);
	}

	
	public String getErrorCode() {
		return "invalidSeatingRequest";
	}

}
