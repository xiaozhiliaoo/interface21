/*
 * NotWritablePropertyException.java
 *
 * Created on 29 September 2001, 12:54
 */

package com.interface21.beans;

/**
 *
 * @author  rod
 * @version 
 */
public class NotWritablePropertyException extends BeansException {

    /**
	 * Creates new <code>NotWritablePropertyException</code> without detail message.
     */
    public NotWritablePropertyException(String propertyName, Class beanClass) {
		super("Property [" + propertyName + "] is not writable in bean class [" + beanClass.getName() + "]");
    }
}


