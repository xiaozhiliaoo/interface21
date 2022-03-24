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
import java.io.File;

import org.springframework.util.StringUtils;

/**
 * Editor for <code>java.io.File</code>, to directly populate a File property
 * instead of using a String file name property as bridge.
 *
 * <p>Supports any pathname accepted by the <code>File(pathname)</code>
 * constructor.
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see File#File(String)
 */
public class FileEditor extends PropertyEditorSupport {

	public void setAsText(String text) throws IllegalArgumentException {
		if (StringUtils.hasText(text)) {
			setValue(new File(text));
		}
		else {
			setValue(null);
		}
	}

	public String getAsText() {
		if (getValue() != null) {
			return ((File) getValue()).getAbsolutePath();
		}
		else {
			return "";
		}
	}

}
