package com.interface21.beans;

import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import java.beans.IndexedPropertyDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import com.interface21.beans.propertyeditors.*;

/**
 * Default implementation of the BeanWrapper interface
 * that should be sufficient for all normal uses. Caches
 * introspection results for efficiency.
 * <p>Note: this class never tries to load a class by name.
 * This won't work if this class is used in a WAR but was loaded
 * by the EJB class loader and the class to be loaded is in the WAR.
 * (This class would use the EJB classloader, which couldn't see
 * the required class.) We don't attempt to solve such problems by
 * obtaining the classloader at runtime, because this violates
 * the EJB programming restrictions.
 * @author  Rod Johnson
 * @since 15 April 2001
 */
public class BeanWrapperImpl implements BeanWrapper {

	public final static boolean DEFAULT_EVENT_PROPAGATION_ENABLED = false;

	// Install default property editors
	// IS THERE A WAY OF GETTING ALL IN THIS PACKAGE AUTOMATICALLY?
	// Would need what it did, however
	static {
		//PropertyEditorManager.registerEditor(Properties.class, PropertiesEditor.class);
		PropertyEditorManager.registerEditor(String[].class, StringArrayPropertyEditor.class);
		
		PropertyEditorManager.setEditorSearchPath(new String[] {
			"sun.beans.editors",
			"com.interface21.beans.propertyeditors"
		});
	}

	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	 * We'll create a lot of these objects, so we don't want a new logger every time
	 */
	private static Logger logger = Logger.getLogger(BeanWrapperImpl.class.getName());

	/** The wrapped object */
	private Object                      object;

	private CachedIntrospectionResults cachedIntrospectionResults;

	/** Standard java.beans helper object used to propagate
	 * events
	 */
	private VetoableChangeSupport	vetoableChangeSupport;

	/** Standard java.beans helper object used to propagate
	 * events
	 */
	private PropertyChangeSupport	propertyChangeSupport;

	/** Should we propagate events!? */
	private boolean					eventPropagationEnabled = DEFAULT_EVENT_PROPAGATION_ENABLED;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/**
	 * Creates new BeanWrapperImpl with default event propagation (disabled)
	 */
	public BeanWrapperImpl(Object object) throws BeansException {
		this(object, DEFAULT_EVENT_PROPAGATION_ENABLED);
	}

	/**
	 * Creates new BeanWrapperImpl, allowing specification of whether event
	 * propagation is enabled.
	 * @param eventPropagationEnabled whether event propagation should be enabled
	 */
	public BeanWrapperImpl(Object object, boolean eventPropagationEnabled) throws BeansException {
		this.eventPropagationEnabled = eventPropagationEnabled;
		setObject(object);
	}


	/** Creates new BeanWrapperImpl */
	public BeanWrapperImpl(Class clazz) throws BeansException {
		cachedIntrospectionResults = CachedIntrospectionResults.forClass(clazz);
		setObject(BeanUtils.instantiateClass(clazz));
	}

	/** Creates new BeanWrapperImpl */
	private BeanWrapperImpl(CachedIntrospectionResults cachedIntrospectionResults) throws BeansException {
		this.cachedIntrospectionResults = cachedIntrospectionResults;
		setObject(BeanUtils.instantiateClass(cachedIntrospectionResults.getBeanClass()));
	}

	/** Creates new BeanWrapperImpl */
	private BeanWrapperImpl(CachedIntrospectionResults cachedIntrospectionResults, Object obj) throws BeansException {
		this.cachedIntrospectionResults = cachedIntrospectionResults;
		setObject(obj);
	}

	/**
	 * This method is included for efficiency. If an implementation
	 * caches all necessary information about the class,
	 * it might be <b>much</b> faster to instantiate a new wrapper copying
	 * the cached information, than to use introspection again.
	 * The wrapped instance is independent, as is the new BeanWrapper:
	 * only the cached introspection information is copied.
	 */
	public BeanWrapper newWrapper(Object obj) throws BeansException {
		if (!this.cachedIntrospectionResults.getBeanClass().equals(obj.getClass()))
			throw new FatalBeanException("Cannot create new wrapper for object of class " + obj.getClass().getName() + " using cached information for class " + cachedIntrospectionResults.getBeanClass(), null);
		return new BeanWrapperImpl(this.cachedIntrospectionResults, obj);
	}

	private void setObject(Object object) throws BeansException {
		if (object == null)
			throw new FatalBeanException("Cannot set BeanWrapperImpl target to a null object", null);
		this.object = object;
		if (cachedIntrospectionResults == null || !cachedIntrospectionResults.getBeanClass().equals(object.getClass())) {
			cachedIntrospectionResults = CachedIntrospectionResults.forClass(object.getClass());
		}
		setEventPropagationEnabled(this.eventPropagationEnabled);
		// assert: cachedIntrospectionResults != null
	}

	//---------------------------------------------------------------------
	// Implementation of BeanWrapper
	//---------------------------------------------------------------------
	public void setWrappedInstance(Object obj) throws BeansException {
		setObject(object);
	}

	public void newWrappedInstance() throws BeansException {
		this.object = BeanUtils.instantiateClass(getWrappedClass());
		vetoableChangeSupport = new VetoableChangeSupport(object);
	}

	public Class getWrappedClass() {
		return object.getClass();
	}

	public Object getWrappedInstance() {
		return object;
	}

	/**
	 * Convert the value to the required type.
	 * Conversions from String to any type use the setAsTest() method of
	 * the PropertyEditor class. Note that a PropertyEditor must be registered
	 * for this class for this to work. This is a standard Java Beans API.
	 */
	private PropertyChangeEvent createPropertyChangeEventWithTypeConversionIfNecessary(Object target, String propertyName, Object oldValue, Object newValue, Class requiredType) throws BeansException {
		// Only need to cast if value isn't null
		if (newValue != null) {
			// We may need to change the value of newValue
			if (!requiredType.isAssignableFrom(newValue.getClass()) && (newValue instanceof String)) {
				if (logger.isLoggable(Level.FINEST))
					logger.finest("Convert: string to " + requiredType);
				PropertyEditor pe = PropertyEditorManager.findEditor(requiredType);
				if (logger.isLoggable(Level.FINEST))
					logger.finest("Using property editor [" + pe + "]");
				if (pe != null) {
					try {
						pe.setAsText((String) newValue);
						newValue = pe.getValue();
					}
					catch (IllegalArgumentException ex) {
						throw new TypeMismatchException(new PropertyChangeEvent(target, propertyName, oldValue, newValue), requiredType);
					}
				}
			}
		}
		return new PropertyChangeEvent(target, propertyName, oldValue, newValue);
	}	// createPropertyChangeEventWithTypeConversionIfNecessary


	public void setPropertyValue(String propertyName, Object value) throws PropertyVetoException, BeansException {
		setPropertyValue(new PropertyValue(propertyName, value));
	}   // setPropertyValue


	private boolean isNestedProperty(String path) {
		return path.indexOf(NESTED_PROPERTY_SEPARATOR) != -1;
	}

	/**
	 * Works if not nested
	 */
	private String getFinalPath(String nestedPath) {
		return nestedPath.substring(nestedPath.lastIndexOf(NESTED_PROPERTY_SEPARATOR) + 1);
	}


	/**
	 * Recursive
	 */
	private BeanWrapper getBeanWrapperForNestedProperty(BeanWrapper bw, String path) {
		// WHAT ABOUT INDEXED PROEPRTIES!?

		int pos = path.indexOf(NESTED_PROPERTY_SEPARATOR);
		// Handle nested properties recursively
		if (pos > -1) {
			String nestedProperty = path.substring(0, pos);
			String nestedPath = path.substring(pos + 1);
			logger.fine("Navigating to property path '" + nestedPath + "' of nested property '" + nestedProperty + "'");
			// Could consider caching these, but they're not that expensive to instantiate
			Object propertyValue = bw.getPropertyValue(nestedProperty);
			if (propertyValue == null)
				throw new NullValueInNestedPathException(bw.getWrappedClass(), nestedProperty);
			BeanWrapper nestedBw = new BeanWrapperImpl(propertyValue, false);
			return getBeanWrapperForNestedProperty(nestedBw, nestedPath);
		}
		else {
			return bw;
		}
	}

	/**
	 * Set an individual field
	 * All other setters go through this
	 */
	public void setPropertyValue(PropertyValue pv) throws PropertyVetoException, BeansException {

		if (isNestedProperty(pv.getName())) {
			try {
				BeanWrapper nestedBw = getBeanWrapperForNestedProperty(this, pv.getName());
				nestedBw.setPropertyValue(new PropertyValue(getFinalPath(pv.getName()), pv.getValue()));
				return;
			}
			catch (NullValueInNestedPathException ex) {
				// Let this through
				throw ex;
			}
			catch (FatalBeanException ex) {
				// Error in the nested path
				throw new NotWritablePropertyException(pv.getName(), getWrappedClass());
			}
		}

		// WHAT ABOUT INDEXED PROEPRTIES!?
		int pos = pv.getName().indexOf(NESTED_PROPERTY_SEPARATOR);
		// Handle nested properties recursively
		if (pos > -1) {
			String nestedProperty = pv.getName().substring(0, pos);
			String nestedPath = pv.getName().substring(pos + 1);
			logger.fine("Navigating to property path '" + nestedPath + "' of nested property '" + nestedProperty + "'");
			// Could consider caching these, but they're not that expensive to instantiate
			BeanWrapper nestedBw = new BeanWrapperImpl(getPropertyValue(nestedProperty), false);
			nestedBw.setPropertyValue(new PropertyValue(nestedPath, pv.getValue()));
			return;
		}


		if (!isWritableProperty(pv.getName()))
			throw new NotWritablePropertyException(pv.getName(), getWrappedClass());
		PropertyDescriptor pd = getPropertyDescriptor(pv.getName());
		Method writeMethod = pd.getWriteMethod();
		Method readMethod = pd.getReadMethod();
		Object oldValue = null;	// May stay null if it's not a readable property
		PropertyChangeEvent propertyChangeEvent = null;

		try {
			if (readMethod != null && eventPropagationEnabled) {
				// Can only find existing value if it's a readable property
				try {
					oldValue = readMethod.invoke(object, new Object[] { });
				}
				catch (Exception ex) {
					// The getter threw an exception, so we couldn't retrieve the old value.
					// We're not really interested in any exceptions at this point,
					// so we merely log the problem and leave oldValue null
					logger.logp(Level.WARNING, "BeanWrapperImpl", "setPropertyValue",
						"Failed to invoke getter '" + readMethod.getName() + "' to get old property value before property change: getter probably threw an exception",
						ex);
				}
			}

			// Old value may still be null
			propertyChangeEvent = createPropertyChangeEventWithTypeConversionIfNecessary(object, pv.getName(), oldValue, pv.getValue(), pd.getPropertyType());

			// May throw PropertyVetoException: if this happens the PropertyChangeSupport
			// class fires a reversion event, and we jump out of this method, meaning
			// the change was never actually made
			if (eventPropagationEnabled) {
				vetoableChangeSupport.fireVetoableChange(propertyChangeEvent);
			}

			// Make the change
			if (logger.isLoggable(Level.FINEST))
				logger.finest("About to invoke write method [" + writeMethod + "] on object of class '" + object.getClass().getName() + "'");
			writeMethod.invoke(object, new Object[] { propertyChangeEvent.getNewValue() });
			if (logger.isLoggable(Level.FINEST))
				logger.finest("Invoked write method [" + writeMethod + "] ok");

			// If we get here we've changed the property OK and can broadcast it
			if (eventPropagationEnabled)
				propertyChangeSupport.firePropertyChange(propertyChangeEvent);
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof PropertyVetoException)
				throw (PropertyVetoException) ex.getTargetException();
			if (ex.getTargetException() instanceof ClassCastException)
				throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
			throw new MethodInvocationException(ex.getTargetException(), propertyChangeEvent);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("illegal attempt to set property [" + pv + "] threw exception", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);
		}
	}   // setPropertyValue


	/** Bulk update from a Map.
	 * Bulk updates from PropertyValues are more powerful: this ,method is
	 * provided for convenience.
	 */
	public void setPropertyValues(Map map) throws BeansException {
		setPropertyValues(new MutablePropertyValues(map));
	}

	/**
	 * <b>The preferred way to perform a bulk update.</b>
	 * Note that performing a bulk update differs from performing a single update,
	 * in that an implementation of this class will continue to update properties
	 * if a <b>recoverable</b> error (such as a vetoed property change or a type mismatch,
	 * but <b>not</b> an invalid fieldname or the like) is encountered, throwing a
	 * PropertyVetoExceptionsException containing all the individual errors.
	 * This exception can be examined later to see all binding errors.
	 * Properties that were successfully updated stay changed.
	 * @param pvs PropertyValues to set on the target object
	 * @param ignoreUnknown should we ignore unknown values (not found in the bean!?)
	 */
	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false, null);
	}

	/**
	 * Perform a bulk update.
	 * All bulk update methods go through this method
	 * PRE: vlaues set
	 * POSt values set??
	 * @param propertyValues Description of properties to be set in the update
	 **/
	public void setPropertyValues(PropertyValues propertyValues, boolean ignoreUnknown, PropertyValuesValidator pvsValidator) throws BeansException {
		// Composite exception to save individual property veto exceptions in

		// Create only if needed
		PropertyVetoExceptionsException propertyVetoExceptionsException = new PropertyVetoExceptionsException(this);

		if (pvsValidator != null) {
			try {
				pvsValidator.validatePropertyValues(propertyValues);
			}
			catch (InvalidPropertyValuesException ipvex) {
				propertyVetoExceptionsException.addMissingFields(ipvex);
			}
		}

		PropertyValue[] pvs = propertyValues.getPropertyValues();
		for (int i = 0; i < pvs.length; i++) {
			try {
				// This method may throw ReflectionException, which won't be caught
				// here, if there is a critical failure such as no matching field.
				// We can attempt to deal only with less serious exceptions
				setPropertyValue(pvs[i]);
			}
			// Fatal ReflectionExceptions will just be rethrown
			catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown)
					throw ex;
				// Otherwise, just ignore it and continue...
			}
			catch (PropertyVetoException ex) {
				//throw new ReflectionException("Property veto on prototype [" + name + "]: property [" + key + "]", ex);
				propertyVetoExceptionsException.addPropertyVetoException(ex);
			}
			catch (TypeMismatchException ex) {
				propertyVetoExceptionsException.addTypeMismatchException(ex);
			}
			catch (MethodInvocationException ex) {
				propertyVetoExceptionsException.addMethodInvocationException(ex);
			}
		}   // for each property

		// If we encountered individual exceptions, throw the composite exception
		if (propertyVetoExceptionsException.getExceptionCount() > 0) {
			throw propertyVetoExceptionsException;
		}
	}	// setPropertyValues


	public Object getPropertyValue(String propertyName) throws BeansException {

		// if null!?

		if (isNestedProperty(propertyName)) {
			BeanWrapper nestedBw = getBeanWrapperForNestedProperty(this, propertyName);
			logger.finest("Final path in nested property value '" + propertyName + "' is '" + getFinalPath(propertyName) + "'");
			return nestedBw.getPropertyValue(getFinalPath(propertyName));
		}

		PropertyDescriptor pd = getPropertyDescriptor(propertyName);
		Method m = pd.getReadMethod();
		if (m == null)
			throw new FatalBeanException("Cannot get scalar property [" + propertyName + "]: not readable", null);
		try {
			return m.invoke(object, null);
		}
		catch (InvocationTargetException ex) {
			throw new FatalBeanException("getter for property [" + propertyName + "] threw exception", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("illegal attempt to get property [" + propertyName + "] threw exception", ex);
		}
	}

	/** Get the value of an indexed property
	 * @param propertyName name of the property to get value of
	 * @param index index from 0 of the property
	 * @return the value of the property
	 */
	public Object getIndexedPropertyValue(String propertyName, int index) throws BeansException {
		PropertyDescriptor pd = getPropertyDescriptor(propertyName);
		if (!(pd instanceof IndexedPropertyDescriptor))
			throw new FatalBeanException("Cannot get indexed property value for [" + propertyName + "]: this property is not an indexed property", null);
		Method m = ((IndexedPropertyDescriptor) pd).getIndexedReadMethod();
		if (m == null)
			throw new FatalBeanException("Cannot get indexed property [" + propertyName + "]: not readable", null);
		try {
			return m.invoke(object, new Object[] { new Integer(index) });
		}
		catch (InvocationTargetException ex) {
			throw new FatalBeanException("getter for indexed property [" + propertyName + "] threw exception", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("illegal attempt to get indexed property [" + propertyName + "] threw exception", ex);
		}
	}

	public PropertyDescriptor[] getProperties() throws BeansException {
		return cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
	}


	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		return cachedIntrospectionResults.getPropertyDescriptor(propertyName);
	}

	public boolean isReadableProperty(String propertyName) {
		try {
			return getPropertyDescriptor(propertyName).getReadMethod() != null;
		}
		catch (BeansException ex) {
			// Doesn't exist, so can't be readable
			return false;
		}
	}

	public boolean isWritableProperty(String propertyName) {
		try {
			return getPropertyDescriptor(propertyName).getWriteMethod() != null;
		}
		catch (BeansException ex) {
			// Doesn't exist, so can't be writable
			return false;
		}
	}

	/**
	 * Invoke a method
	 * @see BeanWrapper#invoke(String, Object[])
	 */
	public Object invoke(String methodName, Object[] args) throws BeansException {
		try {
			MethodDescriptor md = this.cachedIntrospectionResults.getMethodDescriptor(methodName);
			if (logger.isLoggable(Level.FINE))
				logger.fine("About to invoke method [" + methodName + "]");
			Object returnVal = md.getMethod().invoke(this.object, args);
			if (logger.isLoggable(Level.FINE))
				logger.fine("Successfully invoked method [" + methodName + "]");
			return returnVal;
		}
		catch (InvocationTargetException ex) {
			//if (ex.getTargetException() instanceof ClassCastException)
			//	throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex);

			// CHECK!!!!
			throw new MethodInvocationException(ex.getTargetException(), null);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Illegal attempt to invoke method [" + methodName + "] threw exception", ex);
		}
		catch (IllegalArgumentException ex) {
			throw new FatalBeanException("Illegal argument to method [" + methodName + "] threw exception", ex);
		}
	}

	public PropertyDescriptor[] getPropertyDescriptors() {
		return cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
	}

	//---------------------------------------------------------------------
	// Bean event support
	//---------------------------------------------------------------------
	public void addVetoableChangeListener(VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.addVetoableChangeListener(l);
	}

	public void removeVetoableChangeListener(VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.removeVetoableChangeListener(l);
	}

	public void addVetoableChangeListener(String propertyName, VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.addVetoableChangeListener(propertyName, l);
	}

	public void removeVetoableChangeListener(String propertyName, VetoableChangeListener l) {
		if (eventPropagationEnabled)
			vetoableChangeSupport.removeVetoableChangeListener(propertyName, l);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.removePropertyChangeListener(l);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.addPropertyChangeListener(propertyName, l);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
		if (eventPropagationEnabled)
			propertyChangeSupport.removePropertyChangeListener(propertyName, l);
	}

	public boolean isEventPropagationEnabled() {
		return eventPropagationEnabled;
	}

	/**
	 * Disabling event propagation improves
	 * performance
	 */
	public void setEventPropagationEnabled(boolean flag) {
		this.eventPropagationEnabled = flag;
		// Lazily initialize support for events if not already
		// initialized
		if (eventPropagationEnabled && (vetoableChangeSupport == null || propertyChangeSupport == null)) {
			vetoableChangeSupport = new VetoableChangeSupport(object);
			propertyChangeSupport = new PropertyChangeSupport(object);
		}
	}


	//---------------------------------------------------------------------
	// Diagnostics
	//---------------------------------------------------------------------
	/**
	 * Expensive! Only call for diagnostics and debugging reasons,
	 * not in production
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append("BeanWrapperImpl: eventPropagationEnabled=" + eventPropagationEnabled + " wrapping [" + getWrappedInstance().getClass() + "]; ");
			PropertyDescriptor pds[] = getPropertyDescriptors();
			if (pds != null)
				for (int i = 0; i < pds.length; i++) {
					Object val = getPropertyValue(pds[i].getName());
					String valStr = (val != null) ? val.toString() : "null";
					sb.append(pds[i].getName() + "={" + valStr + "}");
				}
		}
		catch (Exception ex) {
			sb.append("exception encountered: " + ex);
		}
		return sb.toString();
	}   // toString

}	// class BeanWrapperImpl