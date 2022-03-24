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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

/**
 * ServletContext-aware subclass of PathMatchingResourcePatternResolver,
 * able to find matching resources below the web application root directory
 * via Servlet 2.3's <code>ServletContext.getResourcePaths</code>.
 * Falls back to the superclass' file system checking for other resources.
 *
 * <p>The advantage of using <code>ServletContext.getResourcePaths</code> to
 * find matching files is that it will work in a WAR file which has not been
 * expanded too. For Servlet containers that do not support Servlet 2.3 or
 * above, this resolver will always fall back to file system checking,
 * which requires an expanded WAR file.
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 */
public class ServletContextResourcePatternResolver extends PathMatchingResourcePatternResolver {

	/**
	 * Create a new ServletContextResourcePatternResolver.
	 * @param servletContext the ServletContext to load resources with
	 * @see ServletContextResourceLoader( ServletContext)
	 */
	public ServletContextResourcePatternResolver(ServletContext servletContext) {
		super(new ServletContextResourceLoader(servletContext));
	}

	/**
	 * Create a new ServletContextResourcePatternResolver.
	 * @param resourceLoader the ResourceLoader to load root directories and
	 * actual resources with
	 */
	public ServletContextResourcePatternResolver(ResourceLoader resourceLoader) {
		super(resourceLoader);
	}

	/**
	 * Overridden version which checks for ServletContextResource
	 * and uses <code>ServletContext.getResourcePaths</code> to find
	 * matching resources below the web application root directory.
	 * In case of other resources, delegates to the superclass version.
	 * @see #doRetrieveMatchingServletContextResources
	 * @see ServletContextResource
	 * @see ServletContext#getResourcePaths
	 */
	protected Set doFindPathMatchingFileResources(Resource rootDirResource, String subPattern) throws IOException {
		if (rootDirResource instanceof ServletContextResource) {
			ServletContextResource scResource = (ServletContextResource) rootDirResource;
			ServletContext sc = scResource.getServletContext();
			if (sc.getMajorVersion() > 2 || (sc.getMajorVersion() == 2 && sc.getMinorVersion() > 2)) {
				// Only try the following on Servlet containers >= 2.3:
				// ServletContext.getResourcePaths is not available before that version.
				String fullPattern = scResource.getPath() + subPattern;
				Set result = new HashSet();
				doRetrieveMatchingServletContextResources(sc, fullPattern, scResource.getPath(), result);
				return result;
			}
		}
		return super.doFindPathMatchingFileResources(rootDirResource, subPattern);
	}

	/**
	 * Recursively retrieve ServletContextResources that match the given pattern,
	 * adding them to the given result set.
	 * @param servletContext the ServletContext to work on
	 * @param fullPattern the pattern to match against,
	 * with preprended root directory path
	 * @param dir the current directory
	 * @param result the Set of matching Resources to add to
	 * @throws IOException if directory contents could not be retrieved
	 * @see ServletContextResource
	 * @see ServletContext#getResourcePaths
	 */
	protected void doRetrieveMatchingServletContextResources(
			ServletContext servletContext, String fullPattern, String dir, Set result) throws IOException {

		Set candidates = servletContext.getResourcePaths(dir);
		if (candidates != null) {
			boolean dirDepthNotFixed = (fullPattern.indexOf("**") != -1);
			for (Iterator it = candidates.iterator(); it.hasNext();) {
				String currPath = (String) it.next();
				if (currPath.endsWith("/") &&
						(dirDepthNotFixed ||
						StringUtils.countOccurrencesOf(currPath, "/") < StringUtils.countOccurrencesOf(fullPattern, "/"))) {
					doRetrieveMatchingServletContextResources(servletContext, fullPattern, currPath, result);
				}
				if (getPathMatcher().match(fullPattern, currPath)) {
					result.add(new ServletContextResource(servletContext, currPath));
				}
			}
		}
	}

}
