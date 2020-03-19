
package com.interface21.jdbc.core;

import java.sql.SQLException;

import com.interface21.dao.InvalidDataAccessResourceUsageException;


/**
 * Exception thrown when SQL specified is invalid.
 * Could even have subclasses for no such table, no such column etc.
 * @version $Id: BadSqlGrammarException.java,v 1.1 2002/08/24 09:45:19 Rod Johnson Exp $
 */
public class BadSqlGrammarException extends InvalidDataAccessResourceUsageException {
	 
	private final SQLException ex;
	
	private final String sql;

	/**
	 * Constructor for BadSqlGrammerException.
	 * @param mesg
	 * @param sql
	 * @param ex
	 */
	public BadSqlGrammarException(String mesg, String sql, SQLException ex) {
		super("Bad SQL grammar [" + sql + "]", ex);
		this.ex = ex;
		this.sql = sql;
	}
	
	public SQLException getSQLException() {
		return ex;
	}
	
	public String getSql() {
		return sql;
	}

}
