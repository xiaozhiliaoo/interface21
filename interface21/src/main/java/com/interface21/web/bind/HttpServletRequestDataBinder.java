package com.interface21.web.bind;

import com.interface21.validation.DataBinder;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;


/**
 * To servlet requests what BeanWrapper is to beans
 * Slightly unusual, as it _is_ an exception
 * Supports property change listeners and vetoable change listeners
 * @author Rod Johnson
 * @version
 */
public class HttpServletRequestDataBinder extends DataBinder  {
	
	public static String ERROR_ATTRIBUTE_NAME = "com.interface21.web.bind.ERRORS";
	
	
	public HttpServletRequestDataBinder(Object target, String name) {
		super(target, name);
	}
	
	//public HttpServletRequestDataBinder(Object target, VetoableChangeListener vtc) {
	//	super(target, vtc);
	//}
	
	
	
	
	public void bind(ServletRequest request, String[] requiredFields) throws ServletException {
		bind(new ServletRequestParameterPropertyValues(request), requiredFields);
	}
	
	/** 
	 * Create a new command object. Invoked on each incoming request, before
	 * either the handleCommand() or handleInvalidRequest() method is invoked (depending on the
	 * result of the binding operation). The incoming request is passed to this method
	 * in case values in the request (such as session data) should cause prepopulation of
	 * the new command object. Otherwise, a new instance of the commandClass object can
	 * simply be instantiated.
	 * @param request incoming request in case we want to look at its state
	 * @return a CommandWrapper object containing the command object and necessary information
	 * about how the framework should process it (such as whether the framework should
	 * attempt to bind request parameters onto it)
	 * @throws ServletException if
	 */
	public void bind(ServletRequest request) throws ServletException {
		bind (new ServletRequestParameterPropertyValues(request));
	}
	
	
	
	/**
	 * Treats errors as fatal
	 * @throws WebRequestBindingException subclass of ServletException on any binding problem
	 */
	public void closeNoCatch() throws WebRequestBindingException {
		if (hasErrors()) {
			throw new WebRequestBindingException("Errors binding onto class " + getTarget(), this);
		}
	}
	
}
