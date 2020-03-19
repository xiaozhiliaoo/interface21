
package com.wrox.expertj2ee.ticket.command.support;

import com.wrox.expertj2ee.ticket.command.PriceBandWithAvailability;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.referencedata.support.PriceBandImpl;

public class PriceBandWithAvailabilityImpl extends PriceBandImpl implements PriceBandWithAvailability {

	private int avail;
	
	/**
	 * Constructor for PriceBandWithAvailabilityImpl.
	 * @param priceBandId
	 * @param classId
	 * @param name
	 * @param description
	 * @param price
	 */
	public PriceBandWithAvailabilityImpl(PriceBand pb, int avail) {
		super(pb.getId(), pb.getSeatTypeId(), pb.getName(), pb.getDescription(), pb.getPrice());
		this.avail = avail;
	}

	/**
	 * @see PriceBandWithAvailability#getFreeSeats()
	 */
	public int getFreeSeats() {
		return this.avail;
	}

}
