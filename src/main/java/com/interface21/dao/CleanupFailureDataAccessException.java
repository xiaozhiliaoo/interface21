
package com.interface21.dao;

/**
 * Exception thrown when we couldn't cleanup after a data
 * access operation, but the actual operation went OK.
 * For example, this would be thrown if a JDBC Connection
 * couldn't be closed after it had been used successfully.
 * @author Rod Johnson
 */
public class CleanupFailureDataAccessException extends DataAccessException {

	/**
	 * Constructor for CleanupFailureDataAccessException.
	 * @param s Message
	 * @param ex Root cause from the underlying data access API,
	 * such as JDBC
	 */
	public CleanupFailureDataAccessException(String s, Throwable ex) {
		super(s, ex);
	}

}
