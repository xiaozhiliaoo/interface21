package com.interface21.beans.propertyeditors;

import com.interface21.util.StringUtils;

import java.beans.PropertyEditorSupport;


/**
 * NB: must be registered
 * Is registered by BeanWrapperImpl
 * Format is defined in java.util.Properties documentation.
 * Each property must be on a new line.
 */
public class StringArrayPropertyEditor extends PropertyEditorSupport {
	/**
	 * @see PropertyEditor#setAsText(String)
	 */
	public void setAsText(String s) throws IllegalArgumentException {
		String[] sa = StringUtils.commaDelimitedListToStringArray(s);

		setValue(sa);
	}

}

