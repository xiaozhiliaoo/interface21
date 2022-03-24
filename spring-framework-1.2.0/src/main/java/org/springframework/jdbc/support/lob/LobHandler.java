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

package org.springframework.jdbc.support.lob;

import java.io.InputStream;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstraction for handling large binary fields and large text fields in
 * specific databases, no matter if represented as simple types or Large OBjects.
 * Its main purpose is to isolate Oracle's peculiar handling of LOBs in
 * OracleLobHandler; most other databases should work with DefaultLobHandler.
 *
 * <p>Provides accessor methods for BLOBs and CLOBs, and acts as factory for
 * LobCreator instances, to be used as sessions for creating BLOBs or CLOBs.
 * LobCreators are typically instantiated for each statement execution or for
 * each transaction. They are not thread-safe because they might track
 * allocated database resources to be able to free them after execution.
 *
 * <p>Most databases/drivers should be able to work with DefaultLobHandler,
 * which simply delegates to JDBC's direct accessor methods, avoiding
 * <code>java.sql.Blob</code> and <code>java.sql.Clob</code> completely.
 *
 * <p>Unfortunately, Oracle 9i just accepts Blob/Clob instances created via its own
 * proprietary BLOB/CLOB API, and additionally doesn't accept large streams for
 * PreparedStatement's corresponding setter methods. Therefore, you need to use
 * OracleLobHandler there, which uses Oracle's BLOB/CLOB API for both all access.
 * The Oracle 10g JDBC driver should work with DefaultLobHandler too.
 *
 * <p>Of course, you need to declare different field types for each database.
 * In Oracle, any binary content needs to go into a BLOB, and all character content
 * beyond 4000 bytes needs to go into a CLOB. In MySQL, there is no notion of a
 * CLOB type but rather a LONGTEXT type that behaves like a VARCHAR. For complete
 * portability, just use a LobHandler for fields that might typically require LOBs
 * on some database because of their size (take Oracle's numbers as a guideline).
 *
 * @author Juergen Hoeller
 * @since 23.12.2003
 * @see DefaultLobHandler
 * @see OracleLobHandler
 * @see ResultSet#getBlob
 * @see ResultSet#getClob
 * @see ResultSet#getBytes
 * @see ResultSet#getBinaryStream
 * @see ResultSet#getString
 * @see ResultSet#getAsciiStream
 * @see ResultSet#getCharacterStream
 */
public interface LobHandler {

	/**
	 * Retrieve the given column as bytes from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getBytes</code> or work with
	 * <code>ResultSet.getBlob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnName the column name to use
	 * @return the content as byte array, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getBytes
	 */
	byte[] getBlobAsBytes(ResultSet rs, String columnName) throws SQLException;

	/**
	 * Retrieve the given column as bytes from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getBytes</code> or work with
	 * <code>ResultSet.getBlob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnIndex the column index to use
	 * @return the content as byte array, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getBytes
	 */
	byte[] getBlobAsBytes(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * Retrieve the given column as binary stream from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getBinaryStream</code> or work with
	 * <code>ResultSet.getBlob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnName the column name to use
	 * @return the content as binary stream, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getBinaryStream
	 */
	InputStream getBlobAsBinaryStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * Retrieve the given column as binary stream from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getBinaryStream</code> or work with
	 * <code>ResultSet.getBlob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnIndex the column index to use
	 * @return the content as binary stream, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getBinaryStream
	 */
	InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * Retrieve the given column as String from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getString</code> or work with
	 * <code>ResultSet.getClob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnName the column name to use
	 * @return the content as String, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getString
	 */
	String getClobAsString(ResultSet rs, String columnName) throws SQLException;

	/**
	 * Retrieve the given column as String from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getString</code> or work with
	 * <code>ResultSet.getClob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnIndex the column index to use
	 * @return the content as String, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getString
	 */
	String getClobAsString(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * Retrieve the given column as ASCII stream from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getAsciiStream</code> or work with
	 * <code>ResultSet.getClob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnName the column name to use
	 * @return the content as ASCII stream, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getAsciiStream
	 */
	InputStream getClobAsAsciiStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * Retrieve the given column as ASCII stream from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getAsciiStream</code> or work with
	 * <code>ResultSet.getClob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnIndex the column index to use
	 * @return the content as ASCII stream, or null in case of SQL NULL
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getAsciiStream
	 */
	InputStream getClobAsAsciiStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * Retrieve the given column as character stream from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getCharacterStream</code> or work with
	 * <code>ResultSet.getClob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnName the column name to use
	 * @return the content as character stream
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getCharacterStream
	 */
	Reader getClobAsCharacterStream(ResultSet rs, String columnName) throws SQLException;

	/**
	 * Retrieve the given column as character stream from the given ResultSet.
	 * Might simply invoke <code>ResultSet.getCharacterStream</code> or work with
	 * <code>ResultSet.getClob</code>, depending on the database and driver.
	 * @param rs the ResultSet to retrieve the content from
	 * @param columnIndex the column index to use
	 * @return the content as character stream
	 * @throws SQLException if thrown by JDBC methods
	 * @see ResultSet#getCharacterStream
	 */
	Reader getClobAsCharacterStream(ResultSet rs, int columnIndex) throws SQLException;

	/**
	 * Create a new LobCreator instance, i.e. a session for creating BLOBs
	 * and CLOBs. Needs to be closed after the created LOBs are not needed
	 * anymore, i.e. after statement execution or transaction completion.
	 * @return the new LobCreator instance
	 * @see LobCreator#close
	 */
	LobCreator getLobCreator();

}
