
package com.wrox.expertj2ee.ticket.command;

import com.interface21.core.TimeStamped;
import com.wrox.expertj2ee.ticket.referencedata.Performance;

public interface PerformanceWithAvailability extends Performance, TimeStamped {

	int getFreeSeats();
	
}
