
package com.interface21.jta;

import com.interface21.core.NestedRuntimeException;

public class TransactionInfrastructureException extends NestedRuntimeException {

	/**
	 * Constructor for TransactionInfrastructureException.
	 * @param s
	 */
	public TransactionInfrastructureException(String s) {
		super(s);
	}

	/**
	 * Constructor for TransactionInfrastructureException.
	 * @param s
	 * @param ex
	 */
	public TransactionInfrastructureException(String s, Throwable ex) {
		super(s, ex);
	}


}
