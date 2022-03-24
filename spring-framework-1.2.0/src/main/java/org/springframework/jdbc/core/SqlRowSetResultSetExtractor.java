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

package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;

import com.sun.rowset.CachedRowSetImpl;

import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * ResultSetExtractor implementation that returns a Spring SqlRowSet
 * representation for each given ResultSet.
 *
 * <p>The default implementation uses a standard JDBC CachedRowSet underneath.
 * This means that JDBC RowSet support needs to be available at runtime:
 * by default, Sun's <code>com.sun.rowset.CachedRowSetImpl</code> class is
 * used, which is part of JDK 1.5+ and also available separately as part of
 * Sun's JDBC RowSet Implementations download.
 *
 * @author Juergen Hoeller
 * @since 1.2
 * @see #newCachedRowSet
 * @see SqlRowSet
 * @see JdbcTemplate#queryForRowSet(String)
 * @see CachedRowSet
 */
public class SqlRowSetResultSetExtractor implements ResultSetExtractor {

	public Object extractData(ResultSet rs) throws SQLException {
		return createSqlRowSet(rs);
	}

	/**
	 * Create a SqlRowSet that wraps the given ResultSet,
	 * representing its data in a disconnected fashion.
	 * <p>This implementation creates a Spring ResultSetWrappingSqlRowSet
	 * instance that wraps a standard JDBC CachedRowSet instance.
	 * Can be overridden to use a different implementation.
	 * @param rs the original ResultSet (connected)
	 * @return the disconnected SqlRowSet
	 * @throws SQLException if thrown by JDBC methods
	 * @see #newCachedRowSet
	 * @see ResultSetWrappingSqlRowSet
	 */
	protected SqlRowSet createSqlRowSet(ResultSet rs) throws SQLException {
		CachedRowSet rowSet = newCachedRowSet();
		rowSet.populate(rs);
		return new ResultSetWrappingSqlRowSet(rowSet);
	}

	/**
	 * Create a new CachedRowSet instance, to be populated by
	 * the <code>createSqlRowSet</code> implementation.
	 * <p>The default implementation creates a new instance of
	 * Sun's <code>com.sun.rowset.CachedRowSetImpl</code> class,
	 * which is part of JDK 1.5+ and also available separately
	 * as part of Sun's JDBC RowSet Implementations download.
	 * @return a new CachedRowSet instance
	 * @throws SQLException if thrown by JDBC methods
	 * @see #createSqlRowSet
	 * @see CachedRowSetImpl
	 */
	protected CachedRowSet newCachedRowSet() throws SQLException {
		return new CachedRowSetImpl();
	}

}
