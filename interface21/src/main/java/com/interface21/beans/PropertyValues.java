
package com.interface21.beans;

/**
 * Object containing a number of PropertyValues comprising
 * one update.
 * @author  Rod Johnson
 * @since 13 May 2001
 * @version $Id: PropertyValues.java,v 1.1.1.1 2002/08/01 12:52:02 Rod Johnson Exp $
 */
public interface PropertyValues {
   
	/** Return an array of the PropertyValue objects
	 * held in this object.
	 * @return an array of the PropertyValue objects
	 * held in this object.
	 */
	PropertyValue[] getPropertyValues();
	
	/** Is there a propertyValue object for this property?
	 * @param propertyName name of the property we're interested in
	 *@return whether there is a propertyValue object for this property?
	 */
	boolean contains(String propertyName);
	
	/** 
	 * @return pv or null
	 */
	PropertyValue getPropertyValue(String propertyName);

}
