/*
 * ContextFactory.java
 *
 * Created on 15 September 2001, 10:29
 */

package com.interface21.jndi;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 *
 * @author  rod
 * @version 
 */
public interface ContextFactory {
    
    Context getContext() throws NamingException;

}

