package com.interface21.jta;

import javax.naming.NamingException;
import javax.transaction.*;

import com.interface21.jndi.JndiServices;

/**
 * Dynamic proxy to create transactions
 * @author Rod Johnson
 */
public class JtaTransactionProxy extends AbstractTransactionProxy {

	public static final String UTX_JNDI_NAME = "java:comp/UserTransaction";

	/**
	 * TransactionFinder for the current server
	 */
	//private TransactionFinder transactionFinder;
	
	private JndiServices jndiHelper = new JndiServices();

	/**
	 * Constructor for J2eeTransactionProxy.
	 * @param wrappedObject
	 */
	public JtaTransactionProxy(Object wrappedObject) {
		super(wrappedObject);
	//	this.transactionFinder = new TransactionFinderFactory().getTransactionFinder();
	}

	/**
	 * Won't be invoked if there is a transaction
	 * @see TransactionProxy#newTransaction()
	 */
	protected Object newTransaction(int isolationLevel) throws TransactionInfrastructureException {

		try {
			UserTransaction utx = getUserTransaction();
			utx.begin();
		}
		catch (NotSupportedException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (SystemException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		
		if (1==1) throw new UnsupportedOperationException("What do we return????");
		return null;
	}

	private UserTransaction getUserTransaction() throws TransactionInfrastructureException {
		try {
			UserTransaction utx = (UserTransaction) jndiHelper.lookup(UTX_JNDI_NAME);
			// Can it ever be null? Assertion?
			return utx;
		}
		catch (NamingException ex) {
			// Couldn't look up user transaction
			throw new TransactionInfrastructureException("Couldn't find user transaction in JNDI", ex);
		}
	}

	/**
	 * Our TransactionObject is a UserTransaction object
	 * @see TransactionProxy#transactionObject()
	 */
	protected Object getTransactionObject() throws TransactionInfrastructureException {

		// OR USE USER TRANSACTION!? but how not to create one!?
		//return transactionFinder.currentTransaction();
		return getUserTransaction();
	}

	/**
	 * @see TransactionProxy#commit()
	 */
	protected void commit(Object txObject) throws TransactionInfrastructureException {

		// OR USE UTX!? what if not in a transaction?
		//Transaction transaction = (Transaction) txObject;
		UserTransaction utx = (UserTransaction) txObject;
		try {
			utx.commit();
		}
		catch (SecurityException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (IllegalStateException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (RollbackException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (HeuristicMixedException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (HeuristicRollbackException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (SystemException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
	}

	/**
	 * @see TransactionProxy#rollback()
	 */
	protected void rollback(Object txObject) throws TransactionInfrastructureException {
		//Transaction transaction = (Transaction) txObject;
		UserTransaction utx = (UserTransaction) txObject;
		try {
			utx.rollback();
		}
		catch (SecurityException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (IllegalStateException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
		catch (SystemException ex) {
			throw new TransactionInfrastructureException("GET CORRECT MESSAGE FROM DOCS", ex);
		}
	}

}	// JtaTransactionProxy