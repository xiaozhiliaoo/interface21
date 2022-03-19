
package com.interface21.validation;

public interface Errors {
	 
	// Reject a value in the last update
	void rejectValue(String field, String code, String message);
	
	void rejectValue(String objName, String field, String code, String message);
	
	int getErrorCount();
	
	boolean hasError(String objName, String field);
	
	Object getPropertyValueOrRejectedUpdate(String objName, String field);
	
	boolean hasErrors();
	
	/**
	 * Return FieldError or null
	 */
	FieldError getError(String objName, String field);

}
