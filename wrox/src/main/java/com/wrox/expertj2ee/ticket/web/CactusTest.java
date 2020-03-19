/*
package com.wrox.expertj2ee.ticket.web;

import com.interface21.beans.factory.BeanFactory;
import com.interface21.web.servlet.FrameworkServlet;
import com.wrox.expertj2ee.ticket.boxoffice.BoxOffice;
import com.wrox.expertj2ee.ticket.boxoffice.ejb.BoxOfficeHome;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;

import javax.naming.Context;
import javax.naming.InitialContext;

public class CactusTest extends ServletTestCase {
	
	private BoxOffice boxOffice;

	*/
/**
	 * Constructor for CactusTest.
	 * @param arg0
	 *//*

	public CactusTest(String arg0) {
		super(arg0);
	}

	public static Test suite() {
		return new TestSuite(CactusTest.class);
	}

	public void setUp() throws Exception {
		Context ctx = new InitialContext();
		BoxOfficeHome home =
			(BoxOfficeHome) ctx.lookup("java:comp/env/ejb/BoxOffice");
		this.boxOffice = home.create();
	}

	public void testCounts() throws Exception {
		int all = boxOffice.getSeatCount(1);
		int free = boxOffice.getFreeSeatCount(1);
		assertTrue("all > o", all > 0);
		assertTrue("all > free", all >= free);
	}
	
	
	public void test2() throws Exception {
		// Use a cactus implicit property
		BeanFactory bf = (BeanFactory) config.getServletContext().getAttribute(FrameworkServlet.SERVLET_CONTEXT_PREFIX + "ticket");
		assertTrue("bean factory is found", bf != null);
		
		Object o = bf.getBean("calendar");
		assertTrue("Found calendar", o != null);
	}

}*/
