
package com.interface21.dao;

/**
 * Thrown on mismatch between Java type and database type
 * (for example on an attempt to set an object of the wrong type
 * in an RDBMS column).
 * @author Rod Johnson
 * @version $Id: TypeMismatchDataAccessException.java,v 1.1 2002/08/24 09:53:27 Rod Johnson Exp $
 */
public class TypeMismatchDataAccessException extends DataAccessException {

	/**
	 * Constructor for TypeMismatchDataAccessException.
	 * @param s mesg
	 * @param ex root cause
	 */
	public TypeMismatchDataAccessException(String s, Throwable ex) {
		super(s, ex);
	}

}
