package com.wrox.expertj2ee.ticket.referencedata.support;

import javax.jms.Message;
import javax.jms.MessageListener;

import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import com.wrox.expertj2ee.ticket.referencedata.Calendar;

public class DataUpdateJmsListener implements MessageListener {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	private CachingCalendar calendar;

	/**
	 * Constructor for TestListener.
	 */
	public DataUpdateJmsListener() {
		super();
	}

	public void setCachingCalendar(CachingCalendar calendar) {
		this.calendar = calendar; 
	}

	/**
	 * @see MessageListener#onMessage(Message)
	 */
	public void onMessage(Message m) {
		try {
			logger.info("Refresing calendar in response to JMS message [" + m + "]");
			calendar.refresh();
		}
		catch (Exception ex) {
			// BEWARE OF THROWING RTEs: kills listener
			logger.logp(Level.SEVERE, getClass().getName(), "onMessage", "Failed to refresh calendar", ex);
		}

	}

}