
package com.wrox.expertj2ee.ticket.boxoffice;

public class InvalidCreditCardNumberException extends CreditCardAuthorizationException {

	/**
	 * Constructor for InvalidCreditCardNumberException.
	 * @param s
	 */
	public InvalidCreditCardNumberException(String s) {
		super(s);
	}

}
