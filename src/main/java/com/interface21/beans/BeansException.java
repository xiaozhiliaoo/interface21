

package com.interface21.beans;

import com.interface21.core.NestedRuntimeException;


/**
 * Abstract superclass for all exceptions thrown in the beans package
 * and subpackages.
 * @author  Rod Johnson
 */
public abstract class BeansException extends NestedRuntimeException {

    /**
     * Constructs an <code>ReflectionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BeansException(String msg, Throwable t) {
        super(msg, t);
    }
    
    /**
     * Constructs an <code>ReflectionException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BeansException(String msg) {
        super(msg);
    }
    
}


