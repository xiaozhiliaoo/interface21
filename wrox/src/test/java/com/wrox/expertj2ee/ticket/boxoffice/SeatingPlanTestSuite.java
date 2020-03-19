package com.wrox.expertj2ee.ticket.boxoffice;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.wrox.expertj2ee.ticket.boxoffice.support.*;


import junit.framework.TestCase;
import populate.Populator;

/**
 *
 * @author  rod 
 * @version 
 */
public class SeatingPlanTestSuite extends TestCase {
	
	private static final int ROWS = 10;
	
	private static final int COLS = 8;

	private PerformanceSeatingPlan sp;

	private List seats;

	private int[] allIds;
	
//	private TestSeatingPlanDataSource testSpds;

	public SeatingPlanTestSuite(String name) {
		super(name);
	} 

//	/** Run for each test */
//	protected void setUp() throws Exception {
//		Populator pop = new Populator();
//		this.seats = pop.populate(ROWS, COLS);
//		this.allIds = pop.getAllIds();
//		//	 this.seats = (Seat[]) l.toArray(new Seat[l.size()]);
//		int performanceId = 1;
//		testSpds = new TestSeatingPlanDataSource(this.seats);
//		sp = new DefaultSeatingPlan(performanceId, testSpds);
//	}

//	private class TestSeatingPlanDataSource implements SeatingPlanDataSource {
//		private List seats;
//		
//		private List unavailable = new LinkedList();
//		
//		public TestSeatingPlanDataSource(List seats) {
//			this.seats = seats;
//		}
//		public Collection getAllSeats(int performanceId) {
//			return seats;
//		}
//
//		public List getFreeSeats(int performanceId, int classId) {
//			List l = new LinkedList();
//			for (int i = 0; i < seats.size(); i++) {
//				Seat s = (Seat) seats.get(i);
//				if (s.getSeatClassId() == classId) {
//					if (isAvailable(s.getId())) {
//						l.add(new Integer(s.getId()));
//					}
//				}
//			}
//			return l;
//		}
//		
//		public void unavailable(int id) {
//			unavailable.add(new Integer(id));
//		}
//		
//		public boolean isAvailable(int id) {
//			return !unavailable.contains(new Integer(id));
//		}
//		
//		public void clear() {
//			unavailable.clear();
//		}
//
//		/**
//		 * @see SeatingPlanDataSource#getFreeSeatCount(int)
//		 */
//		public int getFreeSeatCount(int performanceId) {
//			return seats.size();
//		}
//	};
//	
//	private int countSeatsInClass(int classId) {
//		int count = 0;
//		for (int i = 0; i < seats.size(); i++) {
//			Seat s = (Seat) seats.get(i);
//			if (s.getSeatClassId() == classId)
//				++count;
//		}
//		return count;
//	}
//
//	protected void tearDown() {
//	}
//
//	public void testCounts() throws Exception {
//		assertTrue(
//			"seat count is correct: should have " + seats.size() + " seats, not " + sp.getSeatCount(),
//			seats.size() == sp.getSeatCount());
//			
//		int []classIds = sp.getClassIds();
//		for (int i = 0; i < classIds.length; i++) {
//			System.out.println("Checking count in class " + classIds[i] + "; expecting " + countSeatsInClass(classIds[i]));
//			ClassSeatingStatus ss = sp.newSeatingStatus(classIds[i]);
//			assertTrue("count in class "+ classIds[i] + " is correct: says " + ss.getFreeSeats() + ", expecting " + countSeatsInClass(classIds[i]), ss.getFreeSeats() == countSeatsInClass(classIds[i]));
//		}
////		assertTrue("0 seat status count is correct", ss.getFreeSeats() == 0);
////		ss = sp.newSeatingStatus(null);
////		assertTrue("null seat status count is correct", ss.getFreeSeats() == 0);
//	}
//
//	// Must return l to right
//	private Seat[] testAreFound(int requestedClass, int howMany) throws Exception {
//		ClassSeatingStatus ss = sp.newSeatingStatus(requestedClass);
//		Seat[] seats = ss.getContiguousSeats(howMany);
//		assertTrue("getContiguousSeats should never return null", seats != null);
//
//		assertTrue("Found correct number of seats", howMany == seats.length);
//		// Test they are contiguous: should be returned from left to right
//		Seat s = seats[0];
//		for (int i = 1; i < seats.length; i++) {
//			assertTrue("Not contiguous: " + i + "th seat " + seats[i] + " must be R of " + s, s.getRight() == seats[i].getId());
//			assertTrue("Seat must be of requested class", seats[i].getSeatClassId() == requestedClass);
//			System.out.println(s);
//			s = seats[i];
//		}
//		return seats;
//	}
//	
//	public void testAreFound() throws Exception {
//		testAreFound(1, 1);
//		testAreFound(1, 2);
//		testAreFound(1, 3);
//		testAreFound(1, 4);
//		testAreFound(2, 1);
//		
//	}
//	
//	
//	/**
//	 * Break some rows
//	 */
//	public void testSomeUnavailable() throws Exception {
//		
//		Seat[] seats = testAreFound(1, 5);
//		assertTrue("First is in A row", seats[0].getName().startsWith("A"));
//		
//		// Break first row
//		testSpds.unavailable(4);
//		seats = testAreFound(1, 5);
//		assertTrue("First is in B row", seats[0].getName().startsWith("B"));
//		
//		// Break second row
//		testSpds.unavailable(11);
//		seats = testAreFound(1, 5);
//		assertTrue("First is in C row", seats[0].getName().startsWith("C"));
//		
//		// Break third row
//		testSpds.unavailable(19);
//		try {
//			testAreFound(1, 5);
//			fail("Shouldn't be able to allocate 5 in class 1");
//		}
//		catch (NotEnoughContiguousSeatsException ex) {
//			assertTrue("requested 5", ex.getRequested() == 5);
//			assertTrue("requested class 1", ex.getClassId() == 1);
//			assertTrue("possible=3", ex.getPossible() == 3);
//		}
//	}
//
//	public void testRequestMoreThanInHall() throws Exception {
//		int classId = 1;
//		int requested = seats.size() + 1;
//		ClassSeatingStatus ss = sp.newSeatingStatus(classId);
//		try {
//			ss.getContiguousSeats(requested);
//			fail("getContiguousSeats should never find more seats than in hall");
//		}
//		catch (NotEnoughSeatsException ex) {
//			// Correct
//			assertTrue("Expected poss=" + countSeatsInClass(classId) + ", not " + ex.getPossible(), ex.getPossible() == countSeatsInClass(classId));
//			assertTrue("Expected howMany=" + requested, ex.getRequested() == requested);
//		}
//	} 
//
//	public void testRequestMoreThanInRow() throws Exception {
//		ClassSeatingStatus ss = sp.newSeatingStatus(1);
//		try {
//			ss.getContiguousSeats(COLS + 1);
//			fail("getContiguousSeats should never find more seats than in row");
//		}
//		catch (NotEnoughContiguousSeatsException ex) {
//			// Correct
//			assertTrue("Expected poss=" + (COLS - 3) + ", not " + ex.getPossible(), ex.getPossible() == COLS - 3);
//			assertTrue("Expected howMany=" + (COLS + 1), ex.getRequested() == COLS + 1);
//		}
//	}
//
//	/**
//	 * Expects a runtime exception (fatal)
//	 */
//	public void testRequest0Seats() throws Exception {
//		ClassSeatingStatus ss = sp.newSeatingStatus(1);
//		try {
//			ss.getContiguousSeats(0);
//			fail("getContiguousSeats should never find 0 seats");
//		}
//		catch (InvalidSeatingRequestException ex) {
//		}
//	}
//	
//	public void testRequestBogusClass() throws Exception {
//		try {
//			ClassSeatingStatus ss = sp.newSeatingStatus(100);
//			ss.getContiguousSeats(10);
//			fail("getContiguousSeats should never find seats in bogus class");
//		}
//		catch (InvalidSeatingRequestException ex) {
//		}
//	}
//
//	//	public void testRequestMoreThanInClass() throws Exception {
//	//		try {
//	//			sp.getContiguousSeats(1, seats.length + 1);
//	//			fail("getContiguousSeats should never find more seats than in hall");
//	//		}
//	//		catch (NotEnoughSeatsException ex) {
//	//			// Correct
//	//		}
//	//	}
//
//	//public void testNoSuchClass() {
//
//	//}

}