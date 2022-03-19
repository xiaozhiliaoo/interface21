package com.interface21.jdbc.object;

import com.interface21.dao.InvalidDataAccessApiUsageException;
import com.interface21.jdbc.core.JdbcUpdateAffectedIncorrectNumberOfRowsException;

import javax.sql.DataSource;

/**
 * RdbmsOperation subclass representing a SQL update.
 */
public class SqlUpdate extends SqlOperation {

	/** 
	 * Maximum number of rows the update may affect.
	 * If more are affected, an exception will be thrown.
	 * Ignored if 0.
	 */
	private int maxRowsAffected;
	
	private int requiredRowsAffected;
	
	/**
	 * Bean constructor
	 */
	public SqlUpdate() {
	}

	public SqlUpdate(DataSource ds, String sql, int[] types) {
		this(ds, sql, types, Integer.MAX_VALUE);
	}

	public SqlUpdate(DataSource ds, String sql, int[] types, int maxRowsAffected) {
		setDataSource(ds);
		setSql(sql);
		setTypes(types);
		this.maxRowsAffected = maxRowsAffected;
	}
	
	public void setMaxRowsAffected(int max) {
		this.maxRowsAffected = max;
	}
	
	public void setRequiredRowsAffected(int rowsAffected) {
		this.requiredRowsAffected = requiredRowsAffected;
	}

	/**
	 * Execute the update given the arguments
	 * @param args array of object arguments
	 * @return the number of rows affected by the update
	 */
	public int update(Object[] args) throws InvalidDataAccessApiUsageException {
		validateParameters(args);

		//PreparedStatementCreator psc = new DefaultPreparedStatementCreator(getSql(), getDeclaredParameters(), args);
		int rowsAffected = getJdbcTemplate().update(newPreparedStatementCreator(args));
		logger.fine(getSql());

		if (maxRowsAffected != 0 && rowsAffected > maxRowsAffected) {
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(getSql(), maxRowsAffected, rowsAffected);
		}
		
		if (requiredRowsAffected != 0 && rowsAffected != requiredRowsAffected) {
			throw new JdbcUpdateAffectedIncorrectNumberOfRowsException(getSql(), rowsAffected, requiredRowsAffected);
		}

		logger.info(rowsAffected + " rows affected by SQL update [" + getSql() + "]");
		return rowsAffected;
	}	// update
	
	
	/**
	 * Convenience method to execute an update with no parameters
	 */
	public int update() {
		return update((Object[]) null);
	}

	/** 
	 * Convenient method where we have one int arg
	 */
	public int update(int p1) {
		return update(new Object[] { new Integer(p1)});
	}
	
	/** 
	 * Convenient method where we have 2 int args
	 */
	public int update(int p1, int p2) {
		return update(new Object[] { new Integer(p1), new Integer(p2)});
	}
	
	/** 
	 * Convenient method where we have one String arg
	 */
	public int update(String p) {
		return update(new Object[] { p });
	}


}