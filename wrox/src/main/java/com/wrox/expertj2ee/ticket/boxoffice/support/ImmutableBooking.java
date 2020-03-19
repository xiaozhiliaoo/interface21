
package com.wrox.expertj2ee.ticket.boxoffice.support;

import java.util.Date;

import com.wrox.expertj2ee.ticket.boxoffice.Booking;
import com.wrox.expertj2ee.ticket.boxoffice.Reservation;


/**
 * Immutable
 */
public final class ImmutableBooking implements Booking {

	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------

	private final String authorizationCode;

		/** Holder for property dateMade */
	private final Date dateMade;


	private final Reservation quote;

	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------

	/** Construct a new Booking object without an id.
	 * This is before database insert.
	 */
	public ImmutableBooking(String authorizationCode, Date dateMade, Reservation quote) {
		this.authorizationCode = authorizationCode;
		this.dateMade = dateMade;
		this.quote = quote;
	}

	//-------------------------------------------------------------------------
	// Bean properties
	//-------------------------------------------------------------------------


	
	public String getAuthorizationCode() {
		return authorizationCode;
	}


	
	public Date getDateMade() {
		return dateMade;
	}


	
	public Reservation getReservation() {
		return this.quote;
	}


	//-------------------------------------------------------------------------
	// Diagnostics
	//-------------------------------------------------------------------------
		/** Return a diagnostic String showing this object's state
		 * @return a diagnostic String showing this object's state
		 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName() + ": ");
		sb.append("authorizationCode=" + getAuthorizationCode() + "; ");
		sb.append("dateMade=" + getDateMade() + "; ");
		sb.append(this.quote);
		sb.append(" hashCode=" + hashCode());
		return sb.toString();
	}

}	// class BookingImpl

