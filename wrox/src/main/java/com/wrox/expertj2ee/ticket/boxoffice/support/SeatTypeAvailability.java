
package com.wrox.expertj2ee.ticket.boxoffice.support;

import com.wrox.expertj2ee.ticket.boxoffice.InvalidSeatingRequestException;
import com.wrox.expertj2ee.ticket.boxoffice.NotEnoughSeatsException;
import com.wrox.expertj2ee.ticket.boxoffice.Seat;

/**
 * Interface representing seating in a class.
 * We can disconnect this from the database.
 */
public interface SeatTypeAvailability {
	
	
	// must be adjacent param!?
	SeatAllocation allocateSeats(int howMany, boolean mustBeAdjacent) throws NotEnoughSeatsException, InvalidSeatingRequestException;
	
	int getFreeSeatCount();


}
