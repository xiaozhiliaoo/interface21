
package com.wrox.expertj2ee.ticket.referencedata.support;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.wrox.expertj2ee.ticket.boxoffice.support.DataAnomaly;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.Show;

public class PerformanceImpl extends AbstractReferenceItem implements Performance {
	
	private Show show;
	
	private List priceBands = new LinkedList();
	
	private	int priceStructure;
	
	private Date when;

	/**
	 * Constructor for GenreImpl.
	 * @param id
	 * @param name
	 */
	public PerformanceImpl(int id, String name, int priceStructure, Date when) {
		super(id, name);
		this.priceStructure = priceStructure;
		this.when = when;
	}
	
	public void setShow(Show s) {
		this.show = s;
	}
	
	/**
	 * Concealed from clients
	 */
	public void addPriceBand(PriceBand pb) {
		priceBands.add(pb);
	}
	
	public void addPriceBands(List l) {
		priceBands.addAll(l);
	}
	
	public int getPriceStructureId() {
		return priceStructure;
	}
	
	/**
	 * @see Performance#getPriceBandForSeatType(int)
	 */
	public PriceBand getPriceBandForSeatType(int seatTypeId) {
		PriceBand pb = null;
		for (int i = 0; i < priceBands.size(); i++) {
			pb = (PriceBand) priceBands.get(i);
			if (pb.getSeatTypeId() == seatTypeId)
				return pb;
		}
		// This is an internal error, not a possible
		// result of invalid input
		throw new DataAnomaly("Unknown seat type " + seatTypeId + " for performance " + getId());
	}

	/**
	 * @see Genre#getShows()
	 */
	public List getPriceBands() {
		return priceBands;
	}
	
	public Date getWhen() {
		return when;
	}
	
	public Show getShow() {
		return show;
	}

}
