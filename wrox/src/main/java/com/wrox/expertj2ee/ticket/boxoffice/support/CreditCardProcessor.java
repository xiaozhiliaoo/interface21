
package com.wrox.expertj2ee.ticket.boxoffice.support;

import java.util.Random;

import com.wrox.expertj2ee.ticket.boxoffice.*;

public interface CreditCardProcessor  {
	
	
	/** Return a confirmation code
	 */
	String process(double amount, String creditCardNumber, String expiry) throws CreditCardAuthorizationException;
	 
	
	/**
	 * Returns if valid
	 * @throw if invalid
	 */
	void validate(String creditCardNumber, String expiry) throws InvalidCreditCardNumberException;
	
}
