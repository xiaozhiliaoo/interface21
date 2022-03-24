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

package org.springframework.aop.target;

import java.io.Serializable;

import org.springframework.aop.TargetSource;

/**
 * Canonical TargetSource when there's no target,
 * and behavior is supplied by interfaces and advisors.
 * @author Rod Johnson
 */
public class EmptyTargetSource implements TargetSource, Serializable {
	
	public static final EmptyTargetSource INSTANCE = new EmptyTargetSource();
	
	/**
	 * Enforce Singleton.
	 */
	private EmptyTargetSource() {
	} 
	
	public Class getTargetClass() {
		return null;
	}

	public boolean isStatic() {
		return true;
	}

	public Object getTarget() {
		return null;
	}

	public void releaseTarget(Object target) {
	}
	
	/**
	 * Required to support serialization.
	 * Replaces with canonical instance on deserialization,
	 * protecting the Singleton pattern.
	 * Alternative to overriding <code>equals</code>.
	 */
	private Object readResolve() {
		return INSTANCE;
	}
	
	public String toString() {
		return "EmptyTargetSource: no target";
	}

}
