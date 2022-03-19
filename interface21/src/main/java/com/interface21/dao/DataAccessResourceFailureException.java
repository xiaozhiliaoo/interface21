
package com.interface21.dao;

/**
 * Data access exception thrown when a resource fails
 * completely: for example, if we can't connect to a database
 * using JDBC.
 * @author Rod Johnson
 * @version $Id: DataAccessResourceFailureException.java,v 1.1 2002/08/24 09:48:38 Rod Johnson Exp $
 */
public class DataAccessResourceFailureException extends DataAccessException {

	/**
	 * Constructor for ResourceFailureDataAccessException.
	 * @param s message
	 * @param ex root cause from data access API in use
	 */
	public DataAccessResourceFailureException(String s, Throwable ex) {
		super(s, ex);
	}

}
