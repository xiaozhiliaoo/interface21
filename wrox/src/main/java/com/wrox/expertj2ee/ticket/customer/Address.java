
package com.wrox.expertj2ee.ticket.customer;

import java.io.Serializable;

public class Address implements Serializable {
	
	private String street;
	
	private String line2;
	
	private String city;
	
	private String postcode;


	/**
	 * Gets the street.
	 * @return Returns a String
	 */
	public String getStreet() {
		return street;
	}

	/**
	 * Sets the street.
	 * @param street The street to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}

	/**
	 * Gets the line2.
	 * @return Returns a String
	 */
	public String getLine2() {
		return line2;
	}

	/**
	 * Sets the line2.
	 * @param line2 The line2 to set
	 */
	public void setLine2(String line2) {
		this.line2 = line2;
	}

	/**
	 * Gets the city.
	 * @return Returns a String
	 */
	public String getCity() {
		return city;
	}

	/**
	 * Sets the city.
	 * @param city The city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * Gets the postcode.
	 * @return Returns a String
	 */
	public String getPostcode() {
		return postcode;
	}

	/**
	 * Sets the postcode.
	 * @param postcode The postcode to set
	 */
	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

}
