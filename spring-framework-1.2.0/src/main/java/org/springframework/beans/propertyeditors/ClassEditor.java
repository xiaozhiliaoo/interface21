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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * Editor for <code>java.lang.Class</code>, to directly populate a Class property
 * instead of using a String class name property as bridge.
 *
 * <p>Also supports "java.lang.String[]"-style array class names,
 * in contrast to the standard <code>Class.forName</code> method.
 * Delegates to ClassUtils for actual class name resolution.
 *
 * @author Juergen Hoeller
 * @since 13.05.2003
 * @see Class#forName
 * @see ClassUtils#forName
 */
public class ClassEditor extends PropertyEditorSupport {

	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			try {
				setValue(ClassUtils.forName(text.trim()));
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Invalid class name: " + ex.getMessage());
			}
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		Class clazz = (Class) getValue();
		return (clazz.isArray() ? clazz.getComponentType().getName() + ClassUtils.ARRAY_SUFFIX : clazz.getName());
	}

}
