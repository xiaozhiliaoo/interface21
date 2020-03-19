
package com.wrox.expertj2ee.ticket.boxoffice;

import java.util.List;


/** 
 * Subclass when we have enough seats, but not enough contiguous seats
 * Thrown only if we're not accepting non-adjacent seats
 */
public class NotEnoughContiguousSeatsException extends NotEnoughSeatsException {
	
	private int[] bestMatches;

	/**
	 * Constructor for NotEnoughContiguousSeatsException.
	 * @param possible
	 * @param howMany required
	 * @param matches List of Integer, in descending order
	 */
	public NotEnoughContiguousSeatsException(int classId, int howMany, List matches) {
		super(classId, ( (Integer) matches.get(0)).intValue(), howMany);
		bestMatches = new int[matches.size()];
		for (int i = 0; i < bestMatches.length; i++) {
			bestMatches[i] = ( (Integer) matches.get(i)).intValue();
		}
	}
	
//	public return List() {
//		return list of sorted contiguous groups from max to min;
//	}

/**
	 * @see ErrorCoded#getErrorCode()
	 */
	public String getErrorCode() {
		return "notEnoughAdjacentSeats";
	}
	
	/**
	 * The longest matches, in descending order
	 */
	public int[] getBestMatches() {
		return bestMatches;
	}

}
