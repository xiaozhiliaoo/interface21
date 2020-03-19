
package com.wrox.expertj2ee.ticket.boxoffice;

import java.io.Serializable;

public interface Reservation extends Serializable {
	
	long getTimestamp();
	
	String getReference();
	
	int getMinutesReservationWillBeValid();
	
	double getTotalPrice();
	
	ReservationRequest getQuoteRequest();
	
	Seat[] getSeats();
	
	boolean getSeatsAreAdjacent();
	
	boolean isReserved();
	
	/**
	 * Does this reservation request satisfy the following
	 * request
	 */
	boolean satisfiesRequest(ReservationRequest r);


}
