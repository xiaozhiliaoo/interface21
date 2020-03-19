package com.interface21.jta;

import javax.ejb.EJBException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;


/**
 * @version 	1.0
 * @author Rod Johnson
 */
public class JndiTransactionFinder implements TransactionFinder {


	private String location;
	
	public JndiTransactionFinder() {
	}
	
	public JndiTransactionFinder(String location) {
		this.location = location;
	}
	
	/** Set the JNDI location where we look for the current transaction
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/*
	 * Find the current transaction from JNDI.
	 * Only works on servers that expose the current javax.jdo.Transaction
	 * as a JNDI attribute, like Orion and JBoss.
	 * @see TransactionFinder#currentTransaction()
	 */
	public Transaction currentTransaction() {
		if (location == null)
			throw new EJBException("Cannot lookup transaction: no JNDI location specified");
			
		try {
			return getTransactionManager().getTransaction();
		}
		catch (SystemException ex) {
			// **** TODO
			throw new EJBException("javax.transaction internal error: " + ex);
		}
	}

	/**
	 * @see TransactionFinder#getTransactionManager()
	 */
	public TransactionManager getTransactionManager() {
		if (location == null)
			// CHANGE TO NESTED RTE
			throw new EJBException("Cannot lookup transaction: no JNDI location specified");
			
		Context context = null;
		try {
			context = new InitialContext();
			TransactionManager transactionmanager = (TransactionManager) context.lookup(this.location);
			return transactionmanager;
		}
		catch (NamingException ex) {
			// **** TODO
			throw new EJBException("Can't find transaction at location '" + this.location + "'" + ex);
		}
		finally {
			try {
				if (context != null)
					context.close();
			}
			catch (NamingException ex) {
				// Do nothing
			}
		}
	}

}