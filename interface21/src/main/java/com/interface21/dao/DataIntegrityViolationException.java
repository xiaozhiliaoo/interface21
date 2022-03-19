
package com.interface21.dao;

/**
 * Exception thrown when an attempt to insert or update data
 * results in violation of an integrity constraint.
 * @author Rod Johnson
 * @version $Id: DataIntegrityViolationException.java,v 1.1 2002/08/24 09:49:37 Rod Johnson Exp $
 */
public class DataIntegrityViolationException extends DataAccessException {

	/**
	 * Constructor for DataIntegrityViolationException.
	 * @param s mesg
	 * @param ex root cause
	 */
	public DataIntegrityViolationException(String s, Throwable ex) {
		super(s, ex);
	}

}
