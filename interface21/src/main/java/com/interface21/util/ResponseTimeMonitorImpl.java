

package com.interface21.util;

import java.util.Date;

/** 
 * Implementation of ResponseTimeMonitor for use via delegation by
 * objects that implement this interface. Objects implementing only
 * AuditedResource can of course only delegate those methods.
 * <br>Uses no synchronization, so is suitable for use in a web application.
 * @author Rod Johnson
 * @since November 21, 2000
 */
public class ResponseTimeMonitorImpl implements ResponseTimeMonitor {

	public static String REVISION_ID = "$Id: ResponseTimeMonitorImpl.java,v 1.1.1.1 2002/08/01 12:52:04 Rod Johnson Exp $";
    
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	private int               accessCount;	
    private long                initedMillis;
    private int                 totalResponseTimeMillis = 0;
    private int                 bestResponseTimeMillis = Integer.MAX_VALUE;
    private int                 worstResponseTimeMillis = Integer.MIN_VALUE;
    

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates a new ResponseTimeMonitoringAuditedResourceImpl.
     */
	public ResponseTimeMonitorImpl() {        
		initedMillis = System.currentTimeMillis();
	}
    
   
    //---------------------------------------------------------------------
	// Methods from AuditedResource interface
	//---------------------------------------------------------------------
    /** Return the number of hits this object has handled
     * @return the number of hits this object has handled
     */
    public final int getAccessCount() {
        return accessCount;
    }

    /** Return the number of milliseconds since this object was loaded
     * @return the number of milliseconds since this object was loaded
     */
    public final long getUptime() {
        return System.currentTimeMillis() - initedMillis;
    }

    /** Return the date when this object was loaded
     * @return the date when this object was loaded
     */
    public final Date getLoadDate() {
        return new Date(initedMillis);
    }   

    
    //---------------------------------------------------------------------
	// Implementation of ResponseTimeMonitoringAuditedResource
	//---------------------------------------------------------------------    
    /** Return the average response time achieved by this object
     * @return the average response time achieved by this object
     */
    public final int getAverageResponseTimeMillis() {
    	// Avoid division by 0
    	if (getAccessCount() == 0)
    		return 0;
        return totalResponseTimeMillis / getAccessCount();
    }
       
    
    /** Return the best (lowest) response time achieved by this object
     * @return the best (lowest) response time achieved by this object
     */
    public final int getBestResponseTimeMillis() {
        return bestResponseTimeMillis;
    }
    
    /** Return the worst (slowest) response time achieved by this object
     * @return  the worst (slowest) response time achieved by this object
     */
    public final int getWorstResponseTimeMillis() {
        return worstResponseTimeMillis;
    }
	
	 //---------------------------------------------------------------------
	// Additional methods
	//---------------------------------------------------------------------   
	/** Utility method to record this response time, updating
	 * the best and worst response times if necessary
	 * @param responseTime the response time of this request
	 */
	public final void recordResponseTime(long responseTime) {
		++accessCount;
		int iResponseTime = (int) responseTime;
		totalResponseTimeMillis += iResponseTime;
		if (iResponseTime < bestResponseTimeMillis)
			bestResponseTimeMillis = iResponseTime;
		if (iResponseTime > worstResponseTimeMillis)
			worstResponseTimeMillis = iResponseTime;
    }  
	
	public String toString() {
		return "hits=" + getAccessCount() + "; avg=" + getAverageResponseTimeMillis() + "; best=" + getBestResponseTimeMillis() + "; worst=" + getWorstResponseTimeMillis();
	}
    
}	// class ResponseTimeMonitorImpl
