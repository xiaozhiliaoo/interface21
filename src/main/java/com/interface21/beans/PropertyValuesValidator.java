

package com.interface21.beans;

/**
 *
 * @author  Rod Johnson
 * @version $Revision: 1.1.1.1 $ 
 */
public interface PropertyValuesValidator {
	
	/** 
	 * Are these PropertyValues invalid? For example,
	 * are required properties missing? Does the presence of a particular
	 * property require others to be present? Implementations can
	 * rely on the contains() method of the PropertyValues interface, but cannot
	 * assume anything about the type of properties: type conversion is done 
	 * when PropertyValues are applied to a bean using a BeanWrapper object.
	 * @param pvs PropertyValues to validate
	 * @throws InvalidPropertyValuesException if the PropertyValues object is invalid
	 */
	void validatePropertyValues(PropertyValues pvs) throws InvalidPropertyValuesException;

}

