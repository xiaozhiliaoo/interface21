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

package org.springframework.jdbc.support.rowset;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import org.springframework.jdbc.InvalidResultSetAccessException;

/**
 * Default implementation of Spring's SqlRowSet interface.
 *
 * <p>This implementation wraps a <code>javax.sql.ResultSet</code>,
 * catching any SQLExceptions and translating them to the
 * appropriate Spring DataAccessException.
 *
 * <p>The passed-in ResultSets should already be disconnected if the
 * SqlRowSet is supposed to be usable in a disconnected fashion.
 * This means that you will usually pass in a
 * <code>javax.sql.rowset.CachedRowSet</code>,
 * which implements the ResultSet interface.
 *
 * @author Thomas Risberg
 * @author Juergen Hoeller
 * @since 1.2
 * @see ResultSet
 * @see javax.sql.rowset.CachedRowSet
 */
public class ResultSetWrappingSqlRowSet implements SqlRowSet {

	private final ResultSet resultSet;

	private final SqlRowSetMetaData rowSetMetaData;


	/**
	 * Create a new ResultSetWrappingSqlRowSet for the given ResultSet.
	 * @param resultSet a disconnected ResultSet to wrap
	 * (usually a <code>javax.sql.rowset.CachedRowSet</code>)
	 * @throws InvalidResultSetAccessException if extracting
	 * the ResultSetMetaData failed
	 * @see javax.sql.rowset.CachedRowSet
	 * @see ResultSet#getMetaData
	 * @see ResultSetWrappingSqlRowSetMetaData
	 */
	public ResultSetWrappingSqlRowSet(ResultSet resultSet) throws InvalidResultSetAccessException {
		this.resultSet = resultSet;
		try {
			this.rowSetMetaData = new ResultSetWrappingSqlRowSetMetaData(resultSet.getMetaData());
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * Return the underlying ResultSet
	 * (usually a <code>javax.sql.rowset.CachedRowSet</code>).
	 * @see javax.sql.rowset.CachedRowSet
	 */
	public ResultSet getResultSet() {
		return this.resultSet;
	}

	/**
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	public SqlRowSetMetaData getMetaData() {
		return this.rowSetMetaData;
	}
	
	/**
	 * @see ResultSet#findColumn(String)
	 */
	public int findColumn(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.findColumn(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}


	// RowSet methods for extracting data values

	/**
	 * @see ResultSet#getBigDecimal(int)
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getBigDecimal(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getBigDecimal(String)
	 */
	public BigDecimal getBigDecimal(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getBigDecimal(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getBoolean(int)
	 */
	public boolean getBoolean(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getBoolean(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getBoolean(String)
	 */
	public boolean getBoolean(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getBoolean(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getByte(int)
	 */
	public byte getByte(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getByte(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getByte(String)
	 */
	public byte getByte(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getByte(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getDate(int, Calendar)
	 */
	public Date getDate(int columnIndex, Calendar cal) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDate(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getDate(int)
	 */
	public Date getDate(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDate(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	/**
	 * @see ResultSet#getDate(String, Calendar)
	 */
	public Date getDate(String columnName, Calendar cal) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDate(columnName, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getDate(String)
	 */
	public Date getDate(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDate(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getDouble(int)
	 */
	public double getDouble(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDouble(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getDouble(String)
	 */
	public double getDouble(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getDouble(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getFloat(int)
	 */
	public float getFloat(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getFloat(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getFloat(String)
	 */
	public float getFloat(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getFloat(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	/**
	 * @see ResultSet#getInt(int)
	 */
	public int getInt(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getInt(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getInt(String)
	 */
	public int getInt(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getInt(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getLong(int)
	 */
	public long getLong(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getLong(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getLong(String)
	 */
	public long getLong(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getLong(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getObject(int, Map)
	 */
	public Object getObject(int i, Map map) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getObject(i, map);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getObject(int)
	 */
	public Object getObject(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getObject(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getObject(String, Map)
	 */
	public Object getObject(String columnName, Map map) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getObject(columnName, map);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getObject(String)
	 */
	public Object getObject(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getObject(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getShort(int)
	 */
	public short getShort(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getShort(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getShort(String)
	 */
	public short getShort(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getShort(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getString(int)
	 */
	public String getString(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getString(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getString(String)
	 */
	public String getString(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getString(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getTime(int, Calendar)
	 */
	public Time getTime(int columnIndex, Calendar cal) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTime(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getTime(int)
	 */
	public Time getTime(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTime(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getTime(String, Calendar)
	 */
	public Time getTime(String columnName, Calendar cal) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTime(columnName, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getTime(String)
	 */
	public Time getTime(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTime(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getTimestamp(int, Calendar)
	 */
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
			throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTimestamp(columnIndex, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#getTimestamp(int)
	 */
	public Timestamp getTimestamp(int columnIndex) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTimestamp(columnIndex);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getTimestamp(String, Calendar)
	 */
	public Timestamp getTimestamp(String columnName, Calendar cal)
			throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTimestamp(columnName, cal);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getTimestamp(String)
	 */
	public Timestamp getTimestamp(String columnName) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getTimestamp(columnName);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}


	// RowSet navigation methods
	
	/**
	 * @see ResultSet#absolute(int)
	 */
	public boolean absolute(int row) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.absolute(row);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#afterLast()
	 */
	public void afterLast() throws InvalidResultSetAccessException {
		try {
			this.resultSet.afterLast();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#beforeFirst()
	 */
	public void beforeFirst() throws InvalidResultSetAccessException {
		try {
			this.resultSet.beforeFirst();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#first()
	 */
	public boolean first() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.first();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#getRow()
	 */
	public int getRow() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.getRow();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#isAfterLast()
	 */
	public boolean isAfterLast() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isAfterLast();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

	/**
	 * @see ResultSet#isBeforeFirst()
	 */
	public boolean isBeforeFirst() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isBeforeFirst();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#isFirst()
	 */
	public boolean isFirst() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isFirst();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#isLast()
	 */
	public boolean isLast() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.isLast();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#last()
	 */
	public boolean last() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.last();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#next()
	 */
	public boolean next() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.next();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#previous()
	 */
	public boolean previous() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.previous();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#relative(int)
	 */
	public boolean relative(int rows) throws InvalidResultSetAccessException {
		try {
			return this.resultSet.relative(rows);
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}
	
	/**
	 * @see ResultSet#wasNull()
	 */
	public boolean wasNull() throws InvalidResultSetAccessException {
		try {
			return this.resultSet.wasNull();
		}
		catch (SQLException se) {
			throw new InvalidResultSetAccessException(se);
		}
	}

}
