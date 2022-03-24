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

package org.springframework.jms.support.converter;

import java.io.ByteArrayOutputStream;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

/**
 * A subclass of SimpleMessageConverter that uses the JMS 1.0.2 specification,
 * rather than the JMS 1.1 methods used by SimpleMessageConverter itself.
 * This class can be used for JMS 1.0.2 providers, offering the same functionality
 * as SimpleMessageConverter does for JMS 1.1 providers.
 *
 * <p>The only difference to the default SimpleMessageConverter is that BytesMessage
 * is handled differently: namely, without using the <code>getBodyLength()</code>
 * method which has been introduced in JMS 1.1 and is therefore not available on a
 * JMS 1.0.2 provider.
 *
 * @author Juergen Hoeller
 * @since 1.1.1
 */
public class SimpleMessageConverter102 extends SimpleMessageConverter {

	public static final int BUFFER_SIZE = 4096;

	protected byte[] extractByteArrayFromMessage(BytesMessage message) throws JMSException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
		byte[] buffer = new byte[BUFFER_SIZE];
		int bufferCount = -1;
		while ((bufferCount = message.readBytes(buffer)) >= 0) {
			baos.write(buffer, 0, bufferCount);
			if (bufferCount < BUFFER_SIZE) {
				break;
			}
		}
		return baos.toByteArray();
	}

}
