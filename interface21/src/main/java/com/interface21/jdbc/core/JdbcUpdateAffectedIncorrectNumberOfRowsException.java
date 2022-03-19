
package com.interface21.jdbc.core;

import com.interface21.dao.IncorrectUpdateSemanticsDataAccessException;

public class JdbcUpdateAffectedIncorrectNumberOfRowsException extends IncorrectUpdateSemanticsDataAccessException {
	
	/** Number of rows that should have been affected */
	private final int expected;
	
	/** Number of rows that actually were affected */
	private final int actual;

	/**
	 * Constructor for JdbcUpdateAffectedIncorrectNumberOfRowsException.
	 * @param s
	 */
	public JdbcUpdateAffectedIncorrectNumberOfRowsException(String sql, int expected, int actual) {
		super("SQL update '" + sql + "' affected " + actual + " rows, not " + expected + ", as expected");
		this.expected = expected;
		this.actual = actual;
	}


	public int getExpectedRowsAffected() {
		return expected;
	}
	
	public int getActualRowsAffected() {
		return actual;
	}

	/**
	 * @see IncorrectUpdateSemanticsDataAccessException#getDataWasUpdated()
	 */
	public boolean getDataWasUpdated() {
		return getActualRowsAffected() > 0;
	}

}
