
package com.interface21.jdbc.core;

import com.interface21.dao.UncategorizedDataAccessException;

import java.sql.SQLException;

/**
 * Exception thrown when we can't classify a SQLException
 * @author Rod Johnson
 * @version $Id: UncategorizedSQLException.java,v 1.1 2002/08/24 09:44:15 Rod Johnson Exp $
 */
public class UncategorizedSQLException extends UncategorizedDataAccessException {
	
	private final String sql;

	/**
	 * Constructor for ConnectionFactoryException.
	 * @param mesg message
	 * @param sql SQL we were tring to execute
	 * @param ex SQLException
	 */
	public UncategorizedSQLException(String mesg, String sql, SQLException ex) {
		super(mesg, ex);
		this.sql = sql;
	}
	
	public SQLException getSQLException() {
		return (SQLException) getRootCause();
	}
	
	public String getSql() {
		return sql;
	}

}
