/*
package com.wrox.expertj2ee.ticket.boxoffice;

import java.util.List;

import javax.naming.spi.NamingManager;
import javax.sql.DataSource;
import jdbc.*;
import jdbc.TestDataSource;

import com.interface21.jdbc.core.JdbcHelper;
import com.wrox.expertj2ee.junitee.BoxOfficeTestSuite;
import com.wrox.expertj2ee.ticket.boxoffice.ejb.BoxOfficeEJB;
import com.wrox.expertj2ee.ticket.boxoffice.support.*;

import junit.framework.TestCase;
import mo.ejb.MockCmtEJBSessionContext;
import mo.ejb.MockInitialContextFactoryBuilder;
import populate.Populator; 

*/
/**
 *
 *  ID NAME    CLASS_ID       LEFT      RIGHT
--- ----- ---------- ---------- ----------
  1 A1             2                     2
  2 A2             1          1          3
  3 A3             1          2          4
  4 A4             1          3          5
  5 A5             2          4          6
  6 A6             2          5
  7 B1             2                     8
  8 B2             1          7          9
  9 B3             1          8         10
 10 B4             1          9         11
 11 B5             2         10         12

 ID NAME    CLASS_ID       LEFT      RIGHT
--- ----- ---------- ---------- ----------
 12 B6             2         11
 * @author  rod 
 * @version 
 *//*

public class ExternalBoxOfficeEjbTestSuite extends BoxOfficeTestSuite {
	
	static {
		// Only allowed to do this once
		try {
			NamingManager.setInitialContextFactoryBuilder(new MockInitialContextFactoryBuilder());
			DataSource testDs = new TestDataSource();
			MockInitialContextFactoryBuilder.bind("java:comp/env/jdbc/ticket-ds", testDs);
		}
		catch (Exception ex) {
			System.out.println("FATAL FATAL PROBLEM");
		}
	}


//	private PerformanceSeatingStatus

	DataSource ds = new TestDataSource();
	
	JdbcHelper jh = new JdbcHelper(ds);
	
	
	
	public ExternalBoxOfficeEjbTestSuite(String name) throws Exception {
		super(name);
	}
	

	
	protected BoxOffice newBoxOffice() throws Exception {
		BoxOfficeEJB ejb = new BoxOfficeEJB();
		
		MockCmtEJBSessionContext sc = new MockCmtEJBSessionContext(true);
		ejb.setSessionContext(sc);
		ejb.ejbCreate();
		
		return ejb;
	}

	protected void tearDown() {
	}
	

}*/
