
package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Interface to be implemented by classes that can provide a connection to
 * a relational database. Extends the javax.sql.DataSource interface
 * to allow classes using it to query whether or not the connection should
 * be closed after a given operation.
 * @author  Rod Johnson
 * @version $Id: SmartDataSource.java,v 1.1.1.1 2002/08/01 12:52:03 Rod Johnson Exp $
 */
public interface SmartDataSource extends DataSource {
	
	
	/** 
	 * Should we close this connection, obtained from this factory?
	 * Code that uses connections from the factory should always
	 * use code like 
	 * <code>
	 * if (factory.shouldClose(conn)) 
	 * 	con.close()
	 * </code>
	 * in a finally block
	 * @param conn connection, which should have been obtained
	 * from this data source, to check closure status of
	 */
	boolean shouldClose(Connection conn);

}

