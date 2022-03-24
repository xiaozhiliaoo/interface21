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

package org.springframework.transaction.interceptor;

/**
 * TransactionAttribute implementation that delegates all calls to a given target
 * TransactionAttribute. Abstract because it is meant to be to be subclasses,
 * overriding specific methods that should not simply delegate to the target.
 * @author Juergen Hoeller
 * @since 1.2
 */
public abstract class DelegatingTransactionAttribute implements TransactionAttribute {

	private final TransactionAttribute targetAttribute;

	/**
	 * Create a DelegatingTransactionAttribute for the given target attribute.
	 */
	public DelegatingTransactionAttribute(TransactionAttribute targetAttribute) {
		this.targetAttribute = targetAttribute;
	}

	public int getPropagationBehavior() {
		return this.targetAttribute.getPropagationBehavior();
	}

	public int getIsolationLevel() {
		return this.targetAttribute.getIsolationLevel();
	}

	public int getTimeout() {
		return this.targetAttribute.getTimeout();
	}

	public boolean isReadOnly() {
		return this.targetAttribute.isReadOnly();
	}

	public String getName() {
		return this.targetAttribute.getName();
	}

	public boolean rollbackOn(Throwable ex) {
		return this.targetAttribute.rollbackOn(ex);
	}

	public boolean equals(Object obj) {
		return this.targetAttribute.equals(obj);
	}

	public int hashCode() {
		return this.targetAttribute.hashCode();
	}

	public String toString() {
		return this.targetAttribute.toString();
	}

}
