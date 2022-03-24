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

package org.springframework.orm.jdo;

import java.util.Collection;
import java.util.Map;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of JDO operations.
 * Implemented by JdoTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides JdoTemplate's data access methods that mirror various
 * PersistenceManager methods. See the JDO PersistenceManager javadocs
 * for details on those methods.
 *
 * <p>Note that lazy loading will just work with an open JDO PersistenceManager,
 * either within a Spring-driven transaction (with JdoTransactionManager or
 * JtaTransactionManager) or within OpenPersistenceManagerInViewFilter/Interceptor.
 * Furthermore, some operations just make sense within transactions,
 * for example: <code>evict</code>, <code>evictAll</code>, <code>flush</code>.
 *
 * <p>Updated to expose JDO 2.0 API functionality, as of Spring 1.2:
 * <code>detachCopy</code>, <code>attachCopy</code>, <code>findByNamedQuery</code>, etc.
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see JdoTemplate
 * @see javax.jdo.PersistenceManager
 * @see JdoTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewFilter
 * @see org.springframework.orm.jdo.support.OpenPersistenceManagerInViewInterceptor
 */
public interface JdoOperations {

	/**
	 * Execute the action specified by the given action object within a
	 * PersistenceManager. Application exceptions thrown by the action object
	 * get propagated to the caller (can only be unchecked). JDO exceptions
	 * are transformed into appropriate DAO ones. Allows for returning a
	 * result object, i.e. a domain object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like JdoTransactionManager.
	 * @param action callback object that specifies the JDO action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of JDO errors
	 * @see JdoTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 * @see javax.jdo.PersistenceManager
	 */
	Object execute(JdoCallback action) throws DataAccessException;

	/**
	 * Execute the specified action assuming that the result object is a
	 * Collection. This is a convenience method for executing JDO queries
	 * within an action.
	 * @param action callback object that specifies the JDO action
	 * @return a Collection result returned by the action, or null
	 * @throws DataAccessException in case of JDO errors
	 */
	Collection executeFind(JdoCallback action) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for load, save, delete
	//-------------------------------------------------------------------------

	/**
	 * Return the persistent instance with the given JDO object id,
	 * throwing an exception if not found.
	 * <p>A JDO object id identifies both the persistent class and the id
	 * within the namespace of that class.
	 * @param objectId a JDO object id of the persistent instance
	 * @return the persistent instance
	 * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#getObjectById(Object, boolean)
	 */
	Object getObjectById(Object objectId) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given id value, throwing an exception if not found.
	 * <p>The given id value is typically just unique within the namespace
	 * of the persistent class. Its toString value must correspond to the
	 * toString value of the corresponding JDO object id.
	 * <p>Usually, the passed-in value will have originated from the primary
	 * key field of a persistent object that uses JDO's application identity.
	 * @param entityClass a persistent class
	 * @param idValue an id value of the persistent instance
	 * @return the persistent instance
	 * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newObjectIdInstance(Class, String)
	 * @see javax.jdo.PersistenceManager#getObjectById(Object, boolean)
	 * @see javax.jdo.PersistenceManager#getObjectById(Class, Object)
	 */
	Object getObjectById(Class entityClass, Object idValue) throws DataAccessException;

	/**
	 * Remove the given object from the PersistenceManager cache.
	 * @param entity the persistent instance to evict
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#evict(Object)
	 */
	void evict(Object entity) throws DataAccessException;

	/**
	 * Remove all given objects from the PersistenceManager cache.
	 * @param entities the persistent instances to evict
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#evictAll(Collection)
	 */
	void evictAll(Collection entities) throws DataAccessException;

	/**
	 * Remove all objects from the PersistenceManager cache.
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#evictAll()
	 */
	void evictAll() throws DataAccessException;

	/**
	 * Re-read the state of the given persistent instance.
	 * @param entity the persistent instance to re-read
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#refresh(Object)
	 */
	void refresh(Object entity) throws DataAccessException;

	/**
	 * Re-read the state of all given persistent instances.
	 * @param entities the persistent instances to re-read
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#refreshAll(Collection)
	 */
	void refreshAll(Collection entities) throws DataAccessException;

	/**
	 * Re-read the state of all persistent instances.
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#refreshAll()
	 */
	void refreshAll() throws DataAccessException;

	/**
	 * Make the given transient instance persistent.
	 * @param entity the transient instance to make persistent
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#makePersistent(Object)
	 */
	void makePersistent(Object entity) throws DataAccessException;

	/**
	 * Make the given transient instances persistent.
	 * @param entities the transient instances to make persistent
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#makePersistentAll(Collection)
	 */
	void makePersistentAll(Collection entities) throws DataAccessException;

	/**
	 * Delete the given persistent instance.
	 * @param entity the persistent instance to delete
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#deletePersistent(Object)
	 */
	void deletePersistent(Object entity) throws DataAccessException;

	/**
	 * Delete all given persistent instances.
	 * <p>This can be combined with any of the find methods to delete by query
	 * in two lines of code.
	 * @param entities the persistent instances to delete
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#deletePersistentAll(Collection)
	 */
	void deletePersistentAll(Collection entities) throws DataAccessException;

	/**
	 * Detach a copy of the given persistent instance from the current JDO transaction,
	 * for use outside a JDO transaction (for example, as web form object).
	 * <p>Only available on JDO 2.0 and higher.
	 * @param entity the persistent instance to detach
	 * @see javax.jdo.PersistenceManager#detachCopy(Object)
	 */
	Object detachCopy(Object entity);

	/**
	 * Detach copies of the given persistent instances from the current JDO transaction,
	 * for use outside a JDO transaction (for example, as web form objects).
	 * <p>Only available on JDO 2.0 and higher.
	 * @param entities the persistent instances to detach
	 * @see javax.jdo.PersistenceManager#detachCopyAll(Collection)
	 */
	Collection detachCopyAll(Collection entities);

	/**
	 * Reattach the given detached instance (for example, a web form object) with
	 * the current JDO transaction, merging its changes into the current persistence
	 * instance that represents the corresponding entity.
	 * <p>Only available on JDO 2.0 and higher.
	 * @param detachedEntity the detached instance to attach
	 * @return the corresponding persistent instance
	 * @see javax.jdo.PersistenceManager#attachCopy(Object, boolean)
	 */
	Object attachCopy(Object detachedEntity);

	/**
	 * Reattach the given detached instances (for example, web form objects) with
	 * the current JDO transaction, merging their changes into the current persistence
	 * instances that represent the corresponding entities.
	 * <p>Only available on JDO 2.0 and higher.
	 * @param detachedEntities the detached instances to reattach
	 * @return the corresponding persistent instances
	 * @see javax.jdo.PersistenceManager#attachCopyAll(Collection, boolean)
	 */
	Collection attachCopyAll(Collection detachedEntities);

	/**
	 * Flush all transactional modifications to the database.
	 * <p>Only invoke this for selective eager flushing, for example when JDBC code
	 * needs to see certain changes within the same transaction. Else, it's preferable
	 * to rely on auto-flushing at transaction completion.
	 * <p>Only available on JDO 2.0+ or through a vendor-specific JdoDialect.
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#flush()
	 * @see JdoDialect#flush(javax.jdo.PersistenceManager)
	 */
	void flush() throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience finder methods
	//-------------------------------------------------------------------------

	/**
	 * Find all persistent instances of the given class.
	 * @param entityClass a persistent class
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class)
	 */
	Collection find(Class entityClass) throws DataAccessException;

	/**
	 * Find all persistent instances of the given class that match the given
	 * JDOQL filter.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match (or null if none)
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 */
	Collection find(Class entityClass, String filter) throws DataAccessException;

	/**
	 * Find all persistent instances of the given class that match the given
	 * JDOQL filter, with the given result ordering.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match (or null if none)
	 * @param ordering the ordering of the result (or null if none)
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#setOrdering
	 */
	Collection find(Class entityClass, String filter, String ordering) throws DataAccessException;

	/**
	 * Find all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values the corresponding parameter values
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithArray
	 */
	Collection find(Class entityClass, String filter, String parameters, Object[] values)
			throws DataAccessException;

	/**
	 * Find all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values,
	 * with the given result ordering.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values the corresponding parameter values
	 * @param ordering the ordering of the result (or null if none)
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithArray
	 * @see javax.jdo.Query#setOrdering
	 */
	Collection find(Class entityClass, String filter, String parameters, Object[] values, String ordering)
			throws DataAccessException;

	/**
	 * Find all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values a Map with parameter names as keys and parameter values
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithMap
	 */
	Collection find(Class entityClass, String filter, String parameters, Map values)
			throws DataAccessException;

	/**
	 * Find all persistent instances of the given class that match the given
	 * JDOQL filter, using the given parameter declarations and parameter values,
	 * with the given result ordering.
	 * @param entityClass a persistent class
	 * @param filter the JDOQL filter to match
	 * @param parameters the JDOQL parameter declarations
	 * @param values a Map with parameter names as keys and parameter values
	 * @param ordering the ordering of the result (or null if none)
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(Class, String)
	 * @see javax.jdo.Query#declareParameters
	 * @see javax.jdo.Query#executeWithMap
	 * @see javax.jdo.Query#setOrdering
	 */
	Collection find(Class entityClass, String filter, String parameters, Map values, String ordering)
			throws DataAccessException;

	/**
	 * Find persistent instances through the given query object
	 * in the specified query language.
	 * <p>Only available on JDO 2.0 and higher.
	 * @param language the query language (<code>javax.jdo.Query#JDOQL</code>
	 * or <code>javax.jdo.Query#SQL</code>, for example)
	 * @param queryObject the query object for the specified language
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(String, Object)
	 * @see javax.jdo.Query#JDOQL
	 * @see javax.jdo.Query#SQL
	 */
	Collection find(String language, Object queryObject) throws DataAccessException;

	/**
	 * Find persistent instances through the given single-string JDOQL query.
	 * <p>Only available on JDO 2.0 and higher.
	 * @param queryString the single-string JDOQL query
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newQuery(String)
	 */
	Collection find(String queryString) throws DataAccessException;

	/**
	 * Find persistent instances through the given named query.
	 * <p>Only available on JDO 2.0 and higher.
	 * @param entityClass a persistent class
	 * @param queryName the name of the query
	 * @return the persistent instances
	 * @throws DataAccessException in case of JDO errors
	 * @see javax.jdo.PersistenceManager#newNamedQuery(Class, String)
	 */
	Collection findByNamedQuery(Class entityClass, String queryName) throws DataAccessException;

}
