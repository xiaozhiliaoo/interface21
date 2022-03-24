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

package org.springframework.core.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Resource implementation for class path resources.
 * Uses either the thread context class loader, a given ClassLoader
 * or a given Class for loading resources.
 *
 * <p>Supports resolution as <code>java.io.File</code> if the class path
 * resource resides in the file system, but not for resources in a JAR.
 * Always supports resolution as URL.
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see Thread#getContextClassLoader
 * @see ClassLoader#getResourceAsStream
 * @see Class#getResourceAsStream
 */
public class ClassPathResource extends AbstractResource {

	private final String path;

	private ClassLoader classLoader;

	private Class clazz;

	/**
	 * Create a new ClassPathResource for ClassLoader usage.
	 * A leading slash will be removed, as the ClassLoader
	 * resource access methods will not accept it.
	 * <p>The thread context class loader will be used for
	 * loading the resource.
	 * @param path the absolute path within the class path
	 * @see ClassLoader#getResourceAsStream
	 * @see Thread#getContextClassLoader
	 */
	public ClassPathResource(String path) {
		this(path, (ClassLoader) null);
	}

	/**
	 * Create a new ClassPathResource for ClassLoader usage.
	 * A leading slash will be removed, as the ClassLoader
	 * resource access methods will not accept it.
	 * @param path the absolute path within the classpath
	 * @param classLoader the class loader to load the resource with,
	 * or null for the thread context class loader
	 * @see ClassLoader#getResourceAsStream
	 */
	public ClassPathResource(String path, ClassLoader classLoader) {
		Assert.notNull(path, "path is required");
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		this.path = path;
		this.classLoader = classLoader;
	}

	/**
	 * Create a new ClassPathResource for Class usage.
	 * The path can be relative to the given class,
	 * or absolute within the classpath via a leading slash.
	 * @param path relative or absolute path within the class path
	 * @param clazz the class to load resources with
	 * @see Class#getResourceAsStream
	 */
	public ClassPathResource(String path, Class clazz) {
		Assert.notNull(path, "path is required");
		this.path = path;
		this.clazz = clazz;
	}

	/**
	 * Create a new ClassPathResource with optional ClassLoader and Class.
	 * Only for internal usage.
	 * @param path relative or absolute path within the classpath
	 * @param classLoader the class loader to load the resource with, if any
	 * @param clazz the class to load resources with, if any
	 */
	protected ClassPathResource(String path, ClassLoader classLoader, Class clazz) {
		Assert.notNull(path, "path is required");
		this.path = path;
		this.classLoader = classLoader;
		this.clazz = clazz;
	}

	public InputStream getInputStream() throws IOException {
		InputStream is = null;
		if (this.clazz != null) {
			is = this.clazz.getResourceAsStream(this.path);
		}
		else {
			ClassLoader cl = this.classLoader;
			if (cl == null) {
				// no class loader specified -> use thread context class loader
				cl = Thread.currentThread().getContextClassLoader();
			}
			is = cl.getResourceAsStream(this.path);
		}
		if (is == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be opened because it does not exist");
		}
		return is;
	}

	public URL getURL() throws IOException {
		URL url = null;
		if (this.clazz != null) {
			url = this.clazz.getResource(this.path);
		}
		else {
			ClassLoader cl = this.classLoader;
			if (cl == null) {
				// no class loader specified -> use thread context class loader
				cl = Thread.currentThread().getContextClassLoader();
			}
			url = cl.getResource(this.path);
		}
		if (url == null) {
			throw new FileNotFoundException(
					getDescription() + " cannot be resolved to URL because it does not exist");
		}
		return url;
	}

	public File getFile() throws IOException {
		return ResourceUtils.getFile(getURL(), getDescription());
	}

	public Resource createRelative(String relativePath) {
		String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
		return new ClassPathResource(pathToUse, this.classLoader, this.clazz);
	}

	public String getFilename() {
		return StringUtils.getFilename(this.path);
	}

	public String getDescription() {
		return "class path resource [" + this.path + "]";
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ClassPathResource) {
			ClassPathResource otherRes = (ClassPathResource) obj;
			return (this.path.equals(otherRes.path) && ObjectUtils.nullSafeEquals(this.clazz, otherRes.clazz));
		}
		return false;
	}

	public int hashCode() {
		return this.path.hashCode();
	}

}
