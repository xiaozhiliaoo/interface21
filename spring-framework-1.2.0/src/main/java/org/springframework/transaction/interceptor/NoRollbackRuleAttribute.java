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
 * Tag subclass of RollbackRule. Its class name means that it has the
 * opposite behavior to the RollbackRule superclass.
 * @author Rod Johnson
 * @since 09-Apr-2003
 */
public class NoRollbackRuleAttribute extends RollbackRuleAttribute {
	
	/**
	 * Construct a new NoRollbackRule for the given throwable class.
	 * @param clazz throwable class
	 */
	public NoRollbackRuleAttribute(Class clazz) {
		super(clazz);
	}

	/**
	 * Construct a new NoRollbackRule for the given exception name.
	 * This can be a substring, with no wildcard support at present.
	 * A value of "ServletException" would match ServletException and
	 * subclasses, for example.
	 * @param exceptionName the exception pattern
	 */
	public NoRollbackRuleAttribute(String exceptionName) {
		super(exceptionName);
	}
	
	public String toString() {
		return "No" + super.toString();
	}
	
	// rely on superclass equals and hashCode methods
}
