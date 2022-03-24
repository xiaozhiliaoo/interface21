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

package org.springframework.web.context.support;

import java.io.File;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.WebUtils;

/**
 * Convenient superclass for application objects running in a WebApplicationContext.
 * Provides getWebApplicationContext, getServletContext, and getTempDir methods.
 * @author Juergen Hoeller
 * @since 28.08.2003
 */
public abstract class WebApplicationObjectSupport extends ApplicationObjectSupport {

	/**
	 * Overrides the base class behavior to enforce running in an ApplicationContext.
	 * All accessors will throw IllegalStateException if not running in a context.
	 * @see #getApplicationContext
	 * @see #getMessageSourceAccessor
	 * @see #getWebApplicationContext
	 * @see #getServletContext
	 * @see #getTempDir
	 */
	protected boolean isContextRequired() {
		return true;
	}

	/**
	 * Return the current application context as WebApplicationContext.
	 * @throws IllegalStateException if not running in a WebApplicationContext
	 */
	protected final WebApplicationContext getWebApplicationContext() throws IllegalStateException {
		ApplicationContext ctx = getApplicationContext();
		if (!(ctx instanceof WebApplicationContext)) {
			throw new IllegalStateException(
					"WebApplicationObjectSupport instance [" + this +
					"] does not run in a WebApplicationContext but in: " + ctx);
		}
		return (WebApplicationContext) getApplicationContext();
	}

	/**
	 * Return the current ServletContext.
	 * @throws IllegalStateException if not running in a WebApplicationContext
	 */
	protected final ServletContext getServletContext() {
		return getWebApplicationContext().getServletContext();
	}

	/**
	 * Return the temporary directory for the current web application,
	 * as provided by the servlet container.
	 * @return the File representing the temporary directory
	 * @throws IllegalStateException if not running in a WebApplicationContext
	 */
	protected final File getTempDir() {
		return WebUtils.getTempDir(getServletContext());
	}

}
