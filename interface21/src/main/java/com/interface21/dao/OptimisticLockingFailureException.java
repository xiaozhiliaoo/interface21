
package com.interface21.dao;

/**
 * Thrown on an optimistic locking violation. This exception
 * will generally be thrown by DAOs, rather than a resource
 * abstraction layer such as our JDBC abstraction layer.
 * @author Rod Johnson
 * @version $Id: OptimisticLockingFailureException.java,v 1.1 2002/08/24 09:56:55 Rod Johnson Exp $
 */ 
public class OptimisticLockingFailureException extends DataAccessException {

	/**
	 * Constructor for OptimisticLockingFailureDataAccessException.
	 * @param s mesg
	 */
	public OptimisticLockingFailureException(String s) {
		super(s);
	}

	/**
	 * Constructor for OptimisticLockingFailureDataAccessException.
	 * @param s mesg
	 * @param ex root cause
	 */
	public OptimisticLockingFailureException(String s, Throwable ex) {
		super(s, ex);
	}

}
