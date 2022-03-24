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

package org.springframework.dao.support;

import java.util.Collection;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.util.NumberUtils;

/**
 * Miscellaneous utility methods for DAO implementations.
 * Useful with any data access technology.
 * @author Juergen Hoeller
 * @since 1.0.2
 */
public abstract class DataAccessUtils {

	/**
	 * Return a unique result object from the given Collection.
	 * Returns null if 0 result objects found; throws an exception
	 * if more than 1 found.
	 * @param results the result Collection
	 * @return the unique result object, or null if none
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * result object has been found in the given Collection
	 */
	public static Object uniqueResult(Collection results) throws IncorrectResultSizeDataAccessException {
		int size = results.size();
		if (size == 0) {
			return null;
		}
		if (size > 1) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}

	/**
	 * Return a unique result object from the given Collection.
	 * Throws an exception if 0 or more than 1 result objects found.
	 * @param results the result Collection
	 * @return the unique result object
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * result object or none at all has been found in the given Collection
	 */
	public static Object requiredUniqueResult(Collection results) throws IncorrectResultSizeDataAccessException {
		int size = results.size();
		if (size != 1) {
			throw new IncorrectResultSizeDataAccessException(1, size);
		}
		return results.iterator().next();
	}

	/**
	 * Return a unique result object from the given Collection.
	 * Throws an exception if 0 or more than 1 result objects found,
	 * of if the unique result object is not convertable to the
	 * specified required type.
	 * @param results the result Collection
	 * @return the unique result object
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * result object or none at all has been found in the given Collection
	 * @throws TypeMismatchDataAccessException if the unique object does
	 * not match the specified required type
	 */
	public static Object objectResult(Collection results, Class requiredType)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

		Object result = requiredUniqueResult(results);
		if (requiredType != null && !requiredType.isInstance(result)) {
			if (String.class.equals(requiredType)) {
				result = result.toString();
			}
			else if (Number.class.isAssignableFrom(requiredType) && Number.class.isInstance(result)) {
				try {
					result = NumberUtils.convertNumberToTargetClass(((Number) result), requiredType);
				}
				catch (IllegalArgumentException ex) {
					throw new TypeMismatchDataAccessException(ex.getMessage());
				}
			}
			else {
				throw new TypeMismatchDataAccessException(
						"Result object is of type [" + result.getClass().getName() +
						"] and could not be converted to required type [" + requiredType.getName() + "]");
			}
		}
		return result;
	}

	/**
	 * Return a unique int result from the given Collection.
	 * Throws an exception if 0 or more than 1 result objects found,
	 * of if the unique result object is not convertable to an int.
	 * @param results the result Collection
	 * @return the unique int result
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * result object or none at all has been found in the given Collection
	 * @throws TypeMismatchDataAccessException if the unique object
	 * in the collection is not convertable to an int
	 */
	public static int intResult(Collection results)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {
		return ((Number) objectResult(results, Number.class)).intValue();
	}

	/**
	 * Return a unique long result from the given Collection.
	 * Throws an exception if 0 or more than 1 result objects found,
	 * of if the unique result object is not convertable to a long.
	 * @param results the result Collection
	 * @return the unique long result
	 * @throws IncorrectResultSizeDataAccessException if more than one
	 * result object or none at all has been found in the given Collection
	 * @throws TypeMismatchDataAccessException if the unique object
	 * in the collection is not convertable to a long
	 */
	public static long longResult(Collection results)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {
		return ((Number) objectResult(results, Number.class)).longValue();
	}

}
