
package com.interface21.validation;

public interface Validator {
	
	boolean supports(Class clazz);
	
	void validate(Object obj, Errors errors);

}
