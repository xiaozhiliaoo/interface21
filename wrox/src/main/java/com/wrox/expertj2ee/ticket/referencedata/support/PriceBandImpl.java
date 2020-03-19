
package com.wrox.expertj2ee.ticket.referencedata.support;

import com.wrox.expertj2ee.ticket.referencedata.PriceBand;

/**
 * SCO: no id. Must be returned by a performance.
 */
public class PriceBandImpl extends SeatTypeImpl implements PriceBand {
	
	
	private int seatTypeId;
	
	private double price;

	/**
	 * Constructor for PriceBand.
	 */
	public PriceBandImpl(int priceBandId, int classId, String name, String description, double price) {
		super(priceBandId, name, description);
		this.price = price;
		this.seatTypeId = classId;
	}
	
	public double getPrice() {
		return price;
	}
	
	
	/**
	 * Override, as seat type id isn't id
	 */
	public int getSeatTypeId() {
		return this.seatTypeId;
	}

}
