
package com.interface21.beans;

import java.beans.PropertyChangeEvent;

/**
 * Thrown when a method getter or setter throws an exception
 * (analogous to an InvocationTargetException)
 * @author  Rod Johnson
 * @version $Revision: 1.1.1.1 $
 */
public class MethodInvocationException extends PropertyAccessException {

	/**
	 * @param t throwable raised by invoked method
	 * @param propertyChangeEvent PropertyChangeEvent that resulted in an exception
	 */
    public MethodInvocationException(Throwable t, PropertyChangeEvent propertyChangeEvent) {
        super("Property " + propertyChangeEvent.getPropertyName() + " threw exception (" + t + ")", t, propertyChangeEvent);
    }
	
}


