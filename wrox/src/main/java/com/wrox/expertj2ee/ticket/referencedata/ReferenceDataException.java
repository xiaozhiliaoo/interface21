
package com.wrox.expertj2ee.ticket.referencedata;

import com.wrox.expertj2ee.ticket.exceptions.FatalException;


/**
 * Unchecked, as this shouldn't happen in normal operation
 */
public class ReferenceDataException extends FatalException {

	/**
	 * Constructor for ReferenceDataException.
	 * @param arg0
	 */
	public ReferenceDataException(String arg0) {
		super(arg0);
	}

}
