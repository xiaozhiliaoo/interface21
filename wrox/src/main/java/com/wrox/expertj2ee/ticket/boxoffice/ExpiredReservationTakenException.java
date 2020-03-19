
package com.wrox.expertj2ee.ticket.boxoffice;

import com.wrox.expertj2ee.ticket.exceptions.ApplicationException;


/**
 * Thrown when a seat reservation has expired and the
 * seats have been sold to someone else
 * @author Rod Johnson
 */
public class ExpiredReservationTakenException extends ApplicationException {

	/**
	 * Constructor for ExpiredReservationTakenException.
	 */
	public ExpiredReservationTakenException(String mesg) {
		super(mesg);
	}
	
	/**
	 * @see ErrorCoded#getErrorCode()
	 */
	public String getErrorCode() {
		return "seatsSold";
	}

}
