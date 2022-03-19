package com.interface21.jdbc.object;

import com.interface21.jdbc.core.ResultReader;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Reusable query in which concrete subclasses must
 * implement the abstract
 * extract(ResultSet, int) method to convert each row of the
 * JDBC ResultSet into an object.
 * <p/>
 * Such manual extraction is usually preferable to "automatic"
 * extraction using reflection, which can become complex in non-trivial
 * cases. For example, the present class allows different objects
 * to be used for different rows (for example, if a subclass is indicated).
 * It allows computed fields to be set. And there's no need for 
 * ResultSet columns to have the same names as bean properties.
 * The Pareto Principle in action: going the extra mile to automate
 * the extraction process makes the framework much more complex
 * and delivers little real benefit.
 * <p/>
 * Subclasses can be constructed providing SQL, parameter types
 * and a DataSource. SQL will often vary between subclasses.
 * Subclasses must implement the extract(ResultSet, int) method to
 * convert an entire row of the result set to a Java object.
 * @author Rod Johnson
 */
public abstract class ManualExtractionSqlQueryWithParameters extends SqlQuery {

	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------
	/**
	 * Bean constructor
	 */
	public ManualExtractionSqlQueryWithParameters() {
	}
	
	/** Convenient constructor
	 * @param ds DataSource
	 * @param sql SQL to run
	 * @param types types for the SQL query
	 */
	public ManualExtractionSqlQueryWithParameters(DataSource ds, String sql) {
		super(ds, sql); 
	}
	

	//-------------------------------------------------------------------------
	// Implementation of protected abstract method
	//-------------------------------------------------------------------------

	protected final ResultReader newResultReader(int rowsExpected, Object[] parameters) {
		return new ResultReaderImpl(rowsExpected, parameters);
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
	protected abstract Object extract(ResultSet rs, int rownum, Object[] parameters) throws SQLException;


	//-------------------------------------------------------------------------
	// Inner classes
	//-------------------------------------------------------------------------
	/**
	 * Implementation of ResultReader that calls the enclosing
	 * class's extract() method for each row.
	 */
	private class ResultReaderImpl implements ResultReader {
		
		/** List to save results in */
		private List l; 
		
		private Object[] params;
		
		private int rowNum = 0;
		
		/** Use an array list. More efficient if we know how many
		 * results to expect
		 */
		public ResultReaderImpl(int rowsExpected, Object[] parameters) {
			// Use the more efficient collection if we know how many rows to expect
			this.l = (rowsExpected > 0) ? (List) new ArrayList(rowsExpected) : (List) new LinkedList();
			this.params = parameters;
		}
 
		public void processRow(ResultSet rs) throws SQLException {
			l.add(extract(rs, rowNum++, params));
		}

		/**
		 * @see ResultReader#getResults()
		 */
		public List getResults() {
			return l;
		}
	}	// inner class ResultReaderImpl
	
}	// class ManualExtractionSqlQueryWithParameters