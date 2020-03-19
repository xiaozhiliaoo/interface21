
package com.wrox.expertj2ee.ticket.command;

import com.wrox.expertj2ee.ticket.referencedata.PriceBand;

public interface PriceBandWithAvailability extends PriceBand {
	
	int getFreeSeats();

}
