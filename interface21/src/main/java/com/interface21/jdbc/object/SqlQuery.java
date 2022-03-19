/**
 * Feel free to use this code (with acknowledgment) or to
 * modify it to meet your needs.
 * These packages are described in "Expert One-on-One
 * J2EE Design and Development" by Rod Johnson (Wrox, 2002).
 * 
 * For commercial support, contact rod.johnson@interface21.com.
 */

package com.interface21.jdbc.object;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.InvalidDataAccessApiUsageException;
import com.interface21.jdbc.core.PreparedStatementCreatorFactory;
import com.interface21.jdbc.core.ResultReader;

import javax.sql.DataSource;
import java.util.List;

/**
 * Reusable threadsafe object to represent a SQL query.
 * Subclasses must implement the newResultReader() method to
 * provide an object that can save the results of iterating
 * over the ResultSet.
 * @author Rod Johnson
   @version $Id: SqlQuery.java,v 1.2 2002/08/24 17:48:44 Rod Johnson Exp $
 */
public abstract class SqlQuery extends SqlOperation {
	
	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
	/** 
	 * Number of rows to expect. If 0, unknown.
	*/
 	private int rowsExpected;
 	
 	/** 
 	 * Object used to create PreparedStatementCreators each time this query is executed
 	 * given SQL and declared parameters
 	 */
 	private PreparedStatementCreatorFactory preparedStatementFactory;

	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------
	/**
	 * Allow use as a bean
	 */
	public SqlQuery() {
	}
	
	/**
	 * Convenient constructor.
	 * @param ds DataSource
	 * @param sql (can also be supplied at runtime by overriding
	 * the getSql() method)
	 */
	public SqlQuery(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
	}


	//-------------------------------------------------------------------------
	// Bean properties
	//-------------------------------------------------------------------------
	/**
	 * Gets the rowsExpected.
	 * @return Returns a int
	 */
	public int getRowsExpected() {
		return rowsExpected;
	}

	/**
	 * Sets the rowsExpected.
	 * @param rowsExpected The rowsExpected to set
	 */
	public void setRowsExpected(int rowsExpected) {
		this.rowsExpected = rowsExpected;
	}
	
	//-------------------------------------------------------------------------
	// Execute methods
	//-------------------------------------------------------------------------
	/**
	 * Could override one to keep long-term state, rather
	 * than just return a collection, and thus rely on
	 * this object rather than the Collection returned by
	 * the execute() method.
	 * @param rowsExpected. If 0, we don't know how many rows to expect.
	 * This parameter can be ignored, but may help some implementations
	 * choose the most efficient Collection type: e.g. ArrayList
	 * instead of LinkedList for large result sets.
	 * @param parameters parameters in case subclass is interested
	 * COULD BE A MAP!? but would always require name
	 */
	protected abstract ResultReader newResultReader(int rowsExpected, Object[] parameters);

	/** 
	 * All execution goes through this method
	 */
	public final List execute(final Object[] parameters) throws DataAccessException {
		validateParameters(parameters);

		//PreparedStatementCreator psc = new DefaultPreparedStatementCreator(getSql(), getDeclaredParameters(), parameters);
		

		ResultReader rr = newResultReader(this.rowsExpected, parameters);
		getJdbcTemplate().query(newPreparedStatementCreator(parameters), rr);
		return rr.getResults();
	}
	
	/** 
	 * Convenient method to execute without parameters
	 */
	public final List execute() throws DataAccessException {
		return execute( (Object[]) null);
	}
	
	/** 
	 * Convenient method to execute with a single String parameter
	 */
	public final List execute(int p1) throws DataAccessException {
		return execute(new Object[] { new Integer(p1) });
	}
	
	/** 
	 * Convenient method to execute with two string parameters
	 */
	public final List execute(int p1, int p2) throws DataAccessException {
		return execute(new Object[] { new Integer(p1), new Integer(p2) });
	}
	
	/** 
	 * Convenient method to execute with a single String parameter
	 */
	public final List execute(String p1) throws DataAccessException {
		return execute(new Object[] { p1 });
	}

	/** 
	 * Base of all findObject methods
	 * @return null if not found. Subclasses may choose to treat this
	 * as an error and throw an exception.
	 */
	public final Object findObject(Object[] parameters) throws DataAccessException {
		List l = execute(parameters);
		if (l.size() == 0)
			return null;
		if (l.size() > 1)
			throw new InvalidDataAccessApiUsageException("Result is not unique. Found " + l.size());
		return l.get(0);
	}
	
	/** 
	 * Convenience method 
	 */
	public final Object findObject(int p1) throws DataAccessException {
		return findObject(new Object[] { new Integer(p1) });
	}
	
	/** 
	 * Convenience method 
	 */
	public final Object findObject(int p1, int p2) throws DataAccessException {
		return findObject(new Object[] { new Integer(p1), new Integer(p2) });
	}
	
	/** Convenience method */
	public final Object findObject(String p1) throws DataAccessException {
		return findObject(new Object[] { p1 });
	}	

	
	protected void onCompileInternal() {
		logger.finest("NOP onCompileInternal");
	}

}