package jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.sql.DataSource;

import com.interface21.jdbc.core.SmartDataSource;

 
/** 
 *
 * @author  Rod Johnson
 * @since May 3, 2001
 * @version $RevisionId$
 */
public class TestDataSource implements SmartDataSource {

	//static final String url = "jdbc:cloudscape:rmi:d:\\book\\project\\db\\cloudscape";
	//static final String driverName = "COM.cloudscape.core.RmiJdbcDriver";

	String url = "jdbc:oracle:thin:@127.0.0.1:1521:rj";
	String driverName = "oracle.jdbc.driver.OracleDriver";

	// Default Oracle login
	String user = "SYSTEM";
	String password = "MANAGER";
	
	private PrintWriter pw = new PrintWriter(System.out);
	
	/** Reuse mode is NOT multithreaded */
	private Connection theConn;


	public TestDataSource() {
		try {
			Class.forName(driverName);
			System.out.println("loaded " + driverName);
		}
		catch (Exception ex) {
			throw new RuntimeException("Couldn't load driver " + ex);
		}
	}
	
	public TestDataSource(String url, String driverName, String user, String password) {
		this();
		this.url = url;
		this.driverName = driverName;
		this.user = user;
		this.password = password;
	}
	

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}


	/**
	 * @see ConnectionFactory#shouldClose(Connection)
	 */
	public boolean shouldClose(Connection conn) {
		return false;
	}


	/*
	 * @see ConnectionFactory#getLogWriter()
	 */
	public PrintWriter getLogWriter() {
		return pw;
	}

	/*
	 * @see DataSource#getConnection(String, String)
	 */
	public Connection getConnection(String uname, String pwd) throws SQLException {
		throw new UnsupportedOperationException("getConnection(uname, pwd) is not implemented in TestCF");
	}

	/*
	 * Returns 0: means use default system timeout
	 * @see DataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	/*
	 * @see DataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int arg0) throws SQLException {
		throw new UnsupportedOperationException("setLoginTimeout");
	}

	/*
	 * @see DataSource#setLogWriter(PrintWriter)
	 */
	public void setLogWriter(PrintWriter pw) throws SQLException {
		this.pw = pw;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
}