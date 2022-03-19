package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @version 
 * @author Rod Johnson
 */
public interface PreparedStatementCreator {
	
	/** Create a statement in this connection. Allows
		 * implementations to use PreparedStatements. Only invoked
		 * if no SQL is passed into the ResultSetHandler.
		 * The ResultSetHandler will close this statement.
		 * @param conn Connection to use to create statement
		 * @return a prepared statement
		 */
	PreparedStatement createPreparedStatement(Connection conn) throws SQLException;

}