
package com.wrox.expertj2ee.ticket.command;

import com.wrox.expertj2ee.ticket.exceptions.NoSuchPerformanceException;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.Show;

public interface AvailabilityCheck {

    Show getShowWithAvailability(Show s, boolean acceptCached) throws NoSuchPerformanceException;

    PerformanceWithAvailability getPerformanceWithAvailability(Performance p, boolean acceptCached) throws NoSuchPerformanceException;

}
