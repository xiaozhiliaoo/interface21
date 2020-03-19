
package com.wrox.expertj2ee.ticket.command.support;

import com.interface21.beans.factory.InitializingBean;
import com.wrox.expertj2ee.ticket.boxoffice.BoxOffice;
import com.wrox.expertj2ee.ticket.command.AvailabilityCheck;
import com.wrox.expertj2ee.ticket.command.PerformanceWithAvailability;
import com.wrox.expertj2ee.ticket.command.PriceBandWithAvailability;
import com.wrox.expertj2ee.ticket.exceptions.NoSuchPerformanceException;
import com.wrox.expertj2ee.ticket.referencedata.Calendar;
import com.wrox.expertj2ee.ticket.referencedata.Performance;
import com.wrox.expertj2ee.ticket.referencedata.PriceBand;
import com.wrox.expertj2ee.ticket.referencedata.Show;
import com.wrox.expertj2ee.ticket.referencedata.support.ShowImpl;

import java.util.Map;


public class CachingAvailabilityCheck implements AvailabilityCheck, InitializingBean {

    private BoxOffice boxOffice;

    private Calendar calendar;

    private long timeout = 10000L;

    // HASHTABLE?
    private Map performanceCache =
            //new HashMap();
            new java.util.Hashtable();
    //	new ConcurrentReaderHashMap();


    public void setBoxOffice(BoxOffice boxOffice) {
        this.boxOffice = boxOffice;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public void afterPropertiesSet() throws Exception {
        if (boxOffice == null || calendar == null)
            throw new Exception("boxOffice and calendar properties must be set on " + getClass());
    }

    public void setTimeout(int secs) {
        this.timeout = 1000L * secs;
    }

    /**
     * @see Status#getPerformanceWithAvailability(int)
     */
    public PerformanceWithAvailability getPerformanceWithAvailability(Performance p, boolean acceptCached) throws NoSuchPerformanceException {

        Integer key = new Integer(p.getId());

        PerformanceWithAvailability pai = (PerformanceWithAvailability) performanceCache.get(key);
        if (pai == null ||
                this.timeout <= 0L ||
                !acceptCached ||
                System.currentTimeMillis() - pai.getTimeStamp() > this.timeout) {
            pai = reloadPerformanceWithAvailability(p);
            this.performanceCache.put(key, pai);
        }
        return pai;
    }


    private PerformanceWithAvailability reloadPerformanceWithAvailability(Performance p) throws NoSuchPerformanceException {
        int avail = boxOffice.getFreeSeatCount(p.getId());
        PerformanceWithAvailabilityImpl pai = new PerformanceWithAvailabilityImpl(p, avail);
        for (int i = 0; i < p.getPriceBands().size(); i++) {
            PriceBand pb = (PriceBand) p.getPriceBands().get(i);
            avail = boxOffice.getFreeSeatCount(p.getId(), pb.getId());
            PriceBandWithAvailability pba = new PriceBandWithAvailabilityImpl(pb, avail);
            pai.addPriceBand(pba);
        }
        return pai;
    }


    public Show getShowWithAvailability(Show s, boolean acceptCached) throws NoSuchPerformanceException {
        ShowImpl si = new ShowImpl(s);
        for (int i = 0; i < s.getPerformances().size(); i++) {
            Performance p = (Performance) s.getPerformances().get(i);
            PerformanceWithAvailability pa = getPerformanceWithAvailability(p, acceptCached);
            si.addPerformance(pa);
        }
        return si;
    }

}
