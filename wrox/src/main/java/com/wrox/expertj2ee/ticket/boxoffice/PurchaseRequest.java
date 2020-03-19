
package com.wrox.expertj2ee.ticket.boxoffice;

import com.wrox.expertj2ee.ticket.customer.RegisteredUser;

public class PurchaseRequest {

	private RegisteredUser user;
	
	private String cardNumber;
	
	private String expiry;
	
	private Reservation reservation;
	
	public PurchaseRequest(Reservation reservation) {
		this.reservation = reservation;
		this.user = new RegisteredUser();
	}
	
	public PurchaseRequest(Reservation reservation, RegisteredUser user) {
		this.reservation = reservation;
		this.user = user;
	}
	
	public RegisteredUser getUser() {
		return user;
	}
	
	//public void setUser(RegisteredUser user) {
	//	this.user = user;
	//}

	/**
	 * Gets the cardNumber.
	 * @return Returns a String
	 */
	public String getCardNumber() {
		return cardNumber;
	}

	/**
	 * Sets the cardNumber.
	 * @param cardNumber The cardNumber to set
	 */
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	/**
	 * Gets the expiry.
	 * @return Returns a String
	 */
	public String getExpiry() {
		return expiry;
	}

	/**
	 * Sets the expiry.
	 * @param expiry The expiry to set
	 */
	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}
	
	public Reservation getReservation() {
		return this.reservation;
	}

}
