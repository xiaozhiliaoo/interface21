
package com.wrox.expertj2ee.ticket.referencedata;

import java.util.List;


/**
 * Can't delete something:
 * can add, however
 */
public interface Calendar {
	
	/** 
	 * Genres even with nothing showing
	 * These are deep objects
	 */
	List getCurrentGenres();
	
	/** 
	 * Genres even with nothing showing
	 * These are deep objects
	 */
	List getAllGenres();
	
	
	/**
	 * @throws ReferenceDataException if not found
	 */
	Show getShow(int id) throws ReferenceDataException;
	
	Performance getPerformance(int id) throws ReferenceDataException;
	
	/** Date of last modification */
	long getLastModified();
	
	
	/** NEEDS TO USE BOX OFFICE!? */
//	int getAvailableSeats(int performance, int type);

}
