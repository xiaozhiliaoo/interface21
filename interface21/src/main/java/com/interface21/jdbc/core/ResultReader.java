package com.interface21.jdbc.core;

import java.util.List;


/**
 * Extension of RowCallbackHandler that saves the
 * accumulated results as a Collection.
 */
public interface ResultReader extends RowCallbackHandler {
	 
	/**
	 * Return all results, disconnected from the JDBC ResultSet.
	 * @return all results, disconnected from the JDBC ResultSet.
	 * Never returns null; returns the empty collection if there
	 * were no results.
	 */
	List getResults();

}

