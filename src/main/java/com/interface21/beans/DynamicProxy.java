package com.interface21.beans;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

/**
 * Java 1.3 Dynamic proxy implementation--usable itself--that allows
 * subclasses to customize behavior via hook methods.
 * @author Rod Johnson
 * @version 1.0
 */
public class DynamicProxy implements InvocationHandler {
	
	/**
	 * @param obj object to proxy
	 * @param clazz interface to proxy
	 * @return a new Proxy object for the given object proxying
	 * the given interface
	 */
	public static Object getProxy(Object obj, Class clazz) {
		Object proxy =
			Proxy.newProxyInstance(obj.getClass().getClassLoader(), new Class[] { clazz }, new DynamicProxy(obj));
		return proxy;
	}

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	private Object wrappedObject;
	
	
	private String prefix;


	//---------------------------------------------------------------------
	// Constructor
	//---------------------------------------------------------------------
	/**
	 * Take implemented interfaces as argument?
	 */
	public DynamicProxy(Object wrappedObject) {
		this.wrappedObject = wrappedObject;
		this.prefix = "[" + wrappedObject.hashCode() + "] " + wrappedObject.getClass().getName() + ".";
		logger.info("Created new proxy to implement [" + wrappedObject.getClass().getName() + "] and delegate to [" + wrappedObject + "]");
	}


	/**
	 * Switch the target we're proxying. 
	 * Subclasses should rarely need to call this method.
	 * It might, for example, be invoked by tye
	 * beforeInvocation() method to conceal object pooling.
	 * We don't attempt to check that it implements the required
	 * interfaces; a ClassCastException will be thrown at runtime
	 * if it doesn't.
	 * @param newTarget New object to proxy
	 */
	protected void switchTarget(Object newTarget) {
		// SYNCHROINIZATION IMPLICATIONS!?
		if (newTarget == null)
			throw new FatalBeanException("Can't proxy null", null);
		this.wrappedObject = newTarget;
		if (logger.isLoggable(Level.INFO))
			logger.info("Switched proxied object to [" + newTarget + "]");
	}
	
	/**
	 * @return the proxied object
	 */
	public Object getTarget() {
		return wrappedObject;
	}
	

	//---------------------------------------------------------------------
	// Implementation of InvocationHandler
	//---------------------------------------------------------------------
	public final Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		long started = System.currentTimeMillis();
		try {
			return invokeInternal(proxy, method, args);
		}
		catch (InvocationTargetException ite) {
			// The method threw an exception
			Throwable methodThrew = ite.getTargetException();
			if (isCheckedException(methodThrew)) {
				methodInvocationThrewCheckedException(proxy, method, args, (Exception) methodThrew);
			}
			else {
				methodInvocationThrewRuntimeException(proxy, method, args, methodThrew);
			}
			// Throw the same exception to caller
			throw methodThrew;
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Proxied method [" + method + "] is not accessible", ex);
		}
		catch (Throwable t) {
			// A proxy infrastructure exception
			//throw new FatalBeanException("Fatal proxy exception invoking method [" + method + "]", ex);
			throw t;
		}
		finally {
			long et = System.currentTimeMillis() - started;
			if (logger.isLoggable(Level.INFO))
				logger.info("Invoked [" + method + "] in " + et + "ms");
			cleanUp(method);
		}
	}	// invoke
	

	/**
	 * Perform the actual method invocation
	 * @param proxy the proxy instance the method was invoked on (this object)
	 * @param method method to invoke
	 * @param args arguments to the method, or null if the method takes no arguments.
	 * Primitives will be wrapped.
	 */
	private Object invokeInternal(Object proxy, Method method, Object[] args) throws IllegalAccessException, InvocationTargetException {
		if (logger.isLoggable(Level.FINE))
			// Building this string is slow, so we don't do it unless we know it _will_ be logged
			logger.fine("About to invoke: " + invocationText(method, args));

		if (beforeInvocation(proxy, method, args)) {
			Object obj = method.invoke(wrappedObject, args);
			methodInvocationSucceeded(proxy, method, args);
			return obj;
		}
		else {
			// Invocation skipped
			logger.info("Didn't invoke method '" + method.getName() + "': beforeInvoke() said no");
			return null;
		}
	}	// invokeInternal
	
	
	/**
	 * Return a string describing the invocation
	 */
	private String invocationText(Method method, Object[] args) {
		StringBuffer msg = new StringBuffer();
		msg.append(prefix).append(method.getName()).append("(");
		if(args != null && args.length > 0) {
			int i;
			for(i = 0; i < args.length - 1; i++) {
				msg.append(args[i]).append(",");
			}
			msg.append(args[i]);
		}
		msg.append(")");
		return msg.toString();
	}
	
	/**
	 * @return true if the Throwable is checked exception
	 */
	private boolean isCheckedException(Throwable t) {
		// RuntimeException is a subclass of Exception, so we must exclude runtime exceptions first
		if (t instanceof RuntimeException)
			return false;
		if (t instanceof Exception)
			return true;
		return false;
	}

	//---------------------------------------------------------------------
	// Hook methods that may be overriden by subclasses to customize behavior
	//---------------------------------------------------------------------
	/**
	 * Subclasses could override this to check whether a property has changed
	 * @return whether or not to invoke the underlying method (cancelling
	 * invocation might be applied if we've cached data, or don't need
	 * to call a void method). This implementation always returns true.
	 */
	protected boolean beforeInvocation(Object proxy, Method method, Object[] args) {
		return true;
	}

	/**
	 * This implementation does nothing
	 * RETRY SUPPORT!????
	 */
	protected void methodInvocationSucceeded(Object proxy, Method method, Object[] args) {
	}
	
	/**
	 * Invoked on a checked exception
	 */
	protected void methodInvocationThrewCheckedException(Object proxy, Method method, Object[] args, Exception ex) {
	}
	
	/**
	 * Invoked on a runtime exception
	 */
	protected void methodInvocationThrewRuntimeException(Object proxy, Method method, Object[] args, Throwable t) {
	}
	
	/**
	 * Do any cleanup after an invocation is complete
	 */
	protected void cleanUp(Method method) {
	}

}	// class DynamicProxy