
package com.interface21.dao;

import com.interface21.core.NestedRuntimeException;


/**
 * Root of the hierarchy of data access exceptions.
 * This hierarchy aims to let user code find and
 * handle the kind of error encountered without knowing the details
 * of the particular data access API in use (e.g. JDBC).
 * Thus it is possible to react to an optimistic locking failure
 * without knowing that JDBC is being used.
 * <br/>As this class is a runtime exception, there is no need 
 * for user code to catch it or subclasses if any error is
 * to be considered fatal (the usual case).
 * @author Rod Johnson
 */
public abstract class DataAccessException extends NestedRuntimeException {

	/**
	 * Constructor for DataAccessException.
	 * @param s message
	 */
	public DataAccessException(String s) {
		super(s);
	}

	/**
	 * Constructor for DataAccessException.
	 * @param s message
	 * @param ex root cause (usually from using a underlying
	 * data access API such as JDBC)
	 */
	public DataAccessException(String s, Throwable ex) {
		super(s, ex);
	}

}
