/*
 * $Id: DOMAdapter.java,v 1.1.1.1 2002/08/01 12:52:05 Rod Johnson Exp $
 * $Source: c:\\work\\cvs/framework/src/org/infohazard/domify2/DOMAdapter.java,v $
 */

package org.infohazard.domify2;

import org.apache.log4j.Category;
import org.w3c.dom.Node;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 */
public class DOMAdapter {

	/** Logging category for log4j. */
	protected static Category log = Category.getInstance(DOMAdapter.class.getName());

	/**
	 * Set of Methods to be ignored. Allows avoidance
	 * of cycles through the ignoreProperty() method.
	 */
	private Set ignoredMethods = new HashSet();


	/**
	 * Callers can invoke this to prevent cycles by excluding certain properties
	 * from domification. The given property will not be exposed by the
	 * element created for the given class. This method can also be used
	 * to simplify the generated XML, or prevent expensive-to-invoke properties
	 * being called.
	 * @param clazz class exposing the property to ignore.
	 * <b>Note: must be the concrete class. Won't work on an interface.
	 * </b> For example, if MyClass implements MyInterface, which includes the getFoo() property,
	 * we must call ignoreProperty(MyClass.class, "foo"), not ignoreProperty(MyInterface.class, "foo").
	 * If multiple classes implement the sameinterface, call this method for each implementing class.
	 * @param property property to ignore, such as "foo". <b>Do not prepend with get or is.</b>
	 */
	public void ignoreProperty(Class clazz, String property) {
		try {
			PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			for (int i = 0; i < pds.length; i++) {
				if (pds[i].getName().equals(property)) {
					String getterName = pds[i].getReadMethod().getName();
					log.info("Ignored method on " + clazz + " is '" + getterName + "'");
					ignoredMethods.add(pds[i].getReadMethod());
				}
			}
		}
		catch (IntrospectionException ex) {
			throw new RuntimeException("Can't get bean info for class '" + clazz.getName() + "'");
		}
	}


	/**
	 * Other classes within domify must invoke this for each exposed method to
	 * establish whether or not this property getter method has been excluded by
	 * a user's call to the ignoreProperty() method in this class.
	 * @param m property getter method
	 */
	protected boolean shouldIgnoreMethod(Method m) {
		// Short-circuit evaluation might improve performance
		boolean result = !ignoredMethods.isEmpty() && ignoredMethods.contains(m);
		if (result && log.isInfoEnabled())
			log.info("Ignoring excluded property getter method " + m);
		return result;
	}

	/**
	 */
	public DOMAdapter() {
	}

	/**
	 * List of classes which should be converted to string immediately rather
	 * than exposing their getters as properties.
	 */
	protected List toStringClasses = new ArrayList();

	// Initialize with the things we *know* should be strings
	{
		toStringClasses.add(String.class);
		toStringClasses.add(StringBuffer.class);
		toStringClasses.add(Character.class);
		toStringClasses.add(Boolean.class);
		toStringClasses.add(Number.class);
	}

	/**
	 * Adds a class to the list of classes which are converted to String
	 * immediately rather than exposing their getters as properties.
	 */
	public void stringify(Class cls) {
		if (!toStringClasses.contains(cls))
			this.toStringClasses.add(cls);
	}

	/**
	 */
	protected boolean shouldConvertToString(Object something) {
		Iterator it = toStringClasses.iterator();
		while (it.hasNext()) {
			Class c = (Class) it.next();

			if (c.isInstance(something))
				return true;
		}

		return false;
	}

	/**
	 * The primary method of this package; use this to adapt
	 * a Java object to a DOM representation.
	 *
	 * @param rootName - the name assigned to the root element of the document.
	 */
	public Node adapt(Object root, String rootName) {
		return new DocumentAdapter(root, rootName, this);
	}
}