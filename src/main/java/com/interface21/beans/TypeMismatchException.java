/*
 * ReflectionException.java
 *
 * Created on 13 April 2001, 15:37
 */

package com.interface21.beans;

import java.beans.PropertyChangeEvent;

/**
 * Subclass of ReflectionException thrown on a type mismatch
 * @author  Rod Johnson
 * @version $Revision: 1.1.1.1 $
 */
public class TypeMismatchException extends PropertyAccessException {
	
	private PropertyChangeEvent propertyChangeEvent;
	
	/**
	 * Constructs an <code>ReflectionException</code> with the specified detail message.
	 * @param msg the detail message.
	 */
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType) {
		super("Failed to convert property value of type [" + propertyChangeEvent.getNewValue().getClass().getName() + "] to required type [" + requiredType.getName() + "]", propertyChangeEvent);
	}
	
	public TypeMismatchException(PropertyChangeEvent propertyChangeEvent, Class requiredType, Throwable t) {
		super("Failed to convert property value of type [" + propertyChangeEvent.getNewValue().getClass().getName() + "] to required type [" + requiredType.getName() + "]", t, propertyChangeEvent);
	}
	
}


