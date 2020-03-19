
package com.wrox.expertj2ee.ticket.boxoffice;

import com.wrox.expertj2ee.ticket.exceptions.ApplicationException;

public class NotEnoughSeatsException extends ApplicationException {
	
	private int requested;
	
	private int possible;
	
	private int classId;

	/**
	 * Constructor for NotEnoughSeatsException.
	 * @param arg0
	 */
	public NotEnoughSeatsException(int classId, int possible, int howMany) {
		super("Requested " + howMany + " seats in class " + classId + "; only " + possible + " are free");
		this.classId = classId;
		this.requested = howMany;
		this.possible = possible;
	}
	
	public int getRequested() {
		return requested;
	}
	
	public int getPossible() {
		return possible;
	}
	
	public int getClassId() {
		return classId;
	}
	
	/**
	 * @see ErrorCoded#getErrorCode()
	 */
	public String getErrorCode() {
		return "notEnoughSeats";
	}

}
