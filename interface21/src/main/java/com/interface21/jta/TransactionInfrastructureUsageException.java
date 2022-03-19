
package com.interface21.jta;

public class TransactionInfrastructureUsageException extends TransactionInfrastructureException {

	/**
	 * Constructor for TransactionInfrastructureException.
	 * @param s
	 */
	public TransactionInfrastructureUsageException(String s) {
		super(s);
	}

	/**
	 * Constructor for TransactionInfrastructureException.
	 * @param s
	 * @param ex
	 */
	public TransactionInfrastructureUsageException(String s, Throwable ex) {
		super(s, ex);
	}



}
