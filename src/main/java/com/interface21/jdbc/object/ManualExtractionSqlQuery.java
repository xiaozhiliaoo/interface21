package com.interface21.jdbc.object;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Reusable query in which concrete subclasses must
 * implement the abstract
 * extract(ResultSet, int) method to convert each row of the
 * JDBC ResultSet into an object.
 * <p/>
 * Simplifies ManualExtractionSqlQueryWithParameters API by dropping parameters.
 * Most subclasses won't care about parameters.
 * @author Rod Johnson
 */
public abstract class ManualExtractionSqlQuery extends ManualExtractionSqlQueryWithParameters {

	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------
	/**
	 * Bean constructor
	 */
	public ManualExtractionSqlQuery() {
	}
	
	/** Convenient constructor
	 * @param ds DataSource
	 * @param sql SQL to run
	 * @param types types for the SQL query
	 */
	public ManualExtractionSqlQuery(DataSource ds, String sql) {
		super(ds, sql); 
	}
	

	//-------------------------------------------------------------------------
	// Implementation of protected abstract method
	//-------------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to convert
	 * each row of the ResultSet into an object of the result type.
	 * @param rs RowSet we're working through
	 * @param rownum row number (from 0) we're up to
	 * @return an object of the result type
	 * @throws SQLException if there's an error extracting data.
	 * Subclasses can simply fail to catch SQLExceptions.
	 * @param parameters subclasses are rarely interested in this.
	 * It can be null
	 */
	protected final Object extract(ResultSet rs, int rownum, Object[] parameters) throws SQLException {
		return extract(rs, rownum);
	}
	
	/**
	 * Subclasses must implement this method to convert
	 * each row of the ResultSet into an object of the result type.
	 * @param rs RowSet we're working through
	 * @param rownum row number (from 0) we're up to
	 * @return an object of the result type
	 * @throws SQLException if there's an error extracting data.
	 * Subclasses can simply fail to catch SQLExceptions.
	 * @param parameters subclasses are rarely interested in this.
	 * It can be null
	 */
	protected abstract Object extract(ResultSet rs, int rownum) throws SQLException;
	
}