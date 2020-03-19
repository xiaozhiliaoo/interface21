
package com.interface21.dao;


/**
 * Root of exceptions thrown when we use a data access
 * resource incorrectly. Thrown for example on specifying bad SQL
 * when using a RDBMS.
 * Resource-specific subclasses will probably be supplied by
 * data access packages.
 * @author Rod Johnson
 * @version $Id: InvalidDataAccessResourceUsageException.java,v 1.1 2002/08/24 09:51:06 Rod Johnson Exp $
 */
public class InvalidDataAccessResourceUsageException extends DataAccessException {
	
	/**
	 * Constructor for InvalidResourceSyntaxDataAccessException.
	 * @param s message
	 */
	public InvalidDataAccessResourceUsageException(String s) {
		super(s);
	}
	/**
	 * Constructor for InvalidResourceSyntaxDataAccessException.
	 * @param s message
	 * @param ex root cause
	 */
	public InvalidDataAccessResourceUsageException(String s, Throwable ex) {
		super(s, ex);
	}

}
