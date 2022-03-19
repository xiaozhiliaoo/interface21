
package com.interface21.dao;

/**
 * Data access exception thrown when something unintended
 * appears to have happened with an update.
 * Thrown, for example, when we wanted to update 1 row
 * but actually updated 3
 * @author Rod Johnson
 * @version $Id: IncorrectUpdateSemanticsDataAccessException.java,v 1.1 2002/08/24 09:55:38 Rod Johnson Exp $
 */
public abstract class IncorrectUpdateSemanticsDataAccessException extends InvalidDataAccessResourceUsageException {

	/**
	 * Constructor for IncorrectUpdateSemanticsDataAccessException.
	 * @param s
	 */
	public IncorrectUpdateSemanticsDataAccessException(String s) {
		super(s);
	}

	/**
	 * Constructor for IncorrectUpdateSemanticsDataAccessException.
	 * @param s
	 * @param ex
	 */
	public IncorrectUpdateSemanticsDataAccessException(String s, Throwable ex) {
		super(s, ex);
	}
	
	/**
	 * Return whether data was updated
	 * @return whether data was updated (as opposed to being
	 * incorrectly updated). If this method returns true,
	 * there's nothing to roll back.
	 */
	public abstract boolean getDataWasUpdated();

}
