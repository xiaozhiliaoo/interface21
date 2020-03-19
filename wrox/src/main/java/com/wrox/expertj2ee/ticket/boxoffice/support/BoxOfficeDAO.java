
package com.wrox.expertj2ee.ticket.boxoffice.support;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.interface21.dao.DataAccessException;
import com.wrox.expertj2ee.ticket.boxoffice.BookingPaidForException;
import com.wrox.expertj2ee.ticket.boxoffice.ExpiredReservationTakenException;
import com.wrox.expertj2ee.ticket.boxoffice.Reservation;


public interface BoxOfficeDAO {
	
	//Seat[] getAllSeats(int performanceId);
	
	/**
	 * @return collection of Seat objects
	 */
	List getAllSeats(int performanceId) throws DataAccessException;
	
	
	/**
	 * @return list of PriceBand objects for this performance
	 */
	List getPriceBands(int performanceId);
	
	/**
	 * List of Integer seat id
	 * NB: Must return row by row, from left to right.
	 * Normally lowest ids will be front left
	 * @param lock guarantees that these seats will be available
	 * to the current transaction
	 */
	List getFreeSeats(int performanceId, int classId, boolean lock) throws DataAccessException;
	
	/**
	 * Should never lock
	 */
	int getFreeSeatCount(int performanceId) throws DataAccessException;
	
	
	/**
	 * Must lock against any other transaction modifying the seats
	 */
	int[] getSeatIdsForBooking(String bookingId) throws DataAccessException;
	
	/**
	 * Can't delete a paid-for booking
	 */
	//void deleteBooking(int bookingId) throws BookingPaidForException;
	
	
	/**
	 * @return booking id
	 */
	String reserve(Reservation quote) throws DataAccessException;
	
	
	// TODO**** Will also take address objects as well
	int createPurchaseRecord(Reservation quote) throws ExpiredReservationTakenException, DataAccessException;
	
	Date setAuthorizationCode(int purchaseId, String authorizationCode) throws DataAccessException;

}
