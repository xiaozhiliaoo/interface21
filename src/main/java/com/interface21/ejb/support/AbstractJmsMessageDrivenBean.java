
package com.interface21.ejb.support;

import javax.jms.MessageListener;

/** 
 * Convenient superclass for JMS MDBs.
 * Requires subclasses to implement JMS MessageListener
 * @author Rod Johnson
 * @version $RevisionId: ResultSetHandler.java,v 1.1 2001/09/07 12:48:57 rod Exp $
 */
public abstract class AbstractJmsMessageDrivenBean extends AbstractMessageDrivenBean
	implements MessageListener {
	
	// Empty
	
} 
