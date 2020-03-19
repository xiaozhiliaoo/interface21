/*
package com.wrox.expertj2ee.ticket.client;

import javax.sql.DataSource;
import jdbc.*;
import jdbc.TestDataSource;

import com.interface21.tree.TreeIndex;
import com.wrox.expertj2ee.ticket.command.EjbTicketBusinessDelegate;
import com.wrox.expertj2ee.ticket.command.TicketBusinessDelegate;

import junit.framework.TestCase;

*/
/**
 *
 * @author  rod
 * @version 
 *//*

public class TicketBusinessDelegateTestSuite extends TestCase {

	private TicketBusinessDelegate ticketBD;

	*/
/** Creates new SeatingPlanTest *//*

	public TicketBusinessDelegateTestSuite(String name) {
		super(name);
	}

	*/
/** Run for each test *//*

	protected void setUp() throws Exception {
		ticketBD = new EjbTicketBusinessDelegate();

	}

	protected void tearDown() {
	}

	public void testDirectoryNonNull() throws Exception {
		TreeIndex dir = ticketBD.getTreeIndex();
		assertTrue("ticketBD directory isn't null", dir != null);
		assertTrue("ticketBD directory isn't empty", dir.getNodeCount() > 0);
		System.out.println("Found " + dir.getNodeCount() + " directories");
	}
}*/
