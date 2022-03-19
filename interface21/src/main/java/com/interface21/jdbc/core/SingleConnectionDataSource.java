package com.interface21.jdbc.core;

import java14.java.util.logging.Logger;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Implementation of SmartDataSource that wraps a single connection
 * which is not closed after use. Note that this means that something other than 
 * users of this connection factory (which will never try to close the connection)
 * should close the connection.
 * <p/>This is primarily a test class. For example, it enables easy testing
 * of code outside an application server.
 */
public class SingleConnectionDataSource implements SmartDataSource {

	protected final static Logger logger = Logger.getLogger(SingleConnectionDataSource.class.getName());
	
	/** Wrapped connection */
	private Connection connection;

	private PrintWriter pw = new PrintWriter(System.out);

	/**
	 * Create a new SingleConnectionDataSource
	 */
	public SingleConnectionDataSource(Connection connection) throws SQLException {
		logger.info("Creating a SingleConnectionDataSource with connection [" + connection + "]");
		this.connection = connection;
		connection.setAutoCommit(false);
	}

	public void setAutocommit(boolean flag) throws SQLException {
		connection.setAutoCommit(flag);
	}
	
//	public void commit() throws SQLException {
//		connection.commit();
//	}
//	
//	public void rollback() throws SQLException {
//		connection.rollback();
//	}

	/**
	 * @see ConnectionFactory#getConnection()
	 */
	public Connection getConnection() throws SQLException {
		logger.fine("SingleConnectionConnectionFactory.getConnection with con=" + connection);
		if (connection == null)
			throw new SQLException("Connection is null in SingleConnectionDataSource; must not have been initialized properly");

		if (connection.isClosed()) {
			String mesg = "Connection was closed in SingleConnectionDataSource. "
						+ "Check that user code checks shouldClose() before closing connections";
			logger.severe(mesg);
			throw new SQLException(mesg);
		}
		return connection;
	}

	/**
	 * @see SmartDataSource#shouldClose(Connection)
	 */
	public boolean shouldClose(Connection conn) {
		return false;
	}

	/** Must be invoked in a finally block
	 */
	public void close() throws SQLException {
		connection.close();
	}

	/*
	 * @see DataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() {
		// This is basically a debug class
		return pw;
	}

	public void setLogWriter(PrintWriter pw) {
		this.pw = pw;
	}

	/*
	 * We concentrate on use in an app server, so we don;t
	 * worry about this
	 * @see DataSource#getConnection(String, String)
	 */
	public Connection getConnection(String arg0, String arg1) throws SQLException {
		throw new UnsupportedOperationException();
	}

	/*
	 * @see DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	/*
	 * @see DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int arg0) throws SQLException {
	}


/*
编译时候加的，本来没有
*/
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
}