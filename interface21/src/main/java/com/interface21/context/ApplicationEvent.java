package com.interface21.context;

import java.util.EventObject;

/**
 * Class to be extended by all application events.
 * @author  Rod Johnson
 * @since January 17, 2001
 * @version $RevisionId$
 */
public class ApplicationEvent extends EventObject {

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** System time when the event happened */
	private long	timestamp;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
    /** Creates a new ApplicationEvent
     * @param source component that published the event
     */
	public ApplicationEvent(Object source) {
        super(source);
		timestamp = System.currentTimeMillis();
	}

	//---------------------------------------------------------------------
	// Public methods
	//---------------------------------------------------------------------
	/** Return the system time in milliseconds when the event happened
	 * @return the system time in milliseconds when the event happened
	 */
	public long getTimestamp() {
		return timestamp;
	}

}	// class ApplicationEvent
