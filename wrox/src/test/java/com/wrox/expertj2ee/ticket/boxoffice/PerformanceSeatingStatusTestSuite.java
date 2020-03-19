/*
package com.wrox.expertj2ee.ticket.boxoffice;


import com.wrox.expertj2ee.ticket.boxoffice.support.*;
import com.wrox.expertj2ee.ticket.referencedata.support.PriceBandImpl;
import junit.framework.TestCase;

import java.util.*;

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
 * TEST CONTIGUOUS ONLY
 *//*

public class PerformanceSeatingStatusTestSuite extends TestCase {


//	private PerformanceSeatingStatus

	
	PerformanceSeatingPlan sp;
	
	DummyBoxOfficeDAO dao;
	
	private class DummyBoxOfficeDAO implements BoxOfficeDAO {
		
		private List seats = new LinkedList();
		private List pbList = new LinkedList();
		
		private Set taken = new HashSet();
		
		public DummyBoxOfficeDAO() {
			
			seats.add(new Seat(1, "A1", 1, 0, 2));
			seats.add(new Seat(2, "A2", 1, 1, 3));
			seats.add(new Seat(3, "A3", 1, 2, 4));
			seats.add(new Seat(4, "A4", 1, 3, 0));
			seats.add(new Seat(5, "B1", 1, 0, 6));
			seats.add(new Seat(6, "B2", 1, 5, 7));
			seats.add(new Seat(7, "B3", 1, 6, 8));
			seats.add(new Seat(8, "B4", 1, 7, 0));
			
			seats.add(new Seat(9, "C1", 2, 0, 10));
			seats.add(new Seat(10, "C2", 2, 9, 11));
			seats.add(new Seat(11, "C3", 2, 10, 12));
			seats.add(new Seat(12, "C4", 2, 11, 0));
			seats.add(new Seat(13, "D1", 2, 0, 14));
			seats.add(new Seat(14, "D2", 2, 13, 15));
			seats.add(new Seat(15, "D3", 2, 14, 16));
			seats.add(new Seat(16, "D4", 2, 15, 0));
			
			pbList.add(new PriceBandImpl(1,  1, "A", "Premium", 10));
			pbList.add(new PriceBandImpl(2, 2, "B", "OK", 5));
		}
		
		public void clear() {
			taken.clear();
		}
		
		private Seat findByName(String name) {
			for (int i = 0; i < seats.size(); i++) {
				Seat s = (Seat) seats.get(i);
				if (s.getName().equals(name))
					return s;
			}
			throw new RuntimeException("Failure: couldn't find seat with name '" + name + "'");
		}
		
		*/
/**
		 * @see BoxOfficeDAO#createPurchaseRecord(Reservation)
		 *//*

		public int createPurchaseRecord(Reservation quote) throws ExpiredReservationTakenException {
			throw new UnsupportedOperationException();
		}

		*/
/**
		 * @see BoxOfficeDAO#getAllSeats(int)
		 *//*

		public List getAllSeats(int performanceId) {
			return seats;
		}

		*/
/**
		 * @see BoxOfficeDAO#getFreeSeatCount(int)
		 *//*

		public int getFreeSeatCount(int performanceId) {
			return seats.size() - taken.size();
		}
		
		public void reserve(String name) {
			taken.add(findByName(name));
			
		}

		*/
/**
		 * @see BoxOfficeDAO#getFreeSeats(int, int, boolean)
		 *//*

		public List getFreeSeats(int performanceId, int classId, boolean lock) {
			List l = new LinkedList();
			for (int i = 0; i < seats.size(); i++) {
				Seat s = (Seat) seats.get(i);
				if (s.getSeatClassId() == classId && !taken.contains(s))
					l.add(new Integer(s.getId()));
			}
			System.out.println("FOUND " + l.size() + " free seats in class " + classId);
			return l;
		}

		*/
/**
		 * @see BoxOfficeDAO#getPriceBands(int)
		 *//*

		public List getPriceBands(int performanceId) {
			return pbList;
		}

		*/
/**
		 * @see BoxOfficeDAO#getSeatIdsForBooking(String)
		 *//*

		public int[] getSeatIdsForBooking(String bookingId) {
			 	throw new UnsupportedOperationException();
		}

		*/
/**
		 * @see BoxOfficeDAO#reserve(Reservation)
		 *//*

		public String reserve(Reservation quote) {
			 	throw new UnsupportedOperationException();
		}

		*/
/**
		 * @see BoxOfficeDAO#setAuthorizationCode(int, String)
		 *//*

		public Date setAuthorizationCode(int purchaseId, String authorizationCode) {
			 	throw new UnsupportedOperationException();
		}

	}
	
	public PerformanceSeatingStatusTestSuite(String name) {
		super(name);
	}
	
	*/
/** Run for each test *//*

	protected void setUp() throws Exception {
		dao = new DummyBoxOfficeDAO();
		sp = new DefaultPerformanceSeatingPlan(1, dao);
	}

	protected void tearDown() {
	}
	
	private void testCount(int pid) throws Exception {
//		int dbTotal = jh.runSQLFunction("select count(fk_seat_id) from seat_status where fk_performance_id=" + pid);
//		assertTrue("DB says total=" + dbTotal + "; sp says " + sp.getSeatCount(), dbTotal == sp.getSeatCount());
//		int dbFree = jh.runSQLFunction("select count(*) from seat_status where fk_performance_id=" + pid + " and fk_booking_id is null");
//		assertTrue("DB says free=" + dbFree + "; sp says " + sp.getFreeSeatCount(), dbFree == sp.getFreeSeatCount());
	}
	
	
	public void testPriceBands() throws Exception {
//		int pid = 1;
//		PriceBand[] pbs = sp.getPriceBands();
//		assertTrue("Found > 0 pricebands", pbs.length > 0);
//		for (int i = 0; i < pbs.length; i++) {
//			boolean dbFound = jh.runSQLFunction("select count(*) from seat_status where fk_performance_id = " + pid + " and fk_price_band_id=" + pbs[i].getClassId()) > 0;
//			assertTrue("db says its a priceband", dbFound);
//			// CHECK PRICE!?
//		}
	}
	

	
	public void testCounts() throws Exception {
		testCount(1);
		testCount(2);
	}
	
	private SeatAllocation testAreFound(int requestedClass, int howMany, boolean mustBeContiguous) throws Exception {
		SeatTypeAvailability ss = sp.newSeatingStatus(requestedClass, false);
		SeatAllocation sa = ss.allocateSeats(howMany, false);
		Seat[] seats = sa.getSeats();
		assertTrue("getContiguousSeats should never return null", seats != null);

		assertTrue("Found correct number of seats", howMany == seats.length);
		
		List ids = new LinkedList();
		for (int i = 0; i < seats.length; i++) {
			if (ids.contains(seats[i]))
				fail("Duplicate seat in list " + seats[i]);
			ids.add(seats[i]);
			assertTrue("Seat must be of requested class", seats[i].getSeatClassId() == requestedClass);
			System.out.println(seats[i]);
		}
		
		if (mustBeContiguous) {
		// Test they are contiguous: should be returned from left to right
		Seat s = seats[0];
		for (int i = 1; i < seats.length; i++) {
			assertTrue("Not contiguous: " + i + "th seat " + seats[i] + " must be R of " + s, s.getRight() == seats[i].getId());
			System.out.println(s);
			s = seats[i];
		}
		}
		return sa;
	}
	
	// Must return l to right
	public void testAreFound() throws Exception {
		testAreFound(1, 1, true);
		testAreFound(1, 2, true);
		testAreFound(1, 3, true);
		testAreFound(2, 1, true);
		testAreFound(2, 2, true);
	} 
	
	private void testAreNotFound(int requestedClass, int howMany, boolean adjacentOnly) throws Exception {
		SeatTypeAvailability ss = sp.newSeatingStatus(requestedClass, false);
		try {
			ss.allocateSeats(howMany, adjacentOnly);
			fail("Shouldn't have found " + howMany + " seats in class " + requestedClass);
		}
		catch (NotEnoughSeatsException ex) {
			assertTrue("asked for", ex.getRequested() == howMany);
			assertTrue(ex.getClassId() == requestedClass);
		}	
	}
	
	public void testAreNotFound() throws Exception {
		testAreNotFound(1, 9, false);
		testAreNotFound(1, 9, true);
		testAreNotFound(2, 13, false);
		testAreNotFound(2, 13, true);
	} 
	
	public void testAreFoundButNotAdjacent() throws Exception {
		SeatAllocation sa = testAreFound(1, 5, false);
		assertTrue("aren't adjacent", !sa.adjacent());
		sa =testAreFound(2, 5, false);
		assertTrue("aren't adjacent", !sa.adjacent());
		sa =testAreFound(2, 3, true);
			assertTrue("are adjacent", sa.adjacent());
	} 
	
	public void testAreFoundButNotAdjacentUnacceptable() throws Exception {
		SeatTypeAvailability ss = sp.newSeatingStatus(1, false);
		try {
			SeatAllocation sa = ss.allocateSeats(5, true);
			Seat[] seats = sa.getSeats();
			fail ("Can't find 5 adjacent seats in class 1");
		}
		catch (NotEnoughContiguousSeatsException ex) {
			assertTrue("Could get 4 contiguous", ex.getPossible() == 4);
			assertTrue("2 contiguous runs", ex.getBestMatches().length == 2);
			assertTrue("contiguous runs each of length 4", ex.getBestMatches()[0] == ex.getBestMatches()[1]);
			assertTrue("contiguous runs each of length 4", ex.getBestMatches()[0] == 4);
			checkBestMatches(ex.getBestMatches(), ss.getFreeSeatCount());
		}
	} 
	
	private void checkBestMatches(int[] bestMatches, int expectedTotal) {
		int count = 0;
		int last;
		last = bestMatches[0];
		for (int i = 0; i < bestMatches.length; i++) {
				if (bestMatches[i] > last)
					fail("Best matches isn't in descending order");
				last = bestMatches[i];
				count += bestMatches[i];
			}
			assertTrue("Count matches free seats", expectedTotal == count);
	}
	
	
	public void testAreNotFoundContiguousWithReservation() throws Exception {
//		dao.reserve("A1");
//		dao.reserve("B2");
//		testAreNotFoundContiguous(1, 4);
//		dao.reserve("C1");
//		dao.reserve("D3");
//		testAreNotFoundContiguous(2, 4);
	} 
	
//	public void testRequestMoreThanInHall() throws Exception {
//		int request = 10000;
//		try {
//			SeatingStatus ss = sp.newSeatingStatus(1);
//			ss.getContiguousSeats(request);
//			fail("getContiguousSeats should never find more seats than in hall");
//		}
//		catch (NotEnoughSeatsException ex) {
//			// Correct
//			assertTrue("Expected poss=" + 12 + ", not " + ex.getPossible(), ex.getPossible() == 12);
//			assertTrue("Expected howMany=" + request, ex.getRequested() == request);
//		}
//	}
	
//	public void testRequestMoreThanInRow() throws Exception {
//		SeatingStatus ss = sp.newSeatingStatus(allIds);
//		try {
//			ss.getContiguousSeats(9);
//			fail("getContiguousSeats should never find more seats than in row");
//		}
//		catch (NotEnoughContiguousSeatsException ex) {
//			// Correct
//			assertTrue("Expected poss=" + 8 + ", not " + ex.getPossible(), ex.getPossible() == 8);
//			assertTrue("Expected howMany=" + 9, ex.getRequested() == 9);
//		}
//	}
//	
//	
//	*/
/**
//	 * Expects a runtime exception (fatal)
//	 *//*

//	public void testRequest0Seats() throws Exception {
//		SeatingStatus ss = sp.newSeatingStatus(allIds);
//		try {
//			ss.getContiguousSeats(0); 
//			fail("getContiguousSeats should never find 0 seats");
//		}
//		catch (InvalidSeatingRequestException ex) {
//		}
//	}
	

	
//	public void testRequestMoreThanInClass() throws Exception {
//		try {
//			sp.getContiguousSeats(1, seats.length + 1);
//			fail("getContiguousSeats should never find more seats than in hall");
//		}
//		catch (NotEnoughSeatsException ex) {
//			// Correct
//		}
//	}
	
	
	//public void testNoSuchClass() {
		
	//}

	

}*/
