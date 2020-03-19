package com.interface21.context;

/** Exception thrown when an exception code can't be resolved
 * @author Rod Johnson
 * @revision $RevisionId$
 */
public class NoSuchMessageException extends Exception {
	
	public NoSuchMessageException(String code) {
		super("No message found under code '" + code + "'");
	}

}

