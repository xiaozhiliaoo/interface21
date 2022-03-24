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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.remoting.rmi.CodebaseAwareObjectInputStream;
import org.springframework.remoting.support.RemoteInvocation;
import org.springframework.remoting.support.RemoteInvocationResult;

/**
 * Abstract base implementation of the HttpInvokerRequestExecutor interface.
 *
 * <p>Pre-implements serialization of RemoteInvocation objects and
 * deserialization of RemoteInvocationResults objects.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see #doExecuteRequest
 */
public abstract class AbstractHttpInvokerRequestExecutor implements HttpInvokerRequestExecutor {

	private static final int SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE = 1024;

	protected static final String CONTENT_TYPE_SERIALIZED_OBJECT = "application/x-java-serialized-object";

	protected static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";

	protected static final String HTTP_HEADER_CONTENT_LENGTH = "Content-Length";

	protected static final String HTTP_METHOD_POST = "POST";


	protected final Log logger = LogFactory.getLog(getClass());


	public final RemoteInvocationResult executeRequest(
			HttpInvokerClientConfiguration config, RemoteInvocation invocation)
			throws IOException, ClassNotFoundException {

		ByteArrayOutputStream baos = getByteArrayOutputStream(invocation);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending HTTP invoker request for service at [" + config.getServiceUrl() +
					"], with size " + baos.size());
		}
		return doExecuteRequest(config, baos);
	}

	/**
	 * Serialize the given RemoteInvocation into a ByteArrayOutputStream.
	 * @param invocation the RemoteInvocation object
	 * @return a ByteArrayOutputStream with the serialized RemoteInvocation
	 * @throws IOException if thrown by I/O methods
	 */
	protected ByteArrayOutputStream getByteArrayOutputStream(RemoteInvocation invocation) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(SERIALIZED_INVOCATION_BYTE_ARRAY_INITIAL_SIZE);
		writeRemoteInvocation(invocation, baos);
		return baos;
	}

	/**
	 * Serialize the given RemoteInvocation to the given OutputStream.
	 * <p>The default implementation gives <code>decorateOutputStream</code> a chance
	 * to decorate the stream first (for example, for custom encryption or compression).
	 * Creates an <code>ObjectOutputStream</code> for the final stream and calls
	 * <code>doWriteRemoteInvocation</code> to actually write the object.
	 * <p>Can be overridden for custom serialization of the invocation.
	 * @param invocation the RemoteInvocation object
	 * @param os the OutputStream to write to
	 * @throws IOException if thrown by I/O methods
	 * @see #decorateOutputStream
	 * @see #doWriteRemoteInvocation
	 */
	protected void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(decorateOutputStream(os));
		try {
			doWriteRemoteInvocation(invocation, oos);
			oos.flush();
		}
		finally {
			oos.close();
		}
	}

	/**
	 * Return the OutputStream to use for writing remote invocations,
	 * potentially decorating the given original OutputStream.
	 * <p>The default implementation returns the given stream as-is.
	 * Can be overridden, for example, for custom encryption or compression.
	 * @param os the original OutputStream
	 * @return the potentially decorated OutputStream
	 */
	protected OutputStream decorateOutputStream(OutputStream os) throws IOException {
		return os;
	}

	/**
	 * Perform the actual writing of the given invocation object to the
	 * given ObjectOutputStream.
	 * <p>The default implementation simply calls <code>writeObject</code>.
	 * Can be overridden for serialization of a custom wrapper object rather
	 * than the plain invocation, for example an encryption-aware holder.
	 * @param invocation the RemoteInvocation object
	 * @param oos the ObjectOutputStream to write to
	 * @throws IOException if thrown by I/O methods
	 * @see ObjectOutputStream#writeObject
	 */
	protected void doWriteRemoteInvocation(RemoteInvocation invocation, ObjectOutputStream oos) throws IOException {
		oos.writeObject(invocation);
	}


	/**
	 * Execute a request to send the given serialized remote invocation.
	 * <p>Implementations will usually call <code>readRemoteInvocationResult</code>
	 * to deserialize a returned RemoteInvocationResult object.
	 * @param config the HTTP invoker configuration that specifies the
	 * target service
	 * @param baos the ByteArrayOutputStream that contains the serialized
	 * RemoteInvocation object
	 * @return the RemoteInvocationResult object
	 * @throws IOException if thrown by I/O operations
	 * @throws ClassNotFoundException if thrown during deserialization
	 * @see #readRemoteInvocationResult(InputStream, String)
	 */
	protected abstract RemoteInvocationResult doExecuteRequest(
			HttpInvokerClientConfiguration config, ByteArrayOutputStream baos)
			throws IOException, ClassNotFoundException;

	/**
	 * Deserialize a RemoteInvocationResult object from the given InputStream.
	 * <p>Gives <code>decorateInputStream</code> a chance to decorate the stream
	 * first (for example, for custom encryption or compression). Creates an
	 * <code>ObjectInputStream</code> via <code>createObjectInputStream</code> and
	 * calls <code>doReadRemoteInvocationResult</code> to actually read the object.
	 * <p>Can be overridden for custom serialization of the invocation.
	 * @param is the InputStream to read from
	 * @param codebaseUrl the codebase URL to load classes from if not found locally
	 * @return the RemoteInvocationResult object
	 * @throws IOException if thrown by I/O methods
	 * @throws ClassNotFoundException if thrown during deserialization
	 * @see #decorateInputStream
	 * @see #createObjectInputStream
	 * @see #doReadRemoteInvocationResult
	 */
	protected RemoteInvocationResult readRemoteInvocationResult(InputStream is, String codebaseUrl)
			throws IOException, ClassNotFoundException {

		ObjectInputStream ois = createObjectInputStream(decorateInputStream(is), codebaseUrl);
		try {
			return doReadRemoteInvocationResult(ois);
		}
		finally {
			ois.close();
		}
	}

	/**
	 * Return the InputStream to use for reading remote invocation results,
	 * potentially decorating the given original InputStream.
	 * <p>The default implementation returns the given stream as-is.
	 * Can be overridden, for example, for custom encryption or compression.
	 * @param is the original InputStream
	 * @return the potentially decorated InputStream
	 */
	protected InputStream decorateInputStream(InputStream is) throws IOException {
		return is;
	}

	/**
	 * Create an ObjectInputStream for the given InputStream and codebase.
	 * The default implementation creates a CodebaseAwareObjectInputStream.
	 * <p>Spring's CodebaseAwareObjectInputStream is not only used for loading
	 * from a specified codebase, but also to explicitly resolve primitive
	 * class names. This is done by the standard ObjectInputStream
	 * on JDK 1.4+, but needs to be done explicitly on JDK 1.3.
	 * @param is the InputStream to read from
	 * @param codebaseUrl the codebase URL to load classes from if not found locally
	 * (can be null)
	 * @return the new ObjectInputStream instance to use
	 * @throws IOException if creation of the ObjectInputStream failed
	 * @see CodebaseAwareObjectInputStream
	 */
	protected ObjectInputStream createObjectInputStream(InputStream is, String codebaseUrl) throws IOException {
		return new CodebaseAwareObjectInputStream(is, codebaseUrl);
	}

	/**
	 * Perform the actual reading of an invocation object from the
	 * given ObjectInputStream.
	 * <p>The default implementation simply calls <code>readObject</code>.
	 * Can be overridden for deserialization of a custom wrapper object rather
	 * than the plain invocation, for example an encryption-aware holder.
	 * @param ois the ObjectInputStream to read from
	 * @return the RemoteInvocationResult object
	 * @throws IOException if thrown by I/O methods
	 * @see ObjectOutputStream#writeObject
	 */
	protected RemoteInvocationResult doReadRemoteInvocationResult(ObjectInputStream ois)
			throws IOException, ClassNotFoundException {

		Object obj = ois.readObject();
		if (!(obj instanceof RemoteInvocationResult)) {
			throw new IOException("Deserialized object needs to be assignable to type [" +
					RemoteInvocationResult.class.getName() + "]: " + obj);
		}
		return (RemoteInvocationResult) obj;
	}

}
