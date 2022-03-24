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

package org.springframework.remoting.httpinvoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationBasedExporter;
import org.springframework.remoting.support.RemoteInvocationResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Web controller that exports the specified service bean as HTTP invoker
 * service endpoint, accessible via an HTTP invoker proxy.
 *
 * <p>Deserializes remote invocation objects and serializes remote invocation
 * result objects. Uses Java serialization just like RMI, but provides the
 * same ease of setup as Caucho's HTTP-based Hessian and Burlap protocols.
 *
 * <p><b>HTTP invoker is the recommended protocol for Java-to-Java remoting.</b>
 * It is more powerful and more extensible than Hessian and Burlap, at the
 * expense of being tied to Java. Nevertheless, it is as easy to set up as
 * Hessian and Burlap, which is its main advantage compared to RMI.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see HttpInvokerClientInterceptor
 * @see HttpInvokerProxyFactoryBean
 * @see org.springframework.remoting.rmi.RmiServiceExporter
 * @see org.springframework.remoting.caucho.HessianServiceExporter
 * @see org.springframework.remoting.caucho.BurlapServiceExporter
 */
public class HttpInvokerServiceExporter extends RemoteInvocationBasedExporter
		implements Controller, InitializingBean {

	protected static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

	private Object proxy;

	public void afterPropertiesSet() {
		this.proxy = getProxyForService();
	}


	/**
	 * Read a remote invocation from the request, execute it,
	 * and write the remote invocation result to the response.
	 * @see #readRemoteInvocation(HttpServletRequest)
	 * @see #invokeAndCreateResult
	 * @see #writeRemoteInvocationResult(HttpServletRequest, HttpServletResponse, RemoteInvocationResult)
	 */
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ClassNotFoundException {

		RemoteInvocation invocation = readRemoteInvocation(request);
		RemoteInvocationResult result = invokeAndCreateResult(invocation, this.proxy);
		writeRemoteInvocationResult(request, response, result);
		return null;
	}


	/**
	 * Read a RemoteInvocation from the given HTTP request.
	 * Delegates to <code>readRemoteInvocation(InputStream)</code>
	 * with the servlet request's input stream.
	 * @param request current HTTP request
	 * @return the RemoteInvocation object
	 * @throws IOException if thrown by operations on the request
	 * @throws ClassNotFoundException if thrown by deserialization
	 * @see #readRemoteInvocation(HttpServletRequest, InputStream)
	 * @see javax.servlet.ServletRequest#getInputStream
	 */
	protected RemoteInvocation readRemoteInvocation(HttpServletRequest request)
			throws IOException, ClassNotFoundException {

		return readRemoteInvocation(request, request.getInputStream());
	}

	/**
	 * Deserialize a RemoteInvocation object from the given InputStream.
	 * <p>Gives <code>decorateInputStream</code> a chance to decorate the stream
	 * first (for example, for custom encryption or compression). Creates a
	 * <code>CodebaseAwareObjectInputStream</code> and calls
	 * <code>doReadRemoteInvocation</code> to actually read the object.
	 * <p>Can be overridden for custom serialization of the invocation.
	 * @param request current HTTP request
	 * @param is the InputStream to read from
	 * @return the RemoteInvocation object
	 * @throws IOException if thrown by I/O methods
	 * @throws ClassNotFoundException if thrown during deserialization
	 * @see #decorateInputStream
	 * @see #doReadRemoteInvocation
	 */
	protected RemoteInvocation readRemoteInvocation(HttpServletRequest request, InputStream is)
			throws IOException, ClassNotFoundException {

		ObjectInputStream ois = createObjectInputStream(decorateInputStream(request, is));
		try {
			return doReadRemoteInvocation(ois);
		}
		finally {
			ois.close();
		}
	}

	/**
	 * Return the InputStream to use for reading remote invocations,
	 * potentially decorating the given original InputStream.
	 * <p>The default implementation returns the given stream as-is.
	 * Can be overridden, for example, for custom encryption or compression.
	 * @param request current HTTP request
	 * @param is the original InputStream
	 * @return the potentially decorated InputStream
	 */
	protected InputStream decorateInputStream(HttpServletRequest request, InputStream is) throws IOException {
		return is;
	}

	/**
	 * Create an ObjectInputStream for the given InputStream.
	 * The default implementation creates a CodebaseAwareObjectInputStream.
	 * <p>Spring's CodebaseAwareObjectInputStream is used to explicitly resolve
	 * primitive class names. This is done by the standard ObjectInputStream
	 * on JDK 1.4+, but needs to be done explicitly on JDK 1.3.
	 * @param is the InputStream to read from
	 * @return the new ObjectInputStream instance to use
	 * @throws IOException if creation of the ObjectInputStream failed
	 * @see CodebaseAwareObjectInputStream
	 */
	protected ObjectInputStream createObjectInputStream(InputStream is) throws IOException {
		return new CodebaseAwareObjectInputStream(is, null);
	}

	/**
	 * Perform the actual reading of an invocation result object from the
	 * given ObjectInputStream.
	 * <p>The default implementation simply calls <code>readObject</code>.
	 * Can be overridden for deserialization of a custom wrapper object rather
	 * than the plain invocation, for example an encryption-aware holder.
	 * @param ois the ObjectInputStream to read from
	 * @return the RemoteInvocationResult object
	 * @throws IOException if thrown by I/O methods
	 * @see ObjectOutputStream#writeObject
	 */
	protected RemoteInvocation doReadRemoteInvocation(ObjectInputStream ois)
			throws IOException, ClassNotFoundException {

		Object obj = ois.readObject();
		if (!(obj instanceof RemoteInvocation)) {
			throw new IOException("Deserialized object needs to be assignable to type [" +
					RemoteInvocation.class.getName() + "]: " + obj);
		}
		return (RemoteInvocation) obj;
	}


	/**
	 * Write the given RemoteInvocationResult to the given HTTP response.
	 * <p>Note that a request argument was introduced for Spring 1.1.3, which
	 * is not backwards-compatible for subclasses that override this method.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param result the RemoteInvocationResult object
	 * @throws IOException if thrown by operations on the response
	 */
	protected void writeRemoteInvocationResult(
			HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result)
			throws IOException {

		response.setContentType(CONTENT_TYPE_SERIALIZED_OBJECT);
		writeRemoteInvocationResult(request, response, result, response.getOutputStream());
	}

	/**
	 * Serialize the given RemoteInvocation to the given OutputStream.
	 * <p>The default implementation gives <code>decorateOutputStream</code> a chance
	 * to decorate the stream first (for example, for custom encryption or compression).
	 * Creates an <code>ObjectOutputStream</code> for the final stream and calls
	 * <code>doWriteRemoteInvocationResult</code> to actually write the object.
	 * <p>Can be overridden for custom serialization of the invocation.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param result the RemoteInvocationResult object
	 * @param os the OutputStream to write to
	 * @throws IOException if thrown by I/O methods
	 * @see #decorateOutputStream
	 * @see #doWriteRemoteInvocationResult
	 */
	protected void writeRemoteInvocationResult(
			HttpServletRequest request, HttpServletResponse response, RemoteInvocationResult result, OutputStream os)
			throws IOException {

		ObjectOutputStream oos = new ObjectOutputStream(decorateOutputStream(request, response, os));
		try {
			doWriteRemoteInvocationResult(result, oos);
			oos.flush();
		}
		finally {
			oos.close();
		}
	}

	/**
	 * Return the OutputStream to use for writing remote invocation results,
	 * potentially decorating the given original OutputStream.
	 * <p>The default implementation returns the given stream as-is.
	 * Can be overridden, for example, for custom encryption or compression.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param os the original OutputStream
	 * @return the potentially decorated OutputStream
	 */
	protected OutputStream decorateOutputStream(
			HttpServletRequest request, HttpServletResponse response, OutputStream os) throws IOException {

		return os;
	}

	/**
	 * Perform the actual writing of the given invocation result object to the
	 * given ObjectOutputStream.
	 * <p>The default implementation simply calls <code>writeObject</code>.
	 * Can be overridden for serialization of a custom wrapper object rather
	 * than the plain invocation, for example an encryption-aware holder.
	 * @param result the RemoteInvocationResult object
	 * @param oos the ObjectOutputStream to write to
	 * @throws IOException if thrown by I/O methods
	 * @see ObjectOutputStream#writeObject
	 */
	protected void doWriteRemoteInvocationResult(RemoteInvocationResult result, ObjectOutputStream oos)
			throws IOException {

		oos.writeObject(result);
	}

}
