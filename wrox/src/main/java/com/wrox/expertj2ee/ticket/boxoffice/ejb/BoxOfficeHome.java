
package com.wrox.expertj2ee.ticket.boxoffice.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface BoxOfficeHome extends EJBLocalHome {
	
	BoxOfficeLocal create() throws CreateException;

}
