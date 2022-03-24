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

import java.beans.PropertyEditor;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Default implementation of the Errors interface, supporting
 * registration and evaluation of binding errors.
 * Slightly unusual, as it <i>is</i> an exception.
 *
 * <p>This is mainly a framework-internal class. Normally, application
 * code will work with the Errors interface, or a DataBinder that in
 * turn exposes a BindException via <code>getErrors()</code>.
 *
 * <p>Supports exporting a model, suitable for example for web MVC.
 * Thus, it is sometimes used as parameter type instead of the Errors interface
 * itself - if extracting the model makes sense in the particular context.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #getModel
 * @see DataBinder#getErrors
 */
public class BindException extends Exception implements Errors {

	/**
	 * Prefix for the name of the Errors instance in a model,
	 * followed by the object name.
	 */
	public static final String ERROR_KEY_PREFIX = BindException.class.getName() + ".";


	private final List errors = new LinkedList();

	private final Object target;

	private final String objectName;

	private transient BeanWrapper beanWrapper;

	private MessageCodesResolver messageCodesResolver = new DefaultMessageCodesResolver();

	private String nestedPath = "";

	private final Stack nestedPathStack = new Stack();


	/**
	 * Create a new BindException instance.
	 * @param target target object to bind onto
	 * @param name name of the target object
	 * @see DefaultMessageCodesResolver
	 */
	public BindException(Object target, String name) {
		this.target = target;
		this.objectName = name;
	}

	/**
	 * Return the wrapped target object.
	 */
	public Object getTarget() {
		return target;
	}

	public String getObjectName() {
		return objectName;
	}

	/**
	 * Return the BeanWrapper that this instance uses.
	 * Creates a new one if none existed before.
	 */
	protected BeanWrapper getBeanWrapper() {
		if (this.beanWrapper == null) {
			this.beanWrapper = new BeanWrapperImpl(this.target);
		}
		return this.beanWrapper;
	}

	/**
	 * Set the strategy to use for resolving errors into message codes.
	 * Default is DefaultMessageCodesResolver.
	 * @see DefaultMessageCodesResolver
	 */
	public void setMessageCodesResolver(MessageCodesResolver messageCodesResolver) {
		this.messageCodesResolver = messageCodesResolver;
	}

	/**
	 * Return the strategy to use for resolving errors into message codes.
	 */
	public MessageCodesResolver getMessageCodesResolver() {
		return messageCodesResolver;
	}


	public void setNestedPath(String nestedPath) {
		doSetNestedPath(nestedPath);
		this.nestedPathStack.clear();
	}

	public String getNestedPath() {
		return nestedPath;
	}

	public void pushNestedPath(String subPath) {
		this.nestedPathStack.push(getNestedPath());
		doSetNestedPath(getNestedPath() + subPath);
	}

	public void popNestedPath() throws IllegalArgumentException {
		try {
			String formerNestedPath = (String) this.nestedPathStack.pop();
			doSetNestedPath(formerNestedPath);
		}
		catch (EmptyStackException ex) {
			throw new IllegalStateException("Cannot pop nested path: no nested path on stack");
		}
	}

	/**
	 * Actually set the nested path.
	 * Delegated to by setNestedPath and pushNestedPath.
	 */
	protected void doSetNestedPath(String nestedPath) {
		if (nestedPath == null) {
			nestedPath = "";
		}
		if (nestedPath.length() > 0 && !nestedPath.endsWith(NESTED_PATH_SEPARATOR)) {
			nestedPath += NESTED_PATH_SEPARATOR;
		}
		this.nestedPath = nestedPath;
	}

	/**
	 * Transform the given field into its full path,
	 * regarding the nested path of this instance.
	 */
	protected String fixedField(String field) {
		return getNestedPath() + field;
	}


	public void reject(String errorCode) {
		reject(errorCode, null, null);
	}

	public void reject(String errorCode, String defaultMessage) {
		reject(errorCode, null, defaultMessage);
	}

	public void reject(String errorCode, Object[] errorArgs, String defaultMessage) {
		addError(new ObjectError(getObjectName(), resolveMessageCodes(errorCode), errorArgs, defaultMessage));
	}

	public void rejectValue(String field, String errorCode) {
		rejectValue(field, errorCode, null, null);
	}

	public void rejectValue(String field, String errorCode, String defaultMessage) {
		rejectValue(field, errorCode, null, defaultMessage);
	}

	public void rejectValue(String field, String errorCode, Object[] errorArgs, String defaultMessage) {
		String fixedField = fixedField(field);
		Object newVal = getBeanWrapper().getPropertyValue(fixedField);
		FieldError fe = new FieldError(
				getObjectName(), fixedField, newVal, false,
				resolveMessageCodes(errorCode, field), errorArgs, defaultMessage);
		addError(fe);
	}

	/**
	 * Resolve the given error code into message codes.
	 * Calls the MessageCodesResolver with appropriate parameters.
	 * @param errorCode the error code to resolve into message codes
	 * @return the resolved message codes
	 * @see #setMessageCodesResolver
	 */
	public String[] resolveMessageCodes(String errorCode) {
		return getMessageCodesResolver().resolveMessageCodes(errorCode, getObjectName());
	}

	/**
	 * Resolve the given error code into message codes for the given field.
	 * Calls the MessageCodesResolver with appropriate parameters.
	 * @param errorCode the error code to resolve into message codes
	 * @param field the field to resolve message codes for
	 * @return the resolved message codes
	 * @see #setMessageCodesResolver
	 */
	public String[] resolveMessageCodes(String errorCode, String field) {
		String fixedField = fixedField(field);
		Class fieldType = getBeanWrapper().getPropertyType(fixedField);
		return getMessageCodesResolver().resolveMessageCodes(errorCode, getObjectName(), fixedField, fieldType);
	}

	/**
	 * Add an ObjectError or FieldError to the errors list.
	 * <p>Intended to be used by subclasses like DataBinder,
	 * or by cooperating strategies like a BindingErrorProcessor.
	 * @see ObjectError
	 * @see FieldError
	 * @see DataBinder
	 * @see BindingErrorProcessor
	 */
	public void addError(ObjectError error) {
		this.errors.add(error);
	}

	public void addAllErrors(Errors errors) {
		this.errors.addAll(errors.getAllErrors());
	}


	public boolean hasErrors() {
		return !this.errors.isEmpty();
	}

	public int getErrorCount() {
		return this.errors.size();
	}

	public List getAllErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	public boolean hasGlobalErrors() {
		return (getGlobalErrorCount() > 0);
	}

	public int getGlobalErrorCount() {
		return getGlobalErrors().size();
	}

	public List getGlobalErrors() {
		List result = new LinkedList();
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			Object error = it.next();
			if (!(error instanceof FieldError)) {
				result.add(error);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public ObjectError getGlobalError() {
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			ObjectError objectError = (ObjectError) it.next();
			if (!(objectError instanceof FieldError)) {
				return objectError;
			}
		}
		return null;
	}

	public boolean hasFieldErrors(String field) {
		return (getFieldErrorCount(field) > 0);
	}

	public int getFieldErrorCount(String field) {
		return getFieldErrors(field).size();
	}

	public List getFieldErrors(String field) {
		List result = new LinkedList();
		String fixedField = fixedField(field);
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			Object error = it.next();
			if (error instanceof FieldError && isMatchingFieldError(fixedField, ((FieldError) error))) {
				result.add(error);
			}
		}
		return Collections.unmodifiableList(result);
	}

	public FieldError getFieldError(String field) {
		String fixedField = fixedField(field);
		for (Iterator it = this.errors.iterator(); it.hasNext();) {
			Object error = it.next();
			if (error instanceof FieldError) {
				FieldError fe = (FieldError) error;
				if (isMatchingFieldError(fixedField, fe)) {
					return fe;
				}
			}
		}
		return null;
	}

	/**
	 * Check whether the given FieldError matches the given field.
	 * @param field the field that we are looking up FieldErrors for
	 * @param fieldError the candidate FieldError
	 * @return whether the FieldError matches the given field
	 */
	protected boolean isMatchingFieldError(String field, FieldError fieldError) {
		return (field.equals(fieldError.getField()) ||
				(field.endsWith("*") && fieldError.getField().startsWith(field.substring(0, field.length() - 1))));
	}

	public Object getFieldValue(String field) {
		FieldError fe = getFieldError(field);
		String fixedField = fixedField(field);
		// Use rejected value in case of error, current bean property value else.
		Object value = (fe != null) ? fe.getRejectedValue() : getBeanWrapper().getPropertyValue(fixedField);
		// Apply custom editor, but not on binding failures like type mismatches.
		if (fe == null || !fe.isBindingFailure()) {
			PropertyEditor customEditor = getCustomEditor(fixedField);
			if (customEditor != null) {
				customEditor.setValue(value);
				String textValue = customEditor.getAsText();
				// If the PropertyEditor returned null, there is no appropriate
				// text representation for this value: only use it if non-null.
				if (textValue != null) {
					value = textValue;
				}
			}
		}
		return value;
	}

	/**
	 * Retrieve the custom PropertyEditor for the given field, if any.
	 * @param field the field name
	 * @return the custom PropertyEditor, or null
	 */
	public PropertyEditor getCustomEditor(String field) {
		String fixedField = fixedField(field);
		Class type = getBeanWrapper().getPropertyType(fixedField);
		return getBeanWrapper().findCustomEditor(type, fixedField);
	}


	/**
	 * Return a model Map for the obtained state, exposing an Errors
	 * instance as '{@link #ERROR_KEY_PREFIX ERROR_KEY_PREFIX} + objectName'
	 * and the object itself.
	 * <p>Note that the Map is constructed every time you're calling this method.
	 * Adding things to the map and then re-calling this method will not work.
	 * <p>The attributes in the model Map returned by this method are usually
	 * included in the ModelAndView for a form view that uses Spring's bind tag,
	 * which needs access to the Errors instance. Spring's SimpleFormController
	 * will do this for you when rendering its form or success view. When
	 * building the ModelAndView yourself, you need to include the attributes
	 * from the model Map returned by this method yourself.
	 * @see #getObjectName
	 * @see #ERROR_KEY_PREFIX
	 * @see org.springframework.web.servlet.ModelAndView
	 * @see org.springframework.web.servlet.tags.BindTag
	 * @see org.springframework.web.servlet.mvc.SimpleFormController
	 */
	public Map getModel() {
		Map model = new HashMap();
		// Errors instance, even if no errors.
		model.put(ERROR_KEY_PREFIX + getObjectName(), this);
		// Mapping from name to target object.
		model.put(getObjectName(), getTarget());
		return model;
	}

	/**
	 * Returns diagnostic information about the errors held in this object.
	 */
	public String getMessage() {
		StringBuffer sb = new StringBuffer("BindException: ");
		sb.append(getErrorCount()).append(" errors");
		Iterator it = this.errors.iterator();
		while (it.hasNext()) {
			sb.append("; ").append(it.next());
		}
		return sb.toString();
	}

}
