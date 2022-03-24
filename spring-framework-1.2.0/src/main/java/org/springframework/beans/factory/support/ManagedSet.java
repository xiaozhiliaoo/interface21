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

package org.springframework.beans.factory.support;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.CollectionFactory;

/**
 * Tag collection class used to hold managed Set values,
 * which may include runtime bean references.
 *
 * <p>Wraps a target Set, which will be a linked set if possible
 * (that is, if running on JDK 1.4 or if Commons Collections 3.x is available).
 *
 * @author Juergen Hoeller
 * @since 21.01.2004
 * @see CollectionFactory#createLinkedSetIfPossible
 */
public class ManagedSet implements Set {

	private final Set targetSet;

	public ManagedSet() {
		this(16);
	}

	public ManagedSet(int initialCapacity) {
		this.targetSet = CollectionFactory.createLinkedSetIfPossible(initialCapacity);
	}

	public ManagedSet(Set targetSet) {
		this.targetSet = targetSet;
	}

	public int size() {
		return this.targetSet.size();
	}

	public boolean isEmpty() {
		return this.targetSet.isEmpty();
	}

	public boolean contains(Object obj) {
		return this.targetSet.contains(obj);
	}

	public Iterator iterator() {
		return this.targetSet.iterator();
	}

	public Object[] toArray() {
		return this.targetSet.toArray();
	}

	public Object[] toArray(Object[] arr) {
		return this.targetSet.toArray(arr);
	}

	public boolean add(Object obj) {
		return this.targetSet.add(obj);
	}

	public boolean remove(Object obj) {
		return this.targetSet.remove(obj);
	}

	public boolean containsAll(Collection coll) {
		return this.targetSet.containsAll(coll);
	}

	public boolean addAll(Collection coll) {
		return this.targetSet.addAll(coll);
	}

	public boolean retainAll(Collection coll) {
		return this.targetSet.retainAll(coll);
	}

	public boolean removeAll(Collection coll) {
		return this.targetSet.removeAll(coll);
	}

	public void clear() {
		this.targetSet.clear();
	}

	public int hashCode() {
		return this.targetSet.hashCode();
	}

	public boolean equals(Object obj) {
		return this.targetSet.equals(obj);
	}

	public String toString() {
		return this.targetSet.toString();
	}

}
