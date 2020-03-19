
package com.wrox.expertj2ee.ticket.referencedata;

public interface SeatType extends ReferenceItem {

	/** Actually the same as id */
	int getSeatTypeId();
	
	/** Name is code */
	String getDescription(); 
}
