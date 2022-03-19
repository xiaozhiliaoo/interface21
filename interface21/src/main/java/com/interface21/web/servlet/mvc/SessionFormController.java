
package com.interface21.web.servlet.mvc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public abstract class SessionFormController extends FormController {
	
	private final static String SESSION_ATTRIBUTE = "com.interface21.web.servlet.mvc.SessionFormController.FORM_OBJECT";
	
	public SessionFormController() {
	}
	
	public SessionFormController(Class commandClass) {
		super(commandClass);
	}

	/**
	 * Subclasses must implement this
	 * @see FormController#formBackingObject(HttpServletRequest)
	 */
	protected final Object formBackingObject(HttpServletRequest request) throws ServletException {
		Object formObject = formBackingObjectInternal(request);
		if (formObject == null)
			throw new ServletException("Form object returned by formBackingObjectInternal() may not be null");
		HttpSession session = request.getSession(true);
		session.setAttribute(SESSION_ATTRIBUTE, formObject);
		return formObject;
	}
	
	protected abstract Object formBackingObjectInternal(HttpServletRequest request) throws ServletException;

	/**
	 * @see BaseCommandController#bindObject()
	 */
	protected final Object userObject(HttpServletRequest request) throws ServletException {
		HttpSession session = request.getSession(false);
		if (session == null)
			throw new ServletException("Must have session when trying to bind");
		Object formObject = session.getAttribute(SESSION_ATTRIBUTE);
		if (formObject == null)
			throw new ServletException("Form object not found in session");
		//session.removeAttribute(SESSION_ATTRIBUTE);
		return formObject;
	}
	
	// REMOVE SESSION ATTRIBUTE SOMEWHERE!?

}
