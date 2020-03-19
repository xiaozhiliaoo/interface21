
package com.wrox.expertj2ee.ticket.boxoffice.support;

import com.wrox.expertj2ee.ticket.exceptions.FatalException;

public class DataAnomaly extends FatalException {


	/**
	 * Constructor for DataAnomaly.
	 * @param ex
	 */
	public DataAnomaly(String mesg) {
		super(mesg);
	}


}
