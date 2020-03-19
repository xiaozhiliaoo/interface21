
package com.interface21.ejb.support;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;


/** 
 * Convenient superclass for session beans.
 * Remembers session context.
 * NB: cannot use final
 * @author Rod Johnson
 * @version $RevisionId$
 */
public abstract class AbstractSessionBean extends AbstractEnterpriseBean implements SessionBean {
	
	
	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
	
	/** SessionContext passed to this object */
	private SessionContext	sessionContext;
	
	
	//-------------------------------------------------------------------------
	// Lifecycle methods
	//-------------------------------------------------------------------------	
	/**
	 * Convenience methods for subclasses
	 */
	protected final SessionContext getSessionContext() {
		return sessionContext;
	}
	
	/**
	 * Sets the session context.
	 *
	 * @param ctx SessionContext Context for session
	 */
	public void setSessionContext(SessionContext sessionContext) {
		logger.fine("setSessionContext");
		this.sessionContext = sessionContext;
	}
	
	
	/**
	 * This method is required by the EJB Specification.
	 */
	public void ejbRemove() {
		logger.info("AbstractSessionBean NOP ejbRemove");
	}
	
} 	// class AbstractSessionBean
