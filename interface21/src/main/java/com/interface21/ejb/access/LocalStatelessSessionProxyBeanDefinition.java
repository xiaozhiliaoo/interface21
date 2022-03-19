package com.interface21.ejb.access;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.DynamicProxy;
import java14.java.util.logging.Level;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/** 
* Bean definition that uses a dynamic proxy for the EJB
*/
public class LocalStatelessSessionProxyBeanDefinition extends AbstractLocalStatelessSessionBeanDefinition {

	/** Should we reuse EJB objects !? */
	private boolean reuseEjbObject;
	
	/** Should we flush the Ejb object? */
	private boolean flushEjbObject;
	
	
	/**
	 * Gets the reuseEjbObject.
	 * @return Returns a boolean
	 */
	public boolean getReuseEjbObject() {
		return reuseEjbObject;
	}

	/**
	 * Sets the reuseEjbObject. Default is false.
	 * Reusing EJBObjects is questionable. If this property is false,
	 * a new EJBObject will be created from the cached home before
	 * each method invocation on this object.
	 * @param reuseEjbObject The reuseEjbObject to set
	 */
	public void setReuseEjbObject(boolean reuseEjbObject) {
		this.reuseEjbObject = reuseEjbObject;
	}
	
	/**
	 * Cause a cached EJBOBject to be refreshed with the next invocation on 
	 * the proxy
	 */
	public void refreshEjbObject() {
		this.flushEjbObject = true;
	}
	
	
	/**
	 * Create a dynamic proxy for the EJB, then
	 * return a bean wrapper for the proxy.
	 */
	protected BeanWrapper newBeanWrapperInternal() {
		EJBLocalObject ejbObject = newSessionBeanInstance();
		// Custom EJB Proxy can change target with each call if the reuseEjbObject bean property is set
		Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { getBeanClass() }, 
			new LocalEjbProxy(getHome(), ejbObject));
		logger.config("Created EJB proxy for [" + ejbObject + "]");
		return new BeanWrapperImpl(proxy);
	}

	
	/**
	 * Dynamic proxy that's capable of switching target before each invocation if necessary
	 */
	private class LocalEjbProxy extends DynamicProxy {
		
		private EJBLocalHome home;
		
		/**
		 * Constructor for LocalEjbProxy.
		 * @param wrappedObject
		 * @param className
		 */
		public LocalEjbProxy(EJBLocalHome home, EJBLocalObject ejbObject) {
			super(ejbObject);
			this.home = home;
		}

		/**
		 * Refresh the session bean instance if necessary
		 * before method invocation
		 * @see DynamicProxy#beforeInvocation(Object, Method, Object[])
		 */
		protected boolean beforeInvocation(Object proxy, Method method, Object[] args) {
			if (!reuseEjbObject || flushEjbObject) {
				logger.fine("Getting new EJBObject from home");
				EJBLocalObject newEjbObject = newSessionBeanInstance();
				
				// Threadsafe because object references are atomic
				switchTarget(newEjbObject);
				if (logger.isLoggable(Level.FINE))
					logger.fine("Switched proxy target to new object");
				if (flushEjbObject)
					flushEjbObject = false;
			}
			else {
				if (logger.isLoggable(Level.INFO))
					logger.fine("No need to switch proxied target: using current EJB instance");
			}
			return true;
		}
	}
	
	// IF NO SUCH OBJECT, RETRY? ONCE!!!! only
	// THREADLOCAL? two complex
	
	// WHAT ABOUT SUCCESSIVE INVOCATIONS FROM SAME CLIENT!?

} 