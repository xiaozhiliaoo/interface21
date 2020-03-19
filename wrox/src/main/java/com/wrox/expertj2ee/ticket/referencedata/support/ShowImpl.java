
package com.wrox.expertj2ee.ticket.referencedata.support;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.wrox.expertj2ee.ticket.referencedata.SeatType;
import com.wrox.expertj2ee.ticket.referencedata.Show;
import com.wrox.expertj2ee.ticket.referencedata.Performance;

public class ShowImpl extends AbstractReferenceItem implements Show {
	
	private List performances = new LinkedList();
	
	private List seatTypes = new LinkedList();
	
	private int seatingPlanId;

	/**
	 * Constructor for GenreImpl.
	 * @param id
	 * @param name
	 */
	// GENRE ID AS IMPL!?
	public ShowImpl(int id, String name, int seatingPlanId) {
		super(id, name);
		this.seatingPlanId = seatingPlanId;
	}
	
	/**
	 * Must add performances
	 */
	public ShowImpl(Show other) {
		this(other.getId(), other.getName(), other.getSeatingPlanId());
		this.seatTypes = Collections.unmodifiableList(other.getSeatTypes());
	}
	
	
	/**
	 * Concealed from clients
	 */
	public void addPerformance(Performance p) {
		performances.add(p);
	}
	
	public void addSeatType(SeatType st) {
		seatTypes.add(st);
	}

	/**
	 * @see Genre#getShows()
	 */
	public List getPerformances() {
		return performances;
	}
	
	

	/**
	 * @see Show#getSeatTypes()
	 */
	public List getSeatTypes() {
		return seatTypes;
	}

	/**
	 * @see Show#getSeatingPlanId()
	 */
	public int getSeatingPlanId() {
		return this.seatingPlanId;
	}

}
