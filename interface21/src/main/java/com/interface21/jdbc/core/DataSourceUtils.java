package com.interface21.jdbc.core;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
 
/**
 * Class containing static methods to obtain connections
 * from JNDI and close connections if necessary.
 * @author Rod Johnson
 * @version $RevisionId: ResultSetHandler.java,v 1.1 2001/09/07 12:48:57 rod Exp $
 */
public abstract class DataSourceUtils {
	
	/** 
	 * Get data source from JNDI, under java:comp/env/jdbc
	 * @return data source
	 * @throw NamingException if the data source cannot be located
 	*/
	public static DataSource getDataSourceFromJNDI(String jndiName) throws NamingException {
		Context initCtx = null;
		try {
			// Obtain the initial JNDI context
			initCtx = new InitialContext();
			// Perform JNDI lookup to obtain resource manager
			// connection factory
			DataSource ds = (DataSource) initCtx.lookup("java:comp/env/jdbc/" + jndiName);
			
			return ds;
		}
		catch (NamingException ex) {
			throw new CannotGetJdbcConnectionException("Naming exception in JNDIConnectionFactory looking up data source [" + jndiName + "]", ex);
		}
		finally {
			try {
				if (initCtx != null)
					initCtx.close();
			}
			catch (NamingException ex) {
				// This shouldn't really happen
				throw new CannotGetJdbcConnectionException("Naming exception closing context in JNDIConnectionFactory.getConnection", ex);
			}
		}
	}	// getDataSourceFromJNDI
	
	
	/** 
	 * Correctly changes exceptions 
	 */
	public static Connection getConnection(DataSource ds) throws CannotGetJdbcConnectionException {
		try {
			return ds.getConnection();
		}		
		catch (SQLException ex) {
			throw new CannotGetJdbcConnectionException("DataSource " + ds, ex);
		}
	}

	/**
	 * Close the connection if necessary
	 * @param ds DataSource
	 * @param con connection to close if necessary. If this is
	 * null, this call will be ignored.
	 * @param ds DataSource. May be null
	 */
	public static void closeConnectionIfNecessary(DataSource ds, Connection con) throws CannotCloseJdbcConnectionException {
		if (con == null)
			return;

		boolean shouldClose = true;
		// Leave the connection open only if the DataSource is our
		// special data source, and it wants the connection left open
		if (ds != null && ds instanceof SmartDataSource)
			shouldClose = ((SmartDataSource) ds).shouldClose(con);

		if (shouldClose) {
			try {
				con.close();
			}
			catch (SQLException ex) {
				// OPERATION PARAM?
				throw new CannotCloseJdbcConnectionException("Failed to close connection", ex);
			}
		}
	}	// closeConnectionIfNecessary

}	// class DataSourceUtils