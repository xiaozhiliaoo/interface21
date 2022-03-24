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

package org.springframework.web.multipart;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for multipart resolution strategies that handle file uploads as
 * defined in <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>.
 * Implementations are typically usable both within any application context
 * and standalone.
 *
 * <p>There are two concrete implementations included in Spring:
 * <ul>
 * <li>CommonsMultipartResolver for Jakarta Commons FileUpload
 * <li>CosMultipartResolver for Jason Hunter's COS (com.oreilly.servlet)
 * </ul>
 *
 * <p>There is no default resolver implementation used for Spring DispatcherServlets,
 * as an application might choose to parse its multipart requests itself. To define
 * an implementation, create a bean with the id "multipartResolver" in a
 * DispatcherServlet's application context. Such a resolver gets applied to all
 * requests handled by that DispatcherServlet.
 *
 * <p>If a DispatcherServlet detects a multipart request, it will resolve it
 * via the configured MultipartResolver and pass on a wrapped HttpServletRequest.
 * Controllers can then cast their given request to the MultipartHttpServletRequest
 * interface, being able to access MultipartFiles. Note that this cast is only
 * supported in case of an actual multipart request.
 *
 * <pre>
 * ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
 *   MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
 *   MultipartFile multipartFile = multipartRequest.getFile("image");
 *   ...
 * }</pre>
 *
 * Instead of direct access, command or form controllers can register a
 * ByteArrayMultipartFileEditor or StringMultipartFileEditor with their data
 * binder, to automatically apply multipart content to command bean properties.
 *
 * <p>As an alternative to using a MultipartResolver with a DispatcherServlet,
 * a MultipartFilter can be registered in web.xml. It will delegate to a
 * corresponding MultipartResolver bean in the root application context.
 * This is mainly intended for applications that do not use Spring's own
 * web MVC framework.
 *
 * <p>Note: There is hardly ever a need to access the MultipartResolver itself
 * from application code. It will simply do its work behind the scenes,
 * making MultipartHttpServletRequests available to controllers.
 *
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 * @since 29.09.2003
 * @see MultipartHttpServletRequest
 * @see MultipartFile
 * @see org.springframework.web.multipart.commons.CommonsMultipartResolver
 * @see org.springframework.web.multipart.cos.CosMultipartResolver
 * @see org.springframework.web.multipart.support.ByteArrayMultipartFileEditor
 * @see org.springframework.web.multipart.support.StringMultipartFileEditor
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.servlet.support.RequestContextUtils#getMultipartResolver
 */
public interface MultipartResolver {

	/**
	 * Determine if the request contains multipart content.
	 * <p>Will typically check for content type "multipart/form-data", but the actually
	 * accepted requests might depend on the capabilities of the resolver implementation.
	 * @param request the servlet request to be evaluated
	 * @return whether the request contains multipart content
	 */
	boolean isMultipart(HttpServletRequest request);

	/**
	 * Parse the given HTTP request into multipart files and parameters,
	 * and wrap the request inside a MultipartHttpServletRequest object
	 * that provides access to file descriptors and makes contained
	 * parameters accessible via the standard ServletRequest methods.
	 * @param request the servlet request to wrap (must be of a multipart content type)
	 * @return the wrapped servlet request
	 * @throws MultipartException if the servlet request is not multipart, or if
	 * implementation-specific problems are encountered (such as exceeding file size limits)
	 * @see MultipartHttpServletRequest#getFile
	 * @see MultipartHttpServletRequest#getFileNames
	 * @see MultipartHttpServletRequest#getFileMap
	 * @see HttpServletRequest#getParameter
	 * @see HttpServletRequest#getParameterNames
	 * @see HttpServletRequest#getParameterMap
	 */
	MultipartHttpServletRequest resolveMultipart(HttpServletRequest request) throws MultipartException;

	/**
	 * Cleanup any resources used for the multipart handling,
	 * like a storage for the uploaded files.
	 * @param request the request to cleanup resources for
	 */
	void cleanupMultipart(MultipartHttpServletRequest request);

}
