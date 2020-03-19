
package com.wrox.expertj2ee.ticket.boxoffice;

import com.wrox.expertj2ee.ticket.exceptions.FatalException;

/**
 * Thrown on an attempt to delete a paid for booking
 * NOT CURRENTLY USED:
 * NOT DELETING BOOKING ON INVALID CCN
 */
public class BookingPaidForException extends FatalException {

	/**
	 * Constructor for BookingPaidForException.
	 * @param s
	 * @param ex
	 */
	public BookingPaidForException(int id) {
		super("Can't delete paid-for booking " + id);
	}

}
