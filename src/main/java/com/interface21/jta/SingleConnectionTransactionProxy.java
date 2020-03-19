
package com.interface21.jta;

import java.sql.Connection;
import java.sql.SQLException;

import com.interface21.core.Assertions;

/**
 * Transaction proxy that performs transaction management on a single
 * JDBC connection
 */
public class SingleConnectionTransactionProxy extends AbstractTransactionProxy {
	
	private Connection connection;

	/**
	 * Constructor for SingleConnectionTransactionProxy.
	 * @param wrappedObject
	 * @param className
	 */
	public SingleConnectionTransactionProxy(Object wrappedObject, Connection connection) throws SQLException {
		super(wrappedObject);
		this.connection = connection;
		connection.setAutoCommit(false);
	}

	/**
	 * @see TransactionProxy#newTransaction()
	 */
	protected Object newTransaction(int isolationLevel) throws TransactionInfrastructureException {
		// Don't need to do anything
		// JDBC starts transactions implicitly 
		// when autoCommit is off
		try {
			connection.setTransactionIsolation(isolationLevel);
			return Boolean.TRUE;
		}
		catch (SQLException ex) {
			throw new TransactionInfrastructureException("Couldn't set isolation level on connection", ex);
		}
	}

	/**
	 * @see TransactionProxy#haveTransaction()
	 */
	protected Object getTransactionObject() throws TransactionInfrastructureException {
		return Boolean.TRUE;
	}

	/**
	 * @see TransactionProxy#commit()
	 */
	protected void commit(Object txObject) throws TransactionInfrastructureException {
		Assertions.assertTrue("txObject is what we provided", txObject.equals(Boolean.TRUE));
		try {
			connection.commit();
		} 
		catch(SQLException ex) {
			throw new TransactionInfrastructureException("Cannot commit using JDBC", ex);
		}
	}

	/**
	 * @see TransactionProxy#rollback()
	 */
	protected void rollback(Object txObject) throws TransactionInfrastructureException {
		Assertions.assertTrue("txObject is what we provided", txObject.equals(Boolean.TRUE));
		try {
			connection.rollback();
		} 
		catch(SQLException ex) {
			throw new TransactionInfrastructureException("Cannot rollback using JDBC", ex);
		}
	}

}
