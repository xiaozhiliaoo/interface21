/*
 * ContextCallback.java
 *
 * Created on 15 September 2001, 10:26
 */

package com.interface21.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 *
 * @author  rod
 * @version 
 */
public interface ContextCallback {
    
    void doInContext(Context ctx) throws NamingException;

}

