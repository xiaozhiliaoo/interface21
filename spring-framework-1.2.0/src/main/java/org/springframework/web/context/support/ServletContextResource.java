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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * Resource implementation for ServletContext resources,
 * interpreting relative paths within the web application root.
 *
 * <p>Always supports stream access, but only allows java.io.File
 * access when the web application archive is expanded.
 * Always supports resolution as URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see ServletContext#getResourceAsStream
 * @see ServletContext#getRealPath
 */
public class ServletContextResource extends AbstractResource {

	private final ServletContext servletContext;

	private final String path;

	/**
	 * Create a new ServletContextResource.
	 * <p>The Servlet spec requires that resource paths start with a slash,
	 * even if many containers accept paths without leading slash too.
	 * Consequently, the given path will be prepended with a slash if it
	 * doesn't already start with one.
	 * @param servletContext the ServletContext to load from
	 * @param path the path of the resource
	 */
	public ServletContextResource(ServletContext servletContext, String path) {
		// check ServletContext
		Assert.notNull(servletContext, "Cannot resolve ServletContextResource without ServletContext");
		this.servletContext = servletContext;

		// check path
		Assert.notNull(path, "path is required");
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		this.path = path;
	}

	/**
	 * Return the ServletContext for this resource.
	 */
	public ServletContext getServletContext() {
		return servletContext;
	}

	/**
	 * Return the path for this resource.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * This implementation delegates to ServletContext.getResourceAsStream,
	 * but throws a FileNotFoundException if not found.
	 * @see ServletContext#getResourceAsStream
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is = this.servletContext.getResourceAsStream(this.path);
		if (is == null) {
			throw new FileNotFoundException("Could not open " + getDescription());
		}
		return is;
	}

	public URL getURL() throws IOException {
		URL url = this.servletContext.getResource(this.path);
		if (url == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}

	/**
	 * This implementation delegates to ServletContext.getRealPath,
	 * but throws a FileNotFoundException if not found or not resolvable.
	 * @see ServletContext#getRealPath
	 */
	public File getFile() throws IOException {
		String realPath = WebUtils.getRealPath(this.servletContext, this.path);
		return new File(realPath);
	}

	public Resource createRelative(String relativePath) throws IOException {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return new ServletContextResource(this.servletContext, pathToUse);
	}

	public String getFilename() {
		return StringUtils.getFilename(this.path);
	}

	public String getDescription() {
		return "ServletContext resource [" + this.path + "]";
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ServletContextResource) {
			ServletContextResource otherRes = (ServletContextResource) obj;
			return (this.servletContext.equals(otherRes.servletContext) && this.path.equals(otherRes.path));
		}
		return false;
	}

	public int hashCode() {
		return this.path.hashCode();
	}

}
