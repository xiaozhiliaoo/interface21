
package com.interface21.jdbc.core;

import com.interface21.dao.CleanupFailureDataAccessException;

public class CannotCloseJdbcConnectionException extends CleanupFailureDataAccessException {

	/**
	 * Constructor for CannotGetJdbcConnectionException.
	 * @param s
	 * @param ex
	 */
	public CannotCloseJdbcConnectionException(String s, Throwable ex) {
		super(s, ex);
	}

}
