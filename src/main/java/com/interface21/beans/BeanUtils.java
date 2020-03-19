
package com.interface21.beans;

/**
 *
 * @author  rod
 * @version
 */
public abstract class BeanUtils {

	/** Could use THIS IN BEANWRAPPER IMPL as well
	 * @throws IllegalArgumentException
	 */
	/*
	public static Object fromString(Class requiredType, String s) throws IllegalArgumentException {
		if (!requiredType.equals(String.class)) {
			//System.out.println("Convert: string to "+ requiredType);
			PropertyEditor pe = PropertyEditorManager.findEditor(requiredType);
			//System.out.println("using property editor" + pe);
			if (pe != null) {
				// Might throw IllegalArgumentException
				pe.setAsText(s);
				return pe.getValue();
			}
		}
		else {
			return s;
		}
	}
	 */

	public static Object instantiateClass(Class clazz) throws BeansException {
		try {
			//Object bean = Beans.instantiate(null, className);
			return clazz.newInstance();
		}
		catch (InstantiationException ex) {
			throw new FatalBeanException("Cannot instantiate [" + clazz + "]; is it an interface or an abstract class?", ex);
		}
		catch (IllegalAccessException ex) {
			throw new FatalBeanException("Cannot instantiate [" + clazz + "]; has class definition changed? Is there a public constructor?", ex);
		}
	}

}