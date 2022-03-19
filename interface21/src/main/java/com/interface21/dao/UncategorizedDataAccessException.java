
package com.interface21.dao;


/**
 * Normal superclass when we can't distinguish anything
 * more specific than "something went wrong with the
 * underlying resource": for example, a SQLException we
 * can't pinpoint more precisely.
 * @author Rod Johnson
 * @version $Id: UncategorizedDataAccessException.java,v 1.1 2002/08/24 09:51:37 Rod Johnson Exp $
 */
public abstract class UncategorizedDataAccessException extends DataAccessException {

	/**
	 * Constructor for ResourceSpecificDataAccessException.
	 * @param s description of failure
	 * @param ex exception thrown by underlying data access API
	 */
	public UncategorizedDataAccessException(String s, Throwable ex) {
		super(s, ex);
	}

}
