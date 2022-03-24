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

package org.springframework.jmx.access;

import org.springframework.core.NestedRuntimeException;

/**
 * Thrown when trying to invoke an operation on a proxy that is not exposed
 * by the proxied resource's management interface.
 * @author Juergen Hoeller
 * @since 1.2
 * @see MBeanClientInterceptor
 */
public class InvocationFailureException extends NestedRuntimeException {

	/**
	 * Create a new <code>InvocationFailureException</code> with the supplied
	 * error message.
	 * @param msg the error message
	 */
	public InvocationFailureException(String msg) {
		super(msg);
	}

	/**
	 * Create a new <code>InvocationFailureException</code> with the
	 * specified error message and root cause.
	 * @param msg the error message
	 * @param ex the root cause
	 */
	public InvocationFailureException(String msg, Throwable ex) {
		super(msg, ex);
	}

}
