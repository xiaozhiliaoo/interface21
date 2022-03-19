package com.interface21.jta;

import javax.transaction.TransactionManager;



/**
 * Interface to be implemented by objects that know how to
 * find the current transaction inside an application server.
 * Analogous to Kodo's com.solarmetric.kodo.ee.ManagedRuntime
 * interface
 */
public interface TransactionFinder {
	
	/**
	 * Obtain the current transaction. If there is no
	 * transaction, return null.
	 * @return the current transaction, or null (without error)
	 * if there is no active transaction.
	 */
	//Transaction currentTransaction();
	
	TransactionManager getTransactionManager();

}

