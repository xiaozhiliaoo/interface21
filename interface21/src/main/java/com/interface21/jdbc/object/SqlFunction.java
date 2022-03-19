
package com.interface21.jdbc.object;

import com.interface21.dao.InvalidDataAccessApiUsageException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;



/**
 * SQL function wrapper that expects an int argument.
 * @author Rod Johnson
 */
public class SqlFunction extends ManualExtractionSqlQuery {
	
	/**
	 * Bean constructor
	 */
	public SqlFunction() {
	}
	
	public SqlFunction(DataSource ds, String sql, int[] types) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		setRowsExpected(1);
	}
	
	
	/**
	 * Must add parameters or settle with none
	 */
	public SqlFunction(DataSource ds, String sql) {
		setDataSource(ds);
		setSql(sql);
		setRowsExpected(1);
	}
	
	
	/*
	 * @see CustomExtractionQueryCommand#extract(ResultSet, int)
	 */
	protected Object extract(ResultSet rs, int rownum) throws SQLException, InvalidDataAccessApiUsageException {
		if (rownum != 0) {
			throw new InvalidDataAccessApiUsageException("SQL function '" + getSql() + "' can't return more than one row");			
		}
		return new Integer(rs.getInt(1));
	}
	
	/**
	 * Convenient method to run the function without arguments
	 */
	public int run() {
		Integer I = (Integer) super.findObject((Object[]) null);
		return I.intValue();
	}
	
	public int run(int p) {
		Integer I = (Integer) super.findObject(p);
		return I.intValue();
	}
	
	public int run(Object[] args) {
		Integer I = (Integer) super.findObject(args);
		return I.intValue();
	}

}
