/**
 * Feel free to modify this code to meet your needs.
 * These packages are described in "Expert One-on-One
 * J2EE Design and Development" by Rod Johnson (Wrox, 2002).
 * 
 * For commercial support, contact rod.johnson@interface21.com.
 */

package com.interface21.ejb.support;

import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;


/** 
 * Convenient superclass for MDBs.
 * Doesn't require JMS, as EJB 2.1 MDBs are no longer
 * JMS-specific: see AbstractJmsMessageDrivenBean subclass.
 * NB: cannot use final
 * @author Rod Johnson
 * @version $RevisionId: ResultSetHandler.java,v 1.1 2001/09/07 12:48:57 rod Exp $
 */
public abstract class AbstractMessageDrivenBean extends AbstractEnterpriseBean implements MessageDrivenBean {
	
	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
	
	/** SessionContext passed to this object */
	private MessageDrivenContext	messageDrivenContext;
	
	
	//-------------------------------------------------------------------------
	// Lifecycle methods
	//-------------------------------------------------------------------------	
	/**
	 * Convenience methods for subclasses
	 */
	protected final MessageDrivenContext getMessageDrivenContext() {
		return messageDrivenContext;
	}
	
	/**
	 * Sets the MessageDriven context.
	 * @param messageDrivenContext MessageDrivenContext
	 */
	public void setMessageDrivenContext(MessageDrivenContext messageDrivenContext) {
		logger.fine("setMessageContext");
		this.messageDrivenContext = messageDrivenContext;
	}
	
	/**
	 * Required by the EJB specification but not the MessageDrivenBean interface
	 * Can use BeanFactory here for initialization if required.
	 */
	public abstract void ejbCreate();
	
	
	/**
	 * This method is required by the EJB Specification.
	 */
	public void ejbRemove() {
		logger.info("ejbRemove");
	}
	
} 	// class AbstractMessageDrivenBean
