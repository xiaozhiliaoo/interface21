
package com.wrox.expertj2ee.ticket.boxoffice.support;

import java.util.Random;

import com.wrox.expertj2ee.ticket.boxoffice.*;


/**
 * Simply delays
 */
public class DummyCreditCardProcessor implements CreditCardProcessor {
	
	// Reject card numbers containing this
	public static String failureDigits = "666";
	
	private Random rand = new Random();
	
	
	/** Creates new CreditCardProcessor */
	public DummyCreditCardProcessor() {
	}
	
	/** Return a confirmation code
	 */
	public String process(double amount, String creditCardNumber, String expiry) throws CreditCardAuthorizationException {
		
		validate(creditCardNumber, expiry);
		
		// Pretend to delay processing...
		// Of course this isn't really legit in the EJB tier
		try {
			Thread.sleep(Math.abs(rand.nextLong() % 1500L));
		}
		catch (InterruptedException ex) {
		}
	
		
		if (creditCardNumber.indexOf(failureDigits) != -1) {
			throw new CreditCardAuthorizationException("Transaction declined due to bad cad number '"  + creditCardNumber + "'");
		}
		
		return "AZ" + System.currentTimeMillis();
	}
	 
	
	public void validate(String creditCardNumber, String expiry) throws InvalidCreditCardNumberException {
		if (creditCardNumber == null || expiry == null)
			throw new InvalidCreditCardNumberException("Missing card number or expiry");
		if (creditCardNumber.length() != 16 || expiry.length() != 4)
			throw new InvalidCreditCardNumberException("Card number of expiry length invalid");
		for (int i = 0; i < creditCardNumber.length(); i++)
			if (!Character.isDigit(creditCardNumber.charAt(i)))
				throw new InvalidCreditCardNumberException("Non-numeric character in card number");
		for (int i = 0; i < expiry.length(); i++)
			if (!Character.isDigit(expiry.charAt(i)))
				throw new InvalidCreditCardNumberException("Non-numeric character in expiry");
	}
	
}
