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

package org.springframework.orm.ibatis;

import java.util.List;
import java.util.Map;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.event.RowHandler;

import org.springframework.dao.DataAccessException;

/**
 * Interface that specifies a basic set of iBATIS SqlMapClient operations.
 * Implemented by SqlMapClientTemplate. Not often used, but a useful option
 * to enhance testability, as it can easily be mocked or stubbed.
 *
 * <p>Provides SqlMapClientTemplate's convenience methods that mirror SqlMapExecutor's
 * execution methods. See the SqlMapExecutor javadocs for details on those methods.
 *
 * <p>NOTE: The SqlMapClient/SqlMapSession API is the API of iBATIS SQL Maps 2.
 * With SQL Maps 1.x, the SqlMap/MappedStatement API has to be used.
 *
 * @author Juergen Hoeller
 * @since 24.02.2004
 * @see SqlMapClientTemplate
 * @see com.ibatis.sqlmap.client.SqlMapClient
 * @see com.ibatis.sqlmap.client.SqlMapSession
 * @see com.ibatis.sqlmap.client.SqlMapExecutor
 */
public interface SqlMapClientOperations {

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForObject(String, Object)
	 * @throws DataAccessException in case of errors
	 */
	Object queryForObject(String statementName, Object parameterObject)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForObject(String, Object, Object)
	 * @throws DataAccessException in case of errors
	 */
	Object queryForObject(String statementName, Object parameterObject,	Object resultObject)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForList(String, Object)
	 * @throws DataAccessException in case of errors
	 */
	List queryForList(String statementName, Object parameterObject)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForList(String, Object, int, int)
	 * @throws DataAccessException in case of errors
	 */
	List queryForList(String statementName, Object parameterObject, int skipResults, int maxResults)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryWithRowHandler(String, Object, RowHandler)
	 * @throws DataAccessException in case of errors
	 */
	void queryWithRowHandler(String statementName, Object parameterObject, RowHandler rowHandler)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForPaginatedList(String, Object, int)
	 * @throws DataAccessException in case of errors
	 */
	PaginatedList queryForPaginatedList(String statementName, Object parameterObject, int pageSize)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForMap(String, Object, String)
	 * @throws DataAccessException in case of errors
	 */
	Map queryForMap(String statementName, Object parameterObject, String keyProperty)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#queryForMap(String, Object, String, String)
	 * @throws DataAccessException in case of errors
	 */
	Map queryForMap(String statementName, Object parameterObject, String keyProperty, String valueProperty)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#insert(String, Object)
	 * @throws DataAccessException in case of errors
	 */
	Object insert(String statementName, Object parameterObject)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#update(String, Object)
	 * @throws DataAccessException in case of errors
	 */
	int update(String statementName, Object parameterObject)
			throws DataAccessException;

	/**
	 * @see com.ibatis.sqlmap.client.SqlMapExecutor#delete(String, Object)
	 * @throws DataAccessException in case of errors
	 */
	int delete(String statementName, Object parameterObject)
			throws DataAccessException;

	/**
	 * Convenience method provided by Spring: execute an update operation
	 * with an automatic check that the update affected the given required
	 * number of rows.
	 * @param statementName the name of the mapped statement
	 * @param parameterObject the parameter object
	 * @param requiredRowsAffected the number of rows that the update is
	 * required to affect
	 * @throws DataAccessException in case of errors
	 */
	void update(String statementName, Object parameterObject, int requiredRowsAffected)
			throws DataAccessException;

	/**
	 * Convenience method provided by Spring: execute a delete operation
	 * with an automatic check that the delete affected the given required
	 * number of rows.
	 * @param statementName the name of the mapped statement
	 * @param parameterObject the parameter object
	 * @param requiredRowsAffected the number of rows that the delete is
	 * required to affect
	 * @throws DataAccessException in case of errors
	 */
	void delete(String statementName, Object parameterObject, int requiredRowsAffected)
			throws DataAccessException;

}
