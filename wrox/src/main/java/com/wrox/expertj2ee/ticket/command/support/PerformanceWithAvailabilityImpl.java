
package com.wrox.expertj2ee.ticket.command.support;

import java.util.Date;

import com.interface21.core.TimeStamped;
import com.wrox.expertj2ee.ticket.command.PerformanceWithAvailability;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.support.PerformanceImpl;

public class PerformanceWithAvailabilityImpl extends PerformanceImpl implements PerformanceWithAvailability {

	private int avail;
	
	private long timestamp = System.currentTimeMillis();
	
	/**
	 * Constructor for PerformanceWithAvailabilityImpl.
	 * Must add price bands
	 * @param id
	 * @param name
	 * @param priceStructure
	 * @param when
	 */
	public PerformanceWithAvailabilityImpl(Performance p, int avail) {
		super(p.getId(), p.getName(), 0, p.getWhen());
		setShow(p.getShow());
		this.avail = avail;
	}

	/**
	 * @see PerformanceWithAvailability#getFreeSeats()
	 */
	public int getFreeSeats() {
		return this.avail;
	}
	
	public long getTimeStamp() {
		return timestamp;
	}

}
