
package com.wrox.expertj2ee.ticket.framework;

import com.wrox.expertj2ee.ticket.boxoffice.BoxOffice;
import com.wrox.expertj2ee.ticket.exceptions.FatalException;

public interface BoxOfficeFactory {

	BoxOffice getBoxOffice() throws FatalException;

}
