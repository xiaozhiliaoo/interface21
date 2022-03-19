
package com.interface21.jta;

import javax.ejb.EJBException;
import javax.ejb.EJBException;

import com.interface21.ejb.support.*;

/**
 * @version 	1.0
 * @author
 */
public class TransactionFinderFactory {
	
	/** JNDI name for Transaction in Orion */
	public static final String ORION_LOCATION = "java:comp/UserTransaction";

	/** JNDI name for Transaction in JBoss */
	public static final String JBOSS_LOCATION = "java:/TransactionManager";
	
	/** JNDI name for Transaction in WebLogic */
	public static final String WEBLOGIC_LOCATION = "weblogic.transaction.TransactionManager";


	public TransactionFinder getTransactionFinder() {
		int server = EJBContainerDetective.getInstance().getServer();
		switch (server) {
			case EJBContainerDetective.JBOSS : return new JndiTransactionFinder(JBOSS_LOCATION);
			case EJBContainerDetective.ORION : return new JndiTransactionFinder(ORION_LOCATION);
			case EJBContainerDetective.WEBLOGIC : return new JndiTransactionFinder(WEBLOGIC_LOCATION);
			default : throw new EJBException("TransactionFinderFactory: only JBoss, Orion and WebLogic are supported");
		}
	}

}
