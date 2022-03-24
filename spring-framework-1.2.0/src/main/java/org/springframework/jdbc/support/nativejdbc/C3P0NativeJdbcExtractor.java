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

package org.springframework.jdbc.support.nativejdbc;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import com.mchange.v2.c3p0.C3P0ProxyConnection;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Implementation of the NativeJdbcExtractor interface for the C3P0 connection pool.
 * Returns underlying native Connections to application code instead of C3P0's
 * wrapper implementations; unwraps the Connection for native Statements.
 * The returned JDBC classes can then safely be cast, e.g. to OracleResultSet.
 *
 * <p>This NativeJdbcExtractor can be set just to <i>allow</i> working with
 * a C3P0 DataSource: If a given object is not a C3P0 wrapper, it will be
 * returned as-is.
 *
 * <p>Note that this class requires C3P0 0.8.5 or later; for earlier C3P0 versions,
 * use SimpleNativeJdbcExtractor (which won't work for C3P0 0.8.5 or later).
 *
 * @author Juergen Hoeller
 * @since 1.1.5
 * @see com.mchange.v2.c3p0.C3P0ProxyConnection#rawConnectionOperation
 * @see SimpleNativeJdbcExtractor
 */
public class C3P0NativeJdbcExtractor extends NativeJdbcExtractorAdapter {

	private final Method getRawConnectionMethod;

	public C3P0NativeJdbcExtractor() {
		try {
			this.getRawConnectionMethod = getClass().getMethod("getRawConnection", new Class[] {Connection.class});
		}
		catch (NoSuchMethodException ex) {
			throw new IllegalStateException("Internal error in C3P0NativeJdbcExtractor: " + ex.getMessage());
		}
	}

	public boolean isNativeConnectionNecessaryForNativeStatements() {
		return true;
	}

	public boolean isNativeConnectionNecessaryForNativePreparedStatements() {
		return true;
	}

	public boolean isNativeConnectionNecessaryForNativeCallableStatements() {
		return true;
	}

	/**
	 * Retrieve the Connection via C3P0's <code>rawConnectionOperation</code> API,
	 * using the <code>getRawConnection</code> as callback to get access to the
	 * raw Connection (which is otherwise not directly supported by C3P0).
	 * @see #getRawConnection
	 */
	protected Connection doGetNativeConnection(Connection con) throws SQLException {
		if (con instanceof C3P0ProxyConnection) {
			C3P0ProxyConnection cpCon = (C3P0ProxyConnection) con;
			try {
				return (Connection) cpCon.rawConnectionOperation(
						this.getRawConnectionMethod, null, new Object[] {C3P0ProxyConnection.RAW_CONNECTION});
			}
			catch (Exception ex) {
				throw new DataAccessResourceFailureException("Could not retrieve C3P0's raw connection", ex);
			}
		}
		return con;
	}

	/**
	 * This method is not meant to be used directly; it rather serves
	 * as callback method for C3P0's "rawConnectionOperation" API.
	 * @param con a native Connection handle
	 * @return the native Connection handle, as-is
	 */
	public static Connection getRawConnection(Connection con) {
		return con;
	}

}
