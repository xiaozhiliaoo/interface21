/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.validation;

import org.springframework.beans.PropertyAccessException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * Default <code>BindingErrorProcessor</code> implementation.
 *
 * <p>Uses the "required" error code and the field name to resolve message codes
 * for a missing field error.
 *
 * <p>Creates a <code>FieldError</code> for each <code>PropertyAccessException</code>
 * given, using the <code>PropertyAccessException</code>'s error code ("typeMismatch",
 * "methodInvocation") for resolving message codes.
 *
 * @author Alef Arendsen
 * @author Juergen Hoeller
 * @since 1.2
 * @see #MISSING_FIELD_ERROR_CODE
 * @see DataBinder#setBindingErrorProcessor
 * @see BindException#addError
 * @see BindException#resolveMessageCodes
 * @see PropertyAccessException#getErrorCode
 * @see org.springframework.beans.TypeMismatchException#ERROR_CODE
 * @see org.springframework.beans.MethodInvocationException#ERROR_CODE
 */
public class DefaultBindingErrorProcessor implements BindingErrorProcessor {

	/**
	 * Error code that a missing field error (i.e. a required field not
	 * found in the list of property values) will be registered with:
	 * "required".
	 */
	public static final String MISSING_FIELD_ERROR_CODE = "required";

	public void processMissingFieldError(String missingField, BindException errors) {
		// Create field error with code "required".
		String[] codes = errors.resolveMessageCodes(MISSING_FIELD_ERROR_CODE, missingField);
		Object[] arguments = getArgumentsForBindError(errors.getObjectName(), missingField);
		errors.addError(new FieldError(
				errors.getObjectName(), missingField, "", true,
				codes, arguments, "Field '" + missingField + "' is required"));
	}

	public void processPropertyAccessException(PropertyAccessException ex, BindException errors) {
		// Create field error with the exceptions's code, e.g. "typeMismatch".
		String field = ex.getPropertyChangeEvent().getPropertyName();
		Object value = ex.getPropertyChangeEvent().getNewValue();
		String[] codes = errors.resolveMessageCodes(ex.getErrorCode(), field);
		Object[] arguments = getArgumentsForBindError(errors.getObjectName(), field);
		errors.addError(new FieldError(
				errors.getObjectName(), field, value, true,
				codes, arguments, ex.getLocalizedMessage()));
	}

	/**
	 * Return FieldError arguments for a binding error on the given field.
	 * Invoked for each missing required fields and each type mismatch.
	 * <p>Default implementation returns a DefaultMessageSourceResolvable
	 * with "objectName.field" and "field" as codes.
	 * @param field the field that caused the binding error
	 * @return the Object array that represents the FieldError arguments
	 * @see FieldError#getArguments
	 * @see DefaultMessageSourceResolvable
	 */
	protected Object[] getArgumentsForBindError(String objectName, String field) {
		String[] codes = new String[] {objectName + Errors.NESTED_PATH_SEPARATOR + field, field};
		String defaultMessage = field;
		return new Object[] {new DefaultMessageSourceResolvable(codes, defaultMessage)};
	}

}
