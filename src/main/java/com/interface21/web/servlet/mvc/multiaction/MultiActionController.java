package com.interface21.web.servlet.mvc.multiaction;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java14.java.util.logging.Level;

import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.interface21.context.ApplicationContextException;
import com.interface21.util.StringUtils;
import com.interface21.web.bind.HttpServletRequestDataBinder;
import com.interface21.web.servlet.LastModified;
import com.interface21.web.servlet.ModelAndView;
import com.interface21.web.servlet.mvc.Controller;
import com.interface21.web.servlet.mvc.WebContentGenerator;


/**
 * ANOTHER OPTION IS TO USE ANONYMOUS INNER CONTROLLER CLASSES
 * A HANDLER MAP COULD DO THIS: INNER CLASSES CACHED ON STARTUP
 *
 * map.put("/test.html", new Controller() {
 * 	handleRequest(r, r) {
 * 	do x;
 * }
 * ");
 */


/**
 * Subclasses of this class can handle several different types of request
 * with methods of the form
 * ModelAndView actionName(HttpServletRequest request, HttpServletResponse response);
 * May take a third parameter (HttpSession) in which an existing session will be required.
 * <br/>These methods can throw any kind of exception, but should only let propagate
 * those that they consider fatal, or which their class or superclass is prepared to
 * catch by implementing an exception handler.
 * <br/>This model allows for rapid coding, but loses the advantage of compile-time
 * checking.
 * <p>
 * Inherits superclass bean proprties. Adds methodNameResolver bean property.
 * An implementation of the MethodNameResolver interface defined in this package
 * should return a method name for a given request, based on any aspect of the request,
 * such as its URL or an "action" or like attribute. The default behavior is URL based.
 * <br/>
 * Also supports delegation to another object.
 * <br/>
 * <p>Subclasses can implement custom exception handler methods with names such as
 * ModelAndView anyMeaningfulName(HttpServletRequest request, HttpServletResponse response, ExceptionClass exception);
 * The third parameter can be any subclass or Exception or RuntimeException.
 * </br>
 * There can also be an optional lastModified method for handlers, of signature
 * long anyMeaningfulNameLastModified(HttpServletRequest request)
 * If such a method is present, it will be invoked. Default return from getLastModified()
 * is -1, meaning that content must always be regenerated.
 * Like Struts 1.1 DispatchAction, but more sophisticated.
 * @author Rod Johnson
 */
public class MultiActionController 
	extends WebContentGenerator 
	implements Controller, LastModified  {
		
	public static final String LAST_MODIFIED_METHOD_SUFFIX = "LastModified";
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Methods, keyed by name */
	private HashMap methodHash;
	
	/** LastModified methods, keyed by handler method name (without LAST_MODIFIED_SUFFIX) */
	private HashMap lastModifiedMethodHash;
	
	/** Methods, keyed by exception class */
	private HashMap exceptionHandlerHash;

	/** 
	 * Helper object that knows how to return method names from incoming requests.
	 * Can be overridden via the methodNameResolver bean property
	 */
	private MethodNameResolver methodNameResolver = new InternalPathMethodNameResolver();
	
	/** Object we'll invoke methods on. Defaults to this. */
	private Object delegate;
	
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/**
	 * Constructor for MultiActionController.
	 * Cache methods for quick invocation later. This class's
	 * use of reflection will impose little overhead at runtime
	 * @throws ApplicationContextException if the class doesn't contain any
	 * action handler methods (and so could never handle any requests).
	 */
	public MultiActionController() throws ApplicationContextException {
		setDelegate(this);
	}
	
	public MultiActionController(Object delegate) throws ApplicationContextException {
		setDelegate(delegate);
	}
	
	
	//---------------------------------------------------------------------
	// Bean properties
	//---------------------------------------------------------------------
	public final void setMethodNameResolver(MethodNameResolver methodNameResolver) {
		this.methodNameResolver = methodNameResolver;
	}
	
	public final MethodNameResolver getMethodNameResolver() {
		return this.methodNameResolver;
	}
	
	public final void setDelegate(Object delegate) throws ApplicationContextException {
		this.delegate = delegate;
		if (delegate == null)
			throw new ApplicationContextException("Delegate cannot be null in MultiActionController");

		this.methodHash = new HashMap();
		this.lastModifiedMethodHash = new HashMap();
		
		// Look at all methods in the subclass, trying to find
		// methods that are validators according to our criteria
		Method[] methods = delegate.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			// We're looking for methods with given parameters
			if (methods[i].getReturnType().equals(ModelAndView.class)) {
				// We have a potential handler method, with the correct return type
				Class[] params = methods[i].getParameterTypes();
				
				// Check that the number and types of methods is correct.
				// We don't care about the declared exceptions
				if (params.length >= 2 && params[0].equals(HttpServletRequest.class) && params[1].equals(HttpServletResponse.class)) {
					// We're in business
					logger.config("Found action method [" + methods[i] + "]");
					methodHash.put(methods[i].getName(), methods[i]);
					
					// Look for corresponding LastModified method
					try {
						Method lastModifiedMethod = delegate.getClass().getMethod(methods[i].getName() + LAST_MODIFIED_METHOD_SUFFIX, new Class[] { HttpServletRequest.class } );
						// Put in cache, keyed by handler method name
						lastModifiedMethodHash.put(methods[i].getName(), lastModifiedMethod);
						logger.config("Found last modified method for action method [" + methods[i] + "]");
					}
					catch (NoSuchMethodException ex) {
						// No last modified method. That's ok
					}
				}
			}	// for each method with the correct return type
		} 	// for each method in the class
		
		// There must be SOME handler methods
		
		// WHAT IF SETTING DELEGATE LATER!?
		if (methodHash.isEmpty()) {
			String mesg = "No handler methods in class " + getClass().getName();
			logger.severe(mesg);
			throw new ApplicationContextException(mesg);
		}
		
		
		// Now look for exception handlers
		exceptionHandlerHash = new HashMap();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getReturnType().equals(ModelAndView.class) &&
					methods[i].getParameterTypes().length == 3) {
				Class[] params = methods[i].getParameterTypes();
				if (params[0].equals(HttpServletRequest.class) && 
					params[1].equals(HttpServletResponse.class) &&
					Throwable.class.isAssignableFrom(params[2])
				) {
					// Have an exception handler
					exceptionHandlerHash.put(params[2], methods[i]);
					logger.config("Found exception handler method [" + methods[i] + "]");
				}
			}
		}
		
	}	// setDelegate
	
	
	//---------------------------------------------------------------------
	// Implementation of LastModified
	//---------------------------------------------------------------------
	/**
	 * Try to find an XXXXLastModified method, where XXXX is the name of a handler.
	 * Return -1, indicating that content must be updated, if there's no such handler.
	 * @see LastModified#getLastModified(HttpServletRequest)
	 */
	public final long getLastModified(HttpServletRequest request) {
		try {
			String handlerMethodName = methodNameResolver.getHandlerMethodName(request);
			Method lastModifiedMethod = (Method) this.lastModifiedMethodHash.get(handlerMethodName);
			if (lastModifiedMethod != null) {
				try {
					// Invoke the LastModified method
					Long wrappedLong = (Long) lastModifiedMethod.invoke(this.delegate, new Object[] { request });
					return wrappedLong.longValue();
				}
				catch (Exception ex) {
					// We encountered an error invoking the lastModified method
					// We can't do anything useful except log this, as we can't throw an exception
					logger.logp(Level.WARNING, "MultiActionController", "getLastModified", "Failed to invoke lastModified method", ex);
				}
			}	// if we had a lastModified method for this request
		}
		catch (NoSuchRequestHandlingMethodException ex) {
			// No handler method for this request. This shouldn't
			// happen, as this method shouldn't be called unless a previous invocation
			// of this class has generated content.
			// Do nothing, that's ok: we'll return default
		}
		// The default if we didn't find a method
		return -1L;
	}

	//---------------------------------------------------------------------
	// Implementation of Controller
	//---------------------------------------------------------------------
	/**
	 * @see AbstractController#handleRequestInternal(HttpServletRequest, HttpServletResponse)
	 */
	public final ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {

		try {
			String name = methodNameResolver.getHandlerMethodName(request);
			return invokeNamedMethod(name, request, response);
		}
		catch (NoSuchRequestHandlingMethodException ex) {
			throw new ServletException("No handler", ex);
		}
	}
	
	
	/**
	 * Invoke the named method.
	 * Use a custom exception handler if possible;
	 * otherwise, throw an unchecked exception;
	 * wrap a checked exception or Throwable
	 */
	protected final ModelAndView invokeNamedMethod(String method, HttpServletRequest request, HttpServletResponse response)
		throws NoSuchRequestHandlingMethodException, ServletException, IOException {
		Method m = null;
		
	
		try {
			m = (Method) methodHash.get(method);
			if (m == null)
				throw new NoSuchRequestHandlingMethodException(method, this);

			// A real generic Collection! Parameters to method
			List params = new LinkedList();
			params.add(request);
			params.add(response);
				
			if (m.getParameterTypes().length >= 3 && m.getParameterTypes()[2].equals(HttpSession.class) ){
				// Require a session
				HttpSession session = request.getSession(false);
				if (session == null)
					throw new ServletException("Session was required for method '" + method + "'");
				params.add(session);
			}
			
			// If last parameter isn't of HttpSession type it's a command
			if (m.getParameterTypes().length >= 3 && !m.getParameterTypes()[m.getParameterTypes().length - 1].equals(HttpSession.class)) {
				Object command = newCommandObject(m.getParameterTypes()[m.getParameterTypes().length - 1]);
				params.add(command);
				bind(command, request);
			}
			
			Object[] parray = params.toArray(new Object[params.size()]);
			//System.out.println(StringUtils.arrayToDelimitedString(parray, ","));
			ModelAndView mv = (ModelAndView) m.invoke(this.delegate, parray);
			return mv;
		}
		catch (IllegalAccessException ex) {
			throw new ServletException("Cannot invoke request handler method [" + m + "]: not accessible", ex);
		}
		catch (InvocationTargetException ex) {
			// This is what we're looking for: the handler method threw an exception
			Throwable t = ex.getTargetException();
			
			Method handler = getExceptionHandler(t);
			if (handler != null) {
				return invokeExceptionHandler(handler, request, response, t);
			}
			
			// If we get here, there was no custom handler
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			if (t instanceof Error) {
				throw (Error) t;
			}
			if (t instanceof ServletException) {
				throw (ServletException) t;
			}
			if (t instanceof IOException) {
				throw (IOException) t;
			}

			// Must be a checked application exception
			throw new ServletException("Uncaught exception", t);
		}
	}	// invokeNamedMethod
	
	
	/**
	 * Subclasses can override this if they want.
	 * Uses class.newInstance(), so commands need to have
	 * public no arg constructors.
	 */
	protected Object newCommandObject(Class clazz) throws ServletException {
		logger.info("Must create new command of " + clazz);
		try {
			Object command = clazz.newInstance();
			return command;
		}
		catch (Exception ex) {
			throw new ServletException("Cannot instantiate command " + clazz + "; does it have a public no arg constructor?", ex);
		}
	}
	
	protected void bind(Object command, HttpServletRequest request) throws ServletException {
		logger.info("Binding request parameters onto command");
		HttpServletRequestDataBinder binder = new HttpServletRequestDataBinder(command, "command");
		binder.bind(request);
		binder.closeNoCatch();
	}
	
	/**
	 * Can return null if not found
	 * @param ex Won't be a ServletException or IOException
	 */
	protected Method getExceptionHandler(Throwable exception) {
		Class exceptionClass = exception.getClass();
		logger.info("Trying to find handler for exception of " + exceptionClass);
		Method handler = (Method) exceptionHandlerHash.get(exceptionClass);
		while (handler == null && !exceptionClass.equals(Throwable.class)) {
			logger.info("Looking at superclass " + exceptionClass);
			exceptionClass = exceptionClass.getSuperclass();
			handler = (Method) exceptionHandlerHash.get(exceptionClass);
		}
		return handler;
	}
	
	
	/**
	 * Invoke the selected exception handler
	 * @param handler handler method to invoke
	 */
	private ModelAndView invokeExceptionHandler(Method handler, HttpServletRequest request, HttpServletResponse response, Throwable exception) throws ServletException, IOException {
		if (handler == null)
			throw new ServletException("No handler for exception", exception);
			
		// If we get here, we have a handler
		logger.info("Invoking exception handler [" + handler + "] for exception [" + exception + "]");
		try {
			ModelAndView mv = (ModelAndView) handler.invoke(this.delegate, new Object[] { request, response, exception }); 
			return mv;
		}
		catch (IllegalAccessException ex) {
			throw new ServletException("Cannot invoke request exception handler method [" + handler + "]: not accessible", ex);
		}
		catch (InvocationTargetException ex) {
			Throwable t = ex.getTargetException();
			if (t instanceof ServletException) {
				throw (ServletException) t;
			}
			if (t instanceof IOException) {
				throw (IOException) t;
			}
			// Shouldn't happen
			throw new ServletException("Unexpected exception thrown from exception handler method: ", t);
		}  
	}
	
}	// class MultiActionController