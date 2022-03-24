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

package org.springframework.orm.hibernate3;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.LockMode;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of Hibernate operations.
 * Implemented by HibernateTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides HibernateTemplate's data access methods that mirror
 * various Session methods. See the Hibernate Session javadocs
 * for details on those methods.
 *
 * <p>Note that operations that return an Iterator (i.e. <code>iterate</code>)
 * are supposed to be used within Spring-driven or JTA-driven transactions
 * (with HibernateTransactionManager, JtaTransactionManager, or EJB CMT).
 * Else, the Iterator won't be able to read results from its ResultSet anymore,
 * as the underlying Hibernate Session will already have been closed.
 *
 * <p>Lazy loading will also just work with an open Hibernate Session,
 * either within a transaction or within OpenSessionInViewFilter/Interceptor.
 * Furthermore, some operations just make sense within transactions,
 * for example: <code>contains</code>, <code>evict</code>, <code>lock</code>,
 * <code>flush</code>, <code>clear</code>.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see HibernateTemplate
 * @see org.hibernate.Session
 * @see #iterate
 * @see HibernateTransactionManager
 * @see org.springframework.transaction.jta.JtaTransactionManager
 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewFilter
 * @see org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor
 */
public interface HibernateOperations {

	/**
	 * Execute the action specified by the given action object within a Session.
	 * Application exceptions thrown by the action object get propagated to the
	 * caller (can only be unchecked). Hibernate exceptions are transformed into
	 * appropriate DAO ones. Allows for returning a result object, i.e. a domain
	 * object or a collection of domain objects.
	 * <p>Note: Callback code is not supposed to handle transactions itself!
	 * Use an appropriate transaction manager like HibernateTransactionManager.
	 * Generally, callback code must not touch any Session lifecycle methods,
	 * like close, disconnect, or reconnect, to let the template do its work.
	 * @param action callback object that specifies the Hibernate action
	 * @return a result object returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 * @see HibernateTransactionManager
	 * @see org.springframework.dao
	 * @see org.springframework.transaction
	 * @see org.hibernate.Session
	 */
	Object execute(HibernateCallback action) throws DataAccessException;

	/**
	 * Execute the specified action assuming that the result object is a List.
	 * This is a convenience method for executing Hibernate find calls or
	 * queries within an action.
	 * @param action calback object that specifies the Hibernate action
	 * @return a List result returned by the action, or null
	 * @throws DataAccessException in case of Hibernate errors
	 */
	List executeFind(HibernateCallback action) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for loading individual objects
	//-------------------------------------------------------------------------

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, or null if not found.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @return the persistent instance, or null if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#get(Class, Serializable)
	 */
	Object get(Class entityClass, Serializable id) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, or null if not found.
	 * Obtains the specified lock mode if the instance exists.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @param lockMode the lock mode to obtain
	 * @return the persistent instance, or null if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#get(Class, Serializable, org.hibernate.LockMode)
	 */
	Object get(Class entityClass, Serializable id, LockMode lockMode)
			throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, or null if not found.
	 * @param entityName the name of a persistent entity
	 * @param id an identifier of the persistent instance
	 * @return the persistent instance, or null if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#get(Class, Serializable)
	 */
	Object get(String entityName, Serializable id) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, or null if not found.
	 * Obtains the specified lock mode if the instance exists.
	 * @param entityName the name of a persistent entity
	 * @param id an identifier of the persistent instance
	 * @param lockMode the lock mode to obtain
	 * @return the persistent instance, or null if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#get(Class, Serializable, org.hibernate.LockMode)
	 */
	Object get(String entityName, Serializable id, LockMode lockMode)
			throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, throwing an exception if not found.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @return the persistent instance
	 * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#load(Class, Serializable)
	 */
	Object load(Class entityClass, Serializable id) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, throwing an exception if not found.
	 * Obtains the specified lock mode if the instance exists.
	 * @param entityClass a persistent class
	 * @param id an identifier of the persistent instance
	 * @param lockMode the lock mode to obtain
	 * @return the persistent instance
	 * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#load(Class, Serializable)
	 */
	Object load(Class entityClass, Serializable id, LockMode lockMode)
			throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, throwing an exception if not found.
	 * @param entityName the name of a persistent entity
	 * @param id an identifier of the persistent instance
	 * @return the persistent instance
	 * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#load(Class, Serializable)
	 */
	Object load(String entityName, Serializable id) throws DataAccessException;

	/**
	 * Return the persistent instance of the given entity class
	 * with the given identifier, throwing an exception if not found.
	 * Obtains the specified lock mode if the instance exists.
	 * @param entityName the name of a persistent entity
	 * @param id an identifier of the persistent instance
	 * @param lockMode the lock mode to obtain
	 * @return the persistent instance
	 * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#load(Class, Serializable)
	 */
	Object load(String entityName, Serializable id, LockMode lockMode)
			throws DataAccessException;

	/**
	 * Return all persistent instances of the given entity class.
	 * Note: Use queries or criteria for retrieving a specific subset. 
	 * @param entityClass a persistent class
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException if there is a Hibernate error
	 * @see org.hibernate.Session#createCriteria
	 */
	List loadAll(Class entityClass) throws DataAccessException;

	/**
	 * Load the persistent instance with the given identifier
	 * into the given object, throwing an exception if not found.
	 * @param entity the object (of the target class) to load into
	 * @param id an identifier of the persistent instance
	 * @throws org.springframework.orm.ObjectRetrievalFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#load(Object, Serializable)
	 */
	void load(Object entity, Serializable id) throws DataAccessException;

	/**
	 * Re-read the state of the given persistent instance.
	 * @param entity the persistent instance to re-read
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#refresh(Object)
	 */
	void refresh(Object entity) throws DataAccessException;

	/**
	 * Re-read the state of the given persistent instance.
	 * Obtains the specified lock mode for the instance.
	 * @param entity the persistent instance to re-read
	 * @param lockMode the lock mode to obtain
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#refresh(Object, org.hibernate.LockMode)
	 */
	void refresh(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Check whether the given object is in the Session cache.
	 * @param entity the persistence instance to check
	 * @return whether the given object is in the Session cache
	 * @throws DataAccessException if there is a Hibernate error
	 * @see org.hibernate.Session#contains
	 */
	boolean contains(Object entity) throws DataAccessException;

	/**
	 * Remove the given object from the Session cache.
	 * @param entity the persistent instance to evict
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#evict
	 */
	void evict(Object entity) throws DataAccessException;

	/**
	 * Force initialization of a Hibernate proxy or persistent collection.
	 * @param proxy a proxy for a persistent object or a persistent collection
	 * @throws DataAccessException if we can't initialize the proxy, for example
	 * because it is not associated with an active Session
	 * @see org.hibernate.Hibernate#initialize
	 */
	void initialize(Object proxy) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience methods for storing individual objects
	//-------------------------------------------------------------------------

	/**
	 * Obtain the specified lock level upon the given object, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entity the persistent instance to lock
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#lock(Object, org.hibernate.LockMode)
	 */
	void lock(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Obtain the specified lock level upon the given object, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entityName the name of a persistent entity
	 * @param entity the persistent instance to lock
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#lock(Object, org.hibernate.LockMode)
	 */
	void lock(String entityName, Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Persist the given transient instance.
	 * @param entity the transient instance to persist
	 * @return the generated identifier
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#save(Object)
	 */
	Serializable save(Object entity) throws DataAccessException;

	/**
	 * Persist the given transient instance with the given identifier.
	 * @param entity the transient instance to persist
	 * @param id the identifier to assign
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#save(Object, Serializable)
	 */
	void save(Object entity, Serializable id) throws DataAccessException;

	/**
	 * Persist the given transient instance.
	 * @param entityName the name of a persistent entity
	 * @param entity the transient instance to persist
	 * @return the generated identifier
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#save(Object)
	 */
	Serializable save(String entityName, Object entity) throws DataAccessException;

	/**
	 * Persist the given transient instance with the given identifier.
	 * @param entityName the name of a persistent entity
	 * @param entity the transient instance to persist
	 * @param id the identifier to assign
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#save(Object, Serializable)
	 */
	void save(String entityName, Object entity, Serializable id) throws DataAccessException;

	/**
	 * Update the given persistent instance,
	 * associating it with the current Hibernate Session.
	 * @param entity the persistent instance to update
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#update(Object)
	 */
	void update(Object entity) throws DataAccessException;

	/**
	 * Update the given persistent instance,
	 * associating it with the current Hibernate Session.
	 * <p>Obtains the specified lock mode if the instance exists, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entity the persistent instance to update
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#update(Object)
	 */
	void update(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Update the given persistent instance,
	 * associating it with the current Hibernate Session.
	 * @param entityName the name of a persistent entity
	 * @param entity the persistent instance to update
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#update(Object)
	 */
	void update(String entityName, Object entity) throws DataAccessException;

	/**
	 * Update the given persistent instance,
	 * associating it with the current Hibernate Session.
	 * <p>Obtains the specified lock mode if the instance exists, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entityName the name of a persistent entity
	 * @param entity the persistent instance to update
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#update(Object)
	 */
	void update(String entityName, Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Save or update the given persistent instance,
	 * according to its id (matching the configured "unsaved-value"?).
	 * Associates the instance with the current Hibernate Session.
	 * @param entity the persistent instance to save or update
	 * (to be associated with the Hibernate Session)
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#saveOrUpdate(Object)
	 */
	void saveOrUpdate(Object entity) throws DataAccessException;

	/**
	 * Save or update the given persistent instance,
	 * according to its id (matching the configured "unsaved-value"?).
	 * Associates the instance with the current Hibernate Session.
	 * @param entityName the name of a persistent entity
	 * @param entity the persistent instance to save or update
	 * (to be associated with the Hibernate Session)
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#saveOrUpdate(Object)
	 */
	void saveOrUpdate(String entityName, Object entity) throws DataAccessException;

	/**
	 * Save or update all given persistent instances,
	 * according to its id (matching the configured "unsaved-value"?).
	 * Associates the instances with the current Hibernate Session.
	 * @param entities the persistent instances to save or update
	 * (to be associated with the Hibernate Session)
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#saveOrUpdate(Object)
	 */
	void saveOrUpdateAll(Collection entities) throws DataAccessException;

	/**
	 * Persist the given transient instance. Follows JSR-220 semantics.
	 * <p>Similar to <code>save</code>, associating the given object
	 * with the current Hibernate Session.
	 * @param entity the persistent instance to persist
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#persist(Object)
	 * @see #save
	 */
	void persist(Object entity) throws DataAccessException;

	/**
	 * Persist the given transient instance. Follows JSR-220 semantics.
	 * <p>Similar to <code>save</code>, associating the given object
	 * with the current Hibernate Session.
	 * @param entityName the name of a persistent entity
	 * @param entity the persistent instance to persist
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#persist(Object)
	 * @see #save
	 */
	void persist(String entityName, Object entity) throws DataAccessException;

	/**
	 * Copy the state of the given object onto the persistent object
	 * with the same identifier. Follows JSR-220 semantics.
	 * <p>Similar to <code>saveOrUpdate</code>, but never associates the given
	 * object with the current Hibernate Session. In case of a new entity,
	 * the state will be copied over as well.
	 * <p>Note that <code>merge</code> will <i>not</i> update the identifiers in
	 * the passed-in object graph (in contrast to TopLink)! Consider registering
	 * Spring's IdTransferringMergeEventListener if you'd like to have newly
	 * assigned ids transferred to the original object graph too.
	 * @param entity the object to merge with the corresponding persistence instance
	 * @return the updated, registered persistent instance
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#merge(Object)
	 * @see #saveOrUpdate
	 * @see org.springframework.orm.hibernate3.support.IdTransferringMergeEventListener
	 */
	Object merge(Object entity) throws DataAccessException;

	/**
	 * Copy the state of the given object onto the persistent object
	 * with the same identifier. Follows JSR-220 semantics.
	 * <p>Similar to <code>saveOrUpdate</code>, but never associates the given
	 * object with the current Hibernate Session. In case of a new entity,
	 * the state will be copied over as well.
	 * <p>Note that <code>merge</code> will <i>not</i> update the identifiers in
	 * the passed-in object graph (in contrast to TopLink)! Consider registering
	 * Spring's IdTransferringMergeEventListener if you'd like to have newly
	 * assigned ids transferred to the original object graph too.
	 * @param entityName the name of a persistent entity
	 * @param entity the object to merge with the corresponding persistence instance
	 * @return the updated, registered persistent instance
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#merge(Object)
	 * @see #saveOrUpdate
	 */
	Object merge(String entityName, Object entity) throws DataAccessException;

	/**
	 * Delete the given persistent instance.
	 * @param entity the persistent instance to delete
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#delete(Object)
	 */
	void delete(Object entity) throws DataAccessException;

	/**
	 * Delete the given persistent instance.
	 * <p>Obtains the specified lock mode if the instance exists, implicitly
	 * checking whether the corresponding database entry still exists
	 * (throwing an OptimisticLockingFailureException if not found).
	 * @param entity the persistent instance to delete
	 * @param lockMode the lock mode to obtain
	 * @throws org.springframework.orm.ObjectOptimisticLockingFailureException if not found
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#delete(Object)
	 */
	void delete(Object entity, LockMode lockMode) throws DataAccessException;

	/**
	 * Delete all given persistent instances.
	 * <p>This can be combined with any of the find methods to delete by query
	 * in two lines of code.
	 * @param entities the persistent instances to delete
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#delete(Object)
	 */
	void deleteAll(Collection entities) throws DataAccessException;

	/**
	 * Flush all pending saves, updates and deletes to the database.
	 * <p>Only invoke this for selective eager flushing, for example when JDBC code
	 * needs to see certain changes within the same transaction. Else, it's preferable
	 * to rely on auto-flushing at transaction completion.
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#flush
	 */
	void flush() throws DataAccessException;

	/**
	 * Remove all objects from the Session cache, and cancel all pending saves,
	 * updates and deletes.
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#clear
	 */
	void clear() throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience finder methods for HQL strings
	//-------------------------------------------------------------------------

	/**
	 * Execute a query for persistent instances.
	 * @param queryString a query expressed in Hibernate's query language
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#createQuery
	 */
	List find(String queryString) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#createQuery
	 */
	List find(String queryString, Object value) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#createQuery
	 */
	List find(String queryString, Object[] values) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding
	 * one value to a ":" named parameter in the query string.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramName the name of parameter
	 * @param value the value of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedParam(String queryName, String paramName, Object value)
			throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a
	 * number of values to ":" named parameters in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param paramNames the names of the parameters
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedParam(String queryString, String[] paramNames, Object[] values)
			throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding the properties
	 * of the given bean to <i>named</i> parameters in the query string.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param valueBean the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Query#setProperties
	 * @see org.hibernate.Session#createQuery
	 */
	List findByValueBean(String queryString, Object valueBean) throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience finder methods for named queries
	//-------------------------------------------------------------------------

	/**
	 * Execute a named query for persistent instances.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName) throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a "?" parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName, Object value) throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to "?" parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQuery(String queryName, Object[] values) throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding
	 * one value to a ":" named parameter in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramName the name of parameter
	 * @param value the value of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndNamedParam(String queryName, String paramName, Object value)
			throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding a
	 * number of values to ":" named parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param paramNames the names of the parameters
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndNamedParam(String queryName, String[] paramNames, Object[] values)
			throws DataAccessException;

	/**
	 * Execute a named query for persistent instances, binding the properties
	 * of the given bean to ":" named parameters in the query string.
	 * A named query is defined in a Hibernate mapping file.
	 * @param queryName the name of a Hibernate query in a mapping file
	 * @param valueBean the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Query#setProperties
	 * @see org.hibernate.Session#getNamedQuery(String)
	 */
	List findByNamedQueryAndValueBean(String queryName, Object valueBean)
			throws DataAccessException;


	//-------------------------------------------------------------------------
	// Convenience query methods for iteratation
	//-------------------------------------------------------------------------

	/**
	 * Execute a query for persistent instances.
	 * <p>Returns the results as Iterator. Entities returned are initialized
	 * on demand. See Hibernate docs for details.
	 * @param queryString a query expressed in Hibernate's query language
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#createQuery
	 */
	Iterator iterate(String queryString) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding one value
	 * to a "?" parameter in the query string.
	 * <p>Returns the results as Iterator. Entities returned are initialized
	 * on demand. See Hibernate docs for details.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param value the value of the parameter
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#createQuery
	 */
	Iterator iterate(String queryString, Object value) throws DataAccessException;

	/**
	 * Execute a query for persistent instances, binding a number of
	 * values to "?" parameters in the query string.
	 * <p>Returns the results as Iterator. Entities returned are initialized
	 * on demand. See Hibernate docs for details.
	 * @param queryString a query expressed in Hibernate's query language
	 * @param values the values of the parameters
	 * @return a List containing 0 or more persistent instances
	 * @throws DataAccessException in case of Hibernate errors
	 * @see org.hibernate.Session#createQuery
	 */
	Iterator iterate(String queryString, Object[] values) throws DataAccessException;

	/**
	 * Close an Iterator created by <i>iterate</i> operations immediately,
	 * instead of waiting until the session is closed or disconnected.
	 * @param it the Iterator to close
	 * @throws DataAccessException if the Iterator could not be closed
	 * @see org.hibernate.Hibernate#close
	 */
	void closeIterator(Iterator it) throws DataAccessException;

}
