
package com.wrox.expertj2ee.ticket.boxoffice.support;

import com.wrox.expertj2ee.ticket.boxoffice.Seat;

import java.io.Serializable;

public final class SeatAllocation implements Serializable {
	
	private final Seat[] seats;
	
	private final boolean adjacent;

	/**
	 * Constructor for SeatAllocation.
	 */
	public SeatAllocation(Seat[] pseats, boolean adjacent) {
		seats = new Seat[pseats.length];
		// Deep copy: immutable
		System.arraycopy(pseats, 0, seats, 0, pseats.length);
		this.adjacent = adjacent;
	}
	
	public Seat[] getSeats() {
		return seats;
	}
	
	public boolean adjacent() {
		return adjacent;
	}

}
