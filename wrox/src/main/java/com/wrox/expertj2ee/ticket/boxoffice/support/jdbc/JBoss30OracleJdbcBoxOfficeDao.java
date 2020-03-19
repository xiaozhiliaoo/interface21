/*

package com.wrox.expertj2ee.ticket.boxoffice.support.jdbc;

import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

*/
/**
 * Subclass of OracleJdbcSeatingPlanDao to get underlying connection
 * from JBoss 3.0 connection pool.
 *//*

public class JBoss30OracleJdbcBoxOfficeDao extends OracleJdbcBoxOfficeDAO {

	*/
/**
	 * Constructor for Jboss3OracleJdbcSeatingPlanDao.
	 * @param ds
	 *//*

	public JBoss30OracleJdbcBoxOfficeDao(DataSource ds) {
		super(ds);
	}

	*/
/**
	 * Constructor for Jboss3OracleJdbcSeatingPlanDao.
	 * @throws NamingException
	 *//*

	public JBoss30OracleJdbcBoxOfficeDao() throws NamingException {
		super();
	}

	
	*/
/**
	 * Get underlying connection in JBoss 
	 *//*

	protected Connection getOracleConnection(Connection con) throws SQLException {
		//org.jboss.resource.adapter.jdbc.local.ConnectionInPool cp = (org.jboss.resource.adapter.jdbc.local.ConnectionInPool) con;
		org.jboss.resource.adapter.jdbc.local.LocalConnection cp = (org.jboss.resource.adapter.jdbc.local.LocalConnection) con;
		//logger.info("");
		System.out.println("GOT JBOSS CONNECTION " + cp);
		con = cp.getUnderlyingConnection();
		System.out.println("UNDERLYING JBOSS CONNECTION " + con);
		return con;
	}

}
*/
