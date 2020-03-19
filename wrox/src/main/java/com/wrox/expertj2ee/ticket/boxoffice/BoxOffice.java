
package com.wrox.expertj2ee.ticket.boxoffice;

import com.wrox.expertj2ee.ticket.exceptions.NoSuchPerformanceException;
import com.wrox.expertj2ee.ticket.referencedata.*;


/**
 * The high-level, business interface
 * Uses commands for significant actions
 * that might need queuing etc.
 */
public interface BoxOffice {
	
	/**
	 * Should really be unchecked, as this shouldn't happen.
	 * Problem is EJB behavior on unchecked exceptions.
	 */
	int getSeatCount(int performanceId) throws NoSuchPerformanceException;
	
	int getFreeSeatCount(int performanceId) throws NoSuchPerformanceException;
	
	int getFreeSeatCount(int performanceId, int seatTypeId) throws NoSuchPerformanceException;
	
	//PerformanceWithAvailability getPerformanceWithAvailability(int performanceId) throws NoSuchPerformanceException;
	
	/**
	 */
	Reservation allocateSeats(ReservationRequest request) 
		throws NotEnoughSeatsException, NoSuchPerformanceException, InvalidSeatingRequestException;
	
	// Needs addresses...
	// SHOULD REALLY TAKE A COMMAND, which would include addresses
	/**
	 */
	Booking confirmReservation(PurchaseRequest purchase) 
		throws ExpiredReservationTakenException, CreditCardAuthorizationException, InvalidSeatingRequestException, BoxOfficeInternalException;
	
}
