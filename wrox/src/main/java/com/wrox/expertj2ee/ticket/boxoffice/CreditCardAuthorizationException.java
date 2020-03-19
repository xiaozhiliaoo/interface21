
package com.wrox.expertj2ee.ticket.boxoffice;

import com.wrox.expertj2ee.ticket.exceptions.ApplicationException;

public class CreditCardAuthorizationException extends ApplicationException {

	/**
	 * Constructor for CreditCardAuthorizationException.
	 * @param s
	 */
	public CreditCardAuthorizationException(String s) {
		super(s);
	}
	
	/**
	 * @see ErrorCoded#getErrorCode()
	 */
	public String getErrorCode() {
		return "creditCardAuthorizationFaile";
	}

}
