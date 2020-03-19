
package com.wrox.expertj2ee.ticket.customer;

import java.io.Serializable;

public class RegisteredUser implements Serializable {
	
	private String email;
	
	private int birthYear;
	
	private Address billingAddress;
	
	private Address deliveryAddress;
	

	/**
	 * Constructor for RegisteredUser.
	 */
	public RegisteredUser() {
		super();
		billingAddress = new Address();
		deliveryAddress = new Address();
	}

	/**
	 * Gets the billingAddress.
	 * @return Returns a Address
	 */
	public Address getBillingAddress() {
		return billingAddress;
	}

	/**
	 * Sets the billingAddress.
	 * @param billingAddress The billingAddress to set
	 */
	public void setBillingAddress(Address billingAddress) {
		this.billingAddress = billingAddress;
	}

	/**
	 * Gets the deliveryAddress.
	 * @return Returns a Address
	 */
	public Address getDeliveryAddress() {
		return deliveryAddress;
	}

	/**
	 * Sets the deliveryAddress.
	 * @param deliveryAddress The deliveryAddress to set
	 */
	public void setDeliveryAddress(Address deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	/**
	 * Gets the email.
	 * @return Returns a String
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email.
	 * @param email The email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Gets the birthYear.
	 * @return Returns a int
	 */
	public int getBirthYear() {
		return birthYear;
	}

	/**
	 * Sets the birthYear.
	 * @param birthYear The birthYear to set
	 */
	public void setBirthYear(int birthYear) {
		this.birthYear = birthYear;
	}

}
