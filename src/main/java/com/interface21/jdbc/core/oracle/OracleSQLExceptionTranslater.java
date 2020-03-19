package com.interface21.jdbc.core.oracle;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.DataIntegrityViolationException;
import com.interface21.jdbc.core.BadSqlGrammarException;
import com.interface21.jdbc.core.SQLExceptionTranslater;
import com.interface21.jdbc.core.UncategorizedSQLException;

import java.sql.SQLException;


/**
 * Implementation of SQLExceptionTranslator that uses
 * Oracle vendor code. More precise than SQLState implementation,
 * but Oracle-specific.
 * @version $Id: OracleSQLExceptionTranslater.java,v 1.1 2002/08/24 08:45:33 Rod Johnson Exp $
 */
public class OracleSQLExceptionTranslater implements SQLExceptionTranslater {

	/**
	 * @see SQLExceptionTranslater#translate(String, String, SQLException)
	 */
	public DataAccessException translate(String task, String sql, SQLException sqlex) {
		switch (sqlex.getErrorCode()) {
			case 1 :
				// Unique constraint violated
				return new DataIntegrityViolationException(task + ": " + sqlex.getMessage(), sqlex);
			
			case 1400:
				// 	Can't insert null into non-nullable column
				return new DataIntegrityViolationException(task + ": " + sqlex.getMessage(), sqlex);
			case 936 : 
					// missing expression
					return new BadSqlGrammarException(task, sql, sqlex);
			case 942 :
					// table or view does not ex
			 return new BadSqlGrammarException(task, sql, sqlex);
		}

		// We couldn't identify it more precisely
		return new UncategorizedSQLException("(" + task + 
			"): encountered SQLException [" + 
			sqlex.getMessage() + "]", sql, sqlex);
	}



}