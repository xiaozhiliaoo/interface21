/*
 * PrototypeStoreException.java
 *
 * Created on 16 April 2001, 09:53
 */

package com.interface21.beans.factory;

import com.interface21.beans.*;
/**
 *
 * @author  johnsonr
 * @version 
 */
public class BeanDefinitionStoreException extends BeansException {


    /**
    * Constructs an <code>PrototypeStoreException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BeanDefinitionStoreException(String msg,Throwable t) {
        super(msg, t);
    }
}


