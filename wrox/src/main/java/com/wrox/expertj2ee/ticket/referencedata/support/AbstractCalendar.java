
package com.wrox.expertj2ee.ticket.referencedata.support;

import java.util.List;

import com.wrox.expertj2ee.ticket.referencedata.*;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.Show;

public abstract class AbstractCalendar implements Calendar {


	// IMPLEMENTS DATA STRUCTURE
	
	// DOES CACHING REALLY WORK? this won't cache all the time, will it
	
// 	what will reload?!? 

// does this do caching?
	
	
	/**
	 * Constructor for AbstractCalendar.
	 */
	public AbstractCalendar() {
		super();
	}

	

	/**
	 * @see Calendar#getGenres()
	 */
	public List getCurrentGenres() {
		return null;
	}

	/**
	 * @see Calendar#getPerformance(int)
	 */
	public Performance getPerformance(int id) {
		return null;
	}

	/**
	 * @see Calendar#getShow(int)
	 */
	public Show getShow(int id) {
		return null;
	}

}
