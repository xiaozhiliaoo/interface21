
package com.wrox.expertj2ee.ticket.exceptions;


/**
 * Shouldn't really be checked: only checked because of
 * EJB
 */
public class NoSuchPerformanceException extends ApplicationException {
	
	private int id;

	/**
	 * Constructor for NoSuchPerformanceException.
	 * @param s
	 */
	public NoSuchPerformanceException(int id) {
		super("No performance with id " + id);
		this.id = id;
	}

	public int getPerformanceId() {
		return id;
	}
	
	/**
	 * @see ErrorCoded#getErrorCode()
	 */
	public String getErrorCode() {
		return "noPerformanceWithId";
	}

}
