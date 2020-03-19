
package com.wrox.expertj2ee.ticket.framework.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

import com.interface21.ejb.access.AbstractLocalStatelessSessionServiceLocator;
import com.wrox.expertj2ee.ticket.boxoffice.BoxOffice;
import com.wrox.expertj2ee.ticket.boxoffice.ejb.BoxOfficeHome;
import com.wrox.expertj2ee.ticket.exceptions.FatalException;
import com.wrox.expertj2ee.ticket.framework.BoxOfficeFactory;

/**
 * Bean: must set JNDI name property
 */
public class LocalSLSBBoxOfficeFactory extends AbstractLocalStatelessSessionServiceLocator implements BoxOfficeFactory {

	private BoxOfficeHome	home; 

	/**
	 * @see BoxOfficeFactory#getBoxOffice()
	 */
	public BoxOffice getBoxOffice() throws FatalException {
		try {
			return home.create();
		}
		catch (CreateException ex) {
			throw new EjbConnectionFailure("Cannot create BoxOffice EJB from home", ex);
		}
	}

	/**
	 * @see AbstractLocalStatelessSessionServiceLocator#init()
	 */
	protected void setEjbHome(EJBLocalHome home) { 
		this.home = (BoxOfficeHome) home;
	}

}
