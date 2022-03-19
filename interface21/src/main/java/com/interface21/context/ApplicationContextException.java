

package com.interface21.context;

/**
 *
 * @author  rod
 * @version 
 */
public class ApplicationContextException extends com.interface21.core.NestedRuntimeException {

    /**
	 * Constructs an <code>ApplicationContextException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ApplicationContextException(String msg) {
        super(msg);
    }
	
	public ApplicationContextException(String msg, Throwable t) {
        super(msg, t);
    }
}


