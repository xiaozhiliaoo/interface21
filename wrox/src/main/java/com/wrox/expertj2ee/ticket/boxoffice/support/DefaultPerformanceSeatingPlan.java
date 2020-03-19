package com.wrox.expertj2ee.ticket.boxoffice.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java14.java.util.logging.Logger;

import com.interface21.core.Assertions;
import com.wrox.expertj2ee.ticket.boxoffice.*;
import com.wrox.expertj2ee.ticket.boxoffice.InvalidSeatingRequestException;
import com.wrox.expertj2ee.ticket.boxoffice.NotEnoughContiguousSeatsException;
import com.wrox.expertj2ee.ticket.boxoffice.NotEnoughSeatsException;
import com.wrox.expertj2ee.ticket.boxoffice.Seat;
import com.wrox.expertj2ee.ticket.exceptions.FatalException;
import com.wrox.expertj2ee.ticket.exceptions.NoSuchPerformanceException;
import com.wrox.expertj2ee.ticket.referencedata.*;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;

/**
 * Default implementation of PerformanceSeatingPlan which
 * defers persistence logic to a SeatingPlanDAO.
 * This is a read-only class that creates multiple status classes
 */
public class DefaultPerformanceSeatingPlan implements PerformanceSeatingPlan {

	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	/** Seat, keyed by Integer id */
	private HashMap allSeats;
	
	private BoxOfficeDAO dao;
	
	private int performanceId;
	
	private PriceBand[] priceBands;
	
	
	/**
	 * Cache read-only information for this performance
	 */
	public DefaultPerformanceSeatingPlan(int performanceId, BoxOfficeDAO dao) throws NoSuchPerformanceException {
		this.performanceId = performanceId;
	
		this.dao = dao;
		logger.config("performanceId=" + performanceId + "; seatPlanDataSource=[" + dao + "]");
		
		Set classIdsSet = new TreeSet();
		this.allSeats = new HashMap();

		Collection seats = this.dao.getAllSeats(this.performanceId);
		
		if (seats.size() == 0) 
			throw new NoSuchPerformanceException(performanceId);
		
		for (Iterator itr = seats.iterator(); itr.hasNext(); ) {
			Seat s = (Seat) itr.next();
			this.allSeats.put(new Integer(s.getId()), s);
			classIdsSet.add(new Integer(s.getSeatClassId()));
		}
		
		List pbs = this.dao.getPriceBands(this.performanceId);
		this.priceBands = (PriceBand[]) pbs.toArray(new PriceBand[pbs.size()]);
		
		// Validate price bands?
//		Iterator itr = classIdsSet.iterator();
//		int i = 0;
//		this.classIds = new int[classIdsSet.size()];
//		while (itr.hasNext()) {
//			int classId = ((Integer) itr.next()).intValue();
//			logger.info("Found class id " + classId);
//			this.classIds[i++] = classId;
//		}
	}
	
	
	/**
	 * Return cached information
	 */
	public PriceBand[] getPriceBands() {
		return this.priceBands;
	}
	
	public double getPriceOfSeatType(int classId) {
		double price = 0.0;
		for (int i = 0; i < priceBands.length; i++) {
			if (priceBands[i].getSeatTypeId() == classId)
				return priceBands[i].getPrice();
		}
		// This is an internal error, not a possible
		// result of invalid input
		throw new DataAnomaly("Unknown seat type " + classId + " for performance " + performanceId);
	}
	

	/**
	 * Return cached information
	 */
	public int getSeatCount() {
		return allSeats.size();
	}
	
	
	/**
	 * Dynamic method: uses dao
	 */
	public int getFreeSeatCount() {
		return dao.getFreeSeatCount(this.performanceId);
	}
	
	public int getFreeSeatCount(int classId) {
		return dao.getFreeSeats(this.performanceId, classId, false).size();
	}
	
	//public PerformanceWithAvailability getAvailability() {
	//	PerformanceWithAvailabilityImpl pai = new PerformanceWithAvailabilityImpl(p
	//}
	
	/**
	 * 
	 */
	private void checkClassIdValid(int classId) throws InvalidSeatingRequestException {
		for (int i = 0; i < priceBands.length; i++) {
			if (priceBands[i].getSeatTypeId() == classId)
				return;
		}
		throw new InvalidSeatingRequestException("No such class id - " + classId);
	}


	/**
	 * Get availability of this seat type
	 */
	public SeatTypeAvailability newSeatingStatus(int classId, boolean lock) throws InvalidSeatingPlanDataException, InvalidSeatingRequestException {
		checkClassIdValid(classId);
		return new DefaultSeatType(classId, dao.getFreeSeats(this.performanceId, classId, lock));
	}

	/**
	 * Return the seat or null if not found
	 */
	private Seat getSeatById(int id) {
		return getSeatById(new Integer(id));
	}
	
	private Seat getSeatById(Integer Id) {
		return (Seat) allSeats.get(Id);
	}


	//---------------------------------------------------------------------
	// Inner classes
	//---------------------------------------------------------------------
	/**
	 * An instance of this class is created on each request for seating
	 */
	private class DefaultSeatType implements SeatTypeAvailability {
		
		/**
		 * List of Seat
		 * May be empty: never null 
		 * */
		private List available = new LinkedList();
		
		/**
		 * Primary key of the seat class/type
		 */
		private int classId;

		public DefaultSeatType(int classId, List availableIds) {
			this.classId = classId;
			if (availableIds != null) {
				for (int i = 0; i < availableIds.size(); i++) {
					Integer id = (Integer) availableIds.get(i);
					if (!allSeats.containsKey(id))
						throw new InvalidSeatingPlanDataException("No such seat: " + id);
					available.add(allSeats.get(id));
				}
			}
			logger.info("Created new DefaultSeatType for type " + classId + "; " + available.size() + " seats free");
		}
		
		
		public SeatAllocation allocateSeats(int howMany, boolean mustBeAdjacent) throws InvalidSeatingRequestException,  NotEnoughSeatsException {					
			if (mustBeAdjacent) {
				Seat[] seats = allocateAdjacentSeats(howMany);
				return new SeatAllocation(seats, true);
			}
			else {
				try {
					Seat[] seats = allocateAdjacentSeats(howMany);
					return new SeatAllocation(seats, true);
				}
				catch (NotEnoughContiguousSeatsException ex) {
					// Must retry in two goes
					logger.info("Retrying in two sections");					
					Seat[] block1 = allocateAdjacentSeats(ex.getPossible());
					Seat[] block2 = allocateAdjacentSeats(howMany - ex.getPossible());
					if (block1.length + block2.length != howMany) 
						throw new DataAnomaly("Shouldn't have failed to allocated in two blocks");
					Seat[] combined = new Seat[howMany];
					System.arraycopy(block1, 0, combined, 0, block1.length);
					System.arraycopy(block2, 0, combined, block1.length, block2.length);
					return new SeatAllocation(combined, false);
				}
			}
		}
		
		/**
		 * Assumptions: SEAT ORDERING ASSUMPTIONS
		 * @see PerformancesSeatingPlan#getContiguousSeats(int)
		 */
		private Seat[] allocateAdjacentSeats(int howMany) throws InvalidSeatingRequestException,  NotEnoughSeatsException {					
			if (howMany == 0)
				throw new InvalidSeatingRequestException("Can't request 0 seats");
			if (howMany > getFreeSeatCount())
				throw new NotEnoughSeatsException(this.classId, getFreeSeatCount(), howMany);
				
			// List of Integer
			List bestMatches = new LinkedList();
				
			//Assertions.assertTrue(")
			// List of seats, most recently added first
			List matched = new LinkedList();
			for (int i = 0; i < available.size() && matched.size() != howMany; i++) {
				Seat s = (Seat) available.get(i);
				logger.fine("i=" + i + "; trying new seat " + s);
				Assertions.assertTrue("Available seat " + s + " cannot be null", s != null);
				if (!matched.isEmpty()) {
					Seat lastMatch = (Seat) matched.get(0);
					if (lastMatch.getRight() != s.getId()) {
						bestMatches.add(new Integer(matched.size()));
						matched.clear();
					}
				}
				matched.add(0, s);
				logger.fine("Matched length=" + matched.size() + "; last added is " + s);
			}
			
			if (matched.size() == howMany) {
				// We don't change to array automatically, as we want reverse order
				Seat[] seats = new Seat[howMany];
				for (int i = 0; i < howMany; i++) {
					Seat s = (Seat) matched.get(i);
					seats[howMany - i - 1] = s;
					available.remove(s);
				}				
				return seats;
			} 
			else {
				bestMatches.add(new Integer(matched.size()));
			}
			
			Collections.sort(bestMatches);
			Collections.reverse(bestMatches);
			throw new NotEnoughContiguousSeatsException(this.classId, howMany, bestMatches);
		}	// getContiguousSeats

		/**
		 * @see SeatingStatus#getFreeSeats()
		 */
		public int getFreeSeatCount() {
			return available.size();
		}

	}	// inner class DefaultSeatingStatus

}