package com.interface21.jta;

import com.interface21.beans.DynamicProxy;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.*;

/**
 * Dynamic proxy that can provide declarative transaction management(声明式事务管理)
 * These objects must be threadsafe.
 * THREADLOCAL!?
 * Subclasses will associate an object with a transaction.
 * @author Rod Johnson
 */
public abstract class AbstractTransactionProxy extends DynamicProxy {
	

	private static final HashMap TRANSACTION_ISOLATION_MAP = new HashMap();

	static {
		//事务隔离级别
		TRANSACTION_ISOLATION_MAP.put("TRANSACTION_READ_COMMITTED", new Integer(Connection.TRANSACTION_READ_COMMITTED));
		TRANSACTION_ISOLATION_MAP.put("TRANSACTION_READ_UNCOMMITTED", new Integer(Connection.TRANSACTION_READ_UNCOMMITTED));
		TRANSACTION_ISOLATION_MAP.put("TRANSACTION_REPEATABLE_READ", new Integer(Connection.TRANSACTION_REPEATABLE_READ));
		TRANSACTION_ISOLATION_MAP.put("TRANSACTION_SERIALIZABLE", new Integer(Connection.TRANSACTION_SERIALIZABLE));
	}
	
	/**
	 * @return a constant from the Connection class
	 */
	private static Integer isolationLevelForCode(String code) {
		Integer val = (Integer) TRANSACTION_ISOLATION_MAP.get(code.toUpperCase());
		if (val == null)
			throw new TransactionInfrastructureException("Unrecognized transaction isolation code '" + code + "'");
		return val;
	}
	
	
	// OR PUT IN SEPARATE CLASS!?
	private static class TxStatus {
		public final Object txObject;
		private boolean rollbackOnly;
		
		public TxStatus(Object txObject) {
			this.txObject = txObject;
		}
		public void setRollbackOnly() {
			this.rollbackOnly = true;
		}
		public boolean getRollbackOnly() {
			return rollbackOnly;
		}
	}
	
	
	/**
	 * Contains a TxStatus object for each thread associated
	 * with a transaction by this class.
	 */
	private static ThreadLocal txContexts = new ThreadLocal();
	
	/**
	 * Is the current thread associated with a transaction and is
	 * that transaction set for rollback
	 */
	public static boolean isRollbackOnly() {
		TxStatus status = (TxStatus) txContexts.get();
		if (status == null) {
			System.out.println("No transaction to set rollbackonly on");
			return false;
		}
		else {
			return status.getRollbackOnly();
		}
	}
	
	/**
	 * Set the current thread to rollback only
	 */
	public static void setRollbackOnly() {
		TxStatus status = (TxStatus) txContexts.get();
		if (status == null) {
			//System.out.println("No transaction to set rollbackonly on");
			throw new TransactionInfrastructureUsageException("No transaction context in setRollbackOnly");
		}
		else {
			System.out.println("Set rollbackonly on");
			status.setRollbackOnly();
		}
	}
	
	private static void createTransactionContext(Object txObject) {
		TxStatus status = new TxStatus(txObject);
		// IF ALREADY A CONTEXT!?
		
		System.out.println("CREATE CONTEXT ___________________");
		
		txContexts.set(status);
	}
	
	private static void killTransactionContext() {
		TxStatus status = (TxStatus) txContexts.get();
		if (status != null) {
			System.out.println("CLEAR CONTEXT ___________________");
			txContexts.set(null);
		}
	}

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/** Method name to isolation value int from connection */
	public HashMap txMethods = new HashMap();

	private boolean rollbackOnCheckedExceptions = true;


	//---------------------------------------------------------------------
	// Constructor
	//---------------------------------------------------------------------
	/**
	 * Constructor for TransactionProxy.
	 * @param wrappedObject
	 * @param className
	 */
	public AbstractTransactionProxy(Object wrappedObject) {
		super(wrappedObject);
	}

	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	public final void setRollbackOnCheckedExceptions(boolean rollbackOnCheckedExceptions) {
		this.rollbackOnCheckedExceptions = rollbackOnCheckedExceptions;
	}

	/**
	 * Overloading methods may not have different transaction semantics. 
	 * It doesn't make sense to have one transactional
	 * and one nontransactional method with the same name
	 * @param name of transactional method
	 * TODO: take an arg that sets isolation as well:
	 * where would constants be defined?
	 * **************** OR TRANSACTIONAL INTERFACE METHOD
	 * isTransactional(Method[] Object[] args)
	 *  could lock at lock parameter etc.
	 * or txDescriptor with matching name!?
	 * NAME = ISOLATION_LEVEL
	 */
	public final void setTransactionalMethods(Properties props) {
		// Store isolation for each methods
		// methods without an entry are non transactional
		
		Iterator itr = props.keySet().iterator();
		while (itr.hasNext()) {
			String methodName = (String) itr.next();
			String isolationLevelCode = props.getProperty(methodName);
			txMethods.put(methodName, isolationLevelForCode(isolationLevelCode));
			logger.info("Method '" + methodName + "' marked as transactional with code=" + isolationLevelCode);
		}

		checkTransactionalMethodsExist();
	}


	private void checkTransactionalMethodsExist() {
		Method[] methods = getTarget().getClass().getMethods();
		Set allMethods = new HashSet();
		for (int i = 0; i < methods.length; i++) {
			allMethods.add(methods[i].getName());
		}
		Iterator txMethodIterator = txMethods.keySet().iterator();
		while (txMethodIterator.hasNext()) {
			String methodName = (String) txMethodIterator.next();
			if (!allMethods.contains(methodName)) 
				throw new TransactionInfrastructureException("Method '" + methodName + "' not found in target " + getTarget().getClass());
		}
	}


	//---------------------------------------------------------------------
	// Overridden methods from DynamicProxy
	//---------------------------------------------------------------------
	/**
	 * @see UniversalProxy#beforeInvoke(Object, Method, Object[])
	 * 
	 * WHAT IF WE HAVE A TRANSACTION? DO WE NEED TO COPY TO THIS? 
	 */
	protected final boolean beforeInvocation(Object proxy, Method method, Object[] args) {
		if (isTransactionalMethod(method)) {
			if (haveTransaction()) {
				logger.fine("Don't need to create transaction for transactional method '" + method.getName() + "': one already exists");
				// TODO: parallel no commit if we want to allow this at all
			}
			else {
				logger.info("Creating transaction for method '" + method.getName() + "'");
				// There must be an entry
				Integer isolationLevel = (Integer) txMethods.get(method.getName());
				Object txObject = newTransaction(isolationLevel.intValue());
				createTransactionContext(txObject);
			}
		}
		else {
			logger.fine("Don't need to create transaction for method '" + method.getName() + "': this method isn't transactional");
		}
		return true;
	} // beforeInvocation

	/**
	 * @see UniversalProxy#afterInvoke(Object, Method, Object[])
	 */
	protected final void methodInvocationSucceeded(Object proxy, Method method, Object[] args) {
		if (isTransactionalMethod(method)) {
			logger.info("Committing transaction after successful invocation of method '" + method.getName() + "'");
			doCommit();
		}
	}

	/**
	 * We may commit or roll back, depending on our configuration
	 * @see UniversalProxy#applicationException(Object, Method, Object[], Exception)
	 */
	protected final void methodInvocationThrewCheckedException(Object proxy, Method method, Object[] args, Exception ex) {
		//if (isTransactionalMethod(m)) ?
		// DO WE WANT TO ROLLBACK IF SOMEONE ELSE CRREATED THE TX!? PROBABLY NOT
		if (this.rollbackOnCheckedExceptions) {
			logger.severe(
				"Rolling back transaction on method '" + method.getName() + "' on checked (application) exception: " + ex);
			doRollback();
		}
		else {
			logger.info(
				"Method '" + method.getName() + "' threw checked exception {" + ex + "} but the transaction has NOT been rolled back");
			doCommit();
		}
	}

	/**
	 * Always rollback the transaction.
	 * @see UniversalProxy#runtimeException(Object, Method, Object[], Throwable)
	 */
	protected final void methodInvocationThrewRuntimeException(Object proxy, Method method, Object[] args, Throwable t) {
		if (isTransactionalMethod(method)) {
			logger.severe("Rolling back transaction on method '" + method.getName() + "' on runtime exception: " + t);
			doRollback();
		}
	}
	
	
	/**
	 * Overriden method
	 */
	protected void cleanUp(Method method) {
		if (isTransactionalMethod(method)) {
			killTransactionContext();
		}
	}

	//---------------------------------------------------------------------
	// Implementation methods
	//---------------------------------------------------------------------
	private boolean isTransactionalMethod(Method m) {
		return txMethods.containsKey(m.getName());
	}

	private void doCommit() throws TransactionInfrastructureException {
		if (isRollbackOnly()) {
			logger.severe("Rolling back due to invocation of setRollbackOnly");
			doRollback();
		}
		else {
			Object txObject = getTransactionObject();
			if (txObject == null)
				throw new TransactionInfrastructureException("Cannot commit transaction: don't have a transaction");
			commit(txObject);
		}
	}

	/**
	 * Do nothing if no transaction
	 */
	private void doRollback() throws TransactionInfrastructureException {
		// DO WE WANT TO ROLL
		//		if (isTransactionalMethod(m))
		Object txObject = getTransactionObject();
		if (txObject == null)
			throw new TransactionInfrastructureException("Cannot rollback transaction: don't have a transaction");
		rollback(txObject);
	}


	/**
	 * USE CONTEXT!?
	 */
	public final boolean haveTransaction() throws TransactionInfrastructureException {
		//return getTransactionObject() != null;
		return txContexts.get() != null;
	}

	//---------------------------------------------------------------------
	// Protected abstract methods
	//---------------------------------------------------------------------
	
	// CHANGE PARAMETERS!?

	/**
	 * Create a new transaction
	 * IF ALREADY EXISTS!?
	 * @param isolationLevel isolation level from java.sql.Connection
	 */
	protected abstract Object newTransaction(int isolationLevel) throws TransactionInfrastructureException;

	/**
	 * If we're not in a transaction return null. Else return a subclass-specific
	 * representation of the transaction. Saves checking twice in this class's post-invocation methods.
	 */
	protected abstract Object getTransactionObject() throws TransactionInfrastructureException;

	/**
	 * @param txObject object returned by the transactionObject() method.
	 */
	protected abstract void commit(Object txObject) throws TransactionInfrastructureException;

	protected abstract void rollback(Object txObject) throws TransactionInfrastructureException;

} 	// AbstractTransactionProxy