
package com.wrox.expertj2ee.ticket.boxoffice.support;

import java.io.Serializable;
import java.util.Date;

import com.wrox.expertj2ee.ticket.boxoffice.Reservation;
import com.wrox.expertj2ee.ticket.boxoffice.ReservationRequest;
import com.wrox.expertj2ee.ticket.boxoffice.Seat;

public class ReservationImpl implements Reservation {
	
	/** DO WE WANT SEATS OR IDS?  less to serialize
	 * BUT SIMPLER CODE...
	 * */
	private final Seat[] seats;
	
	private final ReservationRequest quoteRequest;
	
	private final double totalPrice;
	
	private final long timestamp;
	
	private boolean adjacent;
	
	private String reference;
	
	/** Creates new AvailableSeats */
    public ReservationImpl(ReservationRequest quoteRequest, Seat[] seats, boolean adjacent, double totalPrice, String ref) {
		this.seats = seats;
		this.quoteRequest = quoteRequest;
		this.totalPrice = totalPrice;
		this.timestamp = System.currentTimeMillis();
		this.reference = ref;
		this.adjacent = adjacent;
    }
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getReference() {
		return reference;
	}
	
	public double getTotalPrice() {
		return totalPrice;
	}
	
	public ReservationRequest getQuoteRequest() {
		return quoteRequest;
	}
	
	public void reserve(String reference) {
		this.reference = reference;
	}
	
	public Seat[] getSeats() {
		return seats;
	}
	
	public boolean getSeatsAreAdjacent() {
		return adjacent;
	}
	
	// ALSO RESERVED UNTIL?
	public boolean isReserved() {
		return reference != null;
	}
	
	/**
	 * @see Reservation#minutesReservationWillBeValid()
	 */
	public int getMinutesReservationWillBeValid() {
		if (!isReserved())
			return 0;
		// Slight hack here: we add 2 secs to the time to live, so that it will show full
		// time to live when this method is invoked when view renders
		long millisToLive = 2000L + quoteRequest.getHoldTill().getTime() - System.currentTimeMillis();
		// It can't live for a negative amount of time
		if (millisToLive < 0L)
			return 0;
		return (int) (millisToLive / 60000L);
	}
	
	/**
	 * @see Reservation#satisfiesRequest(ReservationRequest)
	 */
	public boolean satisfiesRequest(ReservationRequest r) {
		if (r == this.quoteRequest)
			return true;
			
		return r.getClassID() == quoteRequest.getClassID() &&
				r.getPerformanceID() == quoteRequest.getPerformanceID() &&
				r.getSeatsRequested() == this.seats.length;
				// CLEVER XOR
	//			r.getSeatsMustBeAdjacent();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("Seat quote: ");
		sb.append("ref is '" + getReference() + "'; ");
		sb.append("total price is " + getTotalPrice() + "; ");
		sb.append("adjacent=" + getSeatsAreAdjacent() + "; ");
		sb.append("quote request was [" + quoteRequest + "]; booked seats (");
		for (int i = 0; i < seats.length; i++)
			sb.append(seats[i].getName() + " ");
		return sb.toString() + ")";
	}

}
