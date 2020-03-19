
package com.interface21.dao;

/**
 * Generic exception thrown when this process was the deadlock loser.
 * @author Rod Johnson
 * @version $Id: DeadlockLoserDataAccessException.java,v 1.1 2002/08/24 09:54:30 Rod Johnson Exp $
 */
public class DeadlockLoserDataAccessException extends DataAccessException {

	/**
	 * Constructor for DeadlockLoserDataAccessException.
	 * @param s mesg
	 * @param ex root cause
	 */
	public DeadlockLoserDataAccessException(String s, Throwable ex) {
		super(s, ex);
	}

}
