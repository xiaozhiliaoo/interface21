package com.interface21.web.context.support;

import com.interface21.context.ApplicationEvent;
import com.interface21.context.ApplicationListener;
import com.interface21.util.ResponseTimeMonitor;
import com.interface21.util.ResponseTimeMonitorImpl;
import com.interface21.web.context.RequestHandledEvent;
import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;


/**
 *
 * @author  Rod Johnson
 * @since January 21, 2001
 * @version $RevisionId$
 */
public class PerformanceMonitorListener implements ApplicationListener {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	protected final Logger logger = Logger.getLogger(getClass().getName());

	private ResponseTimeMonitorImpl responseTimeMonitor;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	public PerformanceMonitorListener() {
		responseTimeMonitor = new ResponseTimeMonitorImpl();
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationListener
	//---------------------------------------------------------------------
	/**
	 * Ignore log events
	 */
	public void onApplicationEvent(ApplicationEvent e) {
		if (e instanceof RequestHandledEvent) {
			RequestHandledEvent rhe = (RequestHandledEvent) e;
			// ONE MONITOR PER URL
			responseTimeMonitor.recordResponseTime(rhe.getTimeMillis());
			if (logger.isLoggable(Level.INFO)) {
				// Stringifying objects is expensive. Don't do it unless it will show.
				logger.info("PerformanceMonitorListener: last=" + rhe.getTimeMillis() + "ms; " + responseTimeMonitor + "; client was " + rhe.getIP());
			}
		}
	}

	private void log(String s) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info(s);
		}
	}

	//public void setWebApplicationManager(WebApplicationManager webApplicationManager) {
	//	this.webApplicationManager = webApplicationManager;
	//}

	public ResponseTimeMonitor getResponseTimeMonitor() {
		return responseTimeMonitor;
	}

}	// class PerformanceMonitorListener
