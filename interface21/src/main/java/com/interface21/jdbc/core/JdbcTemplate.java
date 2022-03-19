/**
 * Feel free to modify this code to meet your needs.
 * These packages are described in "Expert One-on-One
 * J2EE Design and Development" by Rod Johnson (Wrox, 2002).
 * 
 * For commercial support, contact rod.johnson@interface21.com.
 */

package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Class that iterates over a ResultSet, handling any errors,
 * and calling back a helper on each line.
 * Provides a convenient way to retrieve data using JDBC.
 * @author  Rod Johnson
 * @since May 3, 2001
 * @version $RevisionId: ResultSetHandler.java,v 1.1 2001/09/07 12:48:57 rod Exp $
 */
public class JdbcTemplate {
	
	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	/** ConnectionFactory to use to obtain connections throughout
	 * the lifecycle of this object.
	 **/
	private DataSource dataSource;
	
	/** If this is false, we will throw exceptions on SQL warnings
	 */
	private boolean ignoreWarnings = true;
	
	/** Helper to translate SQL exceptions to DataAccessExceptions */
	private SQLExceptionTranslater exceptionTranslater;

	//-------------------------------------------------------------------------
	// Constructor
	//-------------------------------------------------------------------------
	/** Construct a new JDBCTemplate, given a connection factory
	 * @param connectionFactory connection factory to use to obtain connections from
	 */
	public JdbcTemplate(DataSource dataSource) {
		this.dataSource = dataSource;
		this.exceptionTranslater = new SQLStateSQLExceptionTranslater();
	}
	
	//-------------------------------------------------------------------------
	// Configuration properties
	//-------------------------------------------------------------------------
	/**
	 * Return whether or not we want to ignore SQLWarnings. 
	 * Default is true
	 */
	public void setIgnoreWarnings(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}
	
	/**
	 * Return whether or not we ignore SQLWarnings
	 * @return whether or not we ignore SQLWarnings.
	 * Default is true.
	 */
	public boolean getIgnoreWarnings() {
		return ignoreWarnings;
	}
	
	
	/**
	 * Set the exception translater used in this class.
	 * If no custom translater is provided, a default is used
	 * which examines the SQLException's SQLState code.
	 * @param exceptionTranslater custom exception translator.
	 */
	public void setExceptionTranslater(SQLExceptionTranslater exceptionTranslater) {
		this.exceptionTranslater = exceptionTranslater;
	}
	
	
	/**
	 * Return the DataSource used by this template
	 * @return the DataSource used by this template
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	//-------------------------------------------------------------------------
	// Public methods
	//-------------------------------------------------------------------------
	/**
	 * Execute a query given static SQL.
	 * Doesn't use a prepared statement.
	 * @param sql
	 * @param callbackHandler
	 * @throws SQLException
	 * @throws NamingException
	 */
	public void query(String sql, RowCallbackHandler callbackHandler) throws DataAccessException {
		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			con = DataSourceUtils.getConnection(this.dataSource);
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			if (logger.isLoggable(Level.INFO))
				logger.info("Executing static SQL query '" + sql + "'");

			while (rs.next()) {
				callbackHandler.processRow(rs);
			}
			
			SQLWarning warning = ps.getWarnings();
			rs.close();
			ps.close();
			
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
		}
		catch (SQLException ex) {
			throw this.exceptionTranslater.translate("JdbcTemplate.query(sql)", sql, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(this.dataSource, con);
		}
	} 	// query
	

	/**
	 * Query using a prepared statement. Most other methods use
	 * this.
	 * @param psc
	 * @param callbackHandler
	 * @throws SQLException
	 * @throws NamingException
	 */
	public void query(PreparedStatementCreator psc, RowCallbackHandler callbackHandler) throws DataAccessException {
		Connection con = null;
		Statement s = null;
		ResultSet rs = null;
		try {
			con = DataSourceUtils.getConnection(this.dataSource);
			PreparedStatement ps = psc.createPreparedStatement(con);
			if (logger.isLoggable(Level.INFO))
				logger.info("Executing SQL query using PreparedStatement: [" + psc + "]");
			rs = ps.executeQuery();

			while (rs.next()) {
				if (logger.isLoggable(Level.FINEST))
					logger.finest("Processing row of ResultSet");
				callbackHandler.processRow(rs);
			}
			
			SQLWarning warning = ps.getWarnings();
			rs.close();
			ps.close();
			throwExceptionOnWarningIfNotIgnoringWarnings(warning);
		}
		catch (SQLException ex) {
			throw this.exceptionTranslater.translate("JdbcTemplate.query(psc) with PreparedStatementCreator [" + psc + "]", null, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(this.dataSource, con);
		}
	} 	// query


	/**
	 * Method update.
	 * @param sql
	 * @return int
	 * @throws SQLException
	 * @throws NamingException
	 */
	public int update(final String sql) throws DataAccessException {
		if (logger.isLoggable(Level.INFO))
			logger.info("Running SQL update '" + sql + "'");
		return update(PreparedStatementCreatorFactory.newPreparedStatementCreator(sql));
	}

	/**
	 * Method update.
	 * @param psc
	 * @return int
	 * @throws SQLException
	 * @throws NamingException
	 */
	public int update(PreparedStatementCreator psc) throws DataAccessException {
		return update(new PreparedStatementCreator[] { psc })[0];
	}

	/**
	 * Run multiple updates.
	 * @param pscs
	 * @return int[]
	 * @throws SQLException
	 * @throws NamingException
	 */
	public int[] update(PreparedStatementCreator[] pscs) throws DataAccessException {
		Connection con = null;
		Statement s = null;
		int index = 0;
		try {
			con = DataSourceUtils.getConnection(this.dataSource);
			int[] retvals = new int[pscs.length];
			for (index = 0; index < retvals.length; index++) {
				PreparedStatement ps = pscs[index].createPreparedStatement(con);
				retvals[index] = ps.executeUpdate();
				if (logger.isLoggable(Level.INFO))
					logger.info("JDBCTemplate: update affected " + retvals[index] + " rows");
				ps.close();
			}
			
			// Don't worry about warnings, as we're more likely to get exception on updates
			// (for example on data truncation)
			return retvals;
		}
		catch (SQLException ex) {
			throw this.exceptionTranslater.translate("processing update " +
				(index + 1) + " of " + pscs.length + "; update was [" + pscs[index] + "]", null, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(this.dataSource, con);
		}
	}	// update

	/**
	 * Method batchUpdate.
	 * ONLY WORKS WITH STATIC SQL: so we don't favor this
	 * @param sql
	 * @return int[]
	 * @throws SQLException
	 * @throws NamingException
	 */
	/*public int[] batchUpdate(String[] sql) throws SQLException {
		Connection con = null;
		try {
			con = connectionFactory.getConnection();
			Statement s = con.createStatement();
			//int retval = ps.executeUpdate();
			//** TEMP FIX RETURN VALS

			for (int i = 0; i < sql.length; i++) {
				s.addBatch(sql[i]);
				logSql(sql + "(BATCH)");
			}

			int[] rowsUpdated = s.executeBatch();
			s.close();
			return rowsUpdated;
		}
		finally {
			closeConnectionIfNecessary(con);
		}
	}
	*/
	
	
	/**
	 * Convenience method to throw a JdbcSqlWarningException if we're
	 * not ignoring warnings
	 * @param warning warning from current statement. May be null,
	 * in which case this method does nothing.
	 */
	private void throwExceptionOnWarningIfNotIgnoringWarnings(SQLWarning warning) throws SQLWarningException {
		if (warning != null) {
			if (this.ignoreWarnings) {
				logger.warning("SQLWarning ignored: " + warning); 
			}
			else {
				throw new SQLWarningException("Warning not ignored", warning);
			}
		}
	}

} // class JdbcTemplate