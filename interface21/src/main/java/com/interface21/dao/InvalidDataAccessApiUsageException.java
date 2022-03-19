
package com.interface21.dao;


/**
 * Exception thrown on incorrect usage of the API
 * (e.g. failing to "compile" an object that needed compilation
 * before execution).
 * A problem in our Java framework, not the underlying data
 * access infrastructure
 * @author Rod Johnson
 */
public class InvalidDataAccessApiUsageException extends DataAccessException {

	/**
	 * Constructor for InvalidDataAccessApiUsageException.
	 * @param s
	 */
	public InvalidDataAccessApiUsageException(String s) {
		super(s);
	}

	/**
	 * Constructor for InvalidDataAccessApiUsageException.
	 * @param s
	 * @param ex
	 */
	public InvalidDataAccessApiUsageException(String s, Throwable ex) {
		super(s, ex);
	}

}
