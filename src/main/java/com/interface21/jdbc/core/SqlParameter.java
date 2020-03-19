package com.interface21.jdbc.core;


/**
 * Object to represent a SQL parameter definition.
 * Parameters may be anonymous, in which case name is null.
 * However all parameters must define a SQL type constant
 * from java.sql.Types.
 */
public class SqlParameter {

	private String name;
	
	/** SQL type constant from java.sql.Types */
	private int type;

	
	/**
	 * Add a new anonymous parameter
	 */
	public SqlParameter(int type) {
		this(null, type);
	}

	public SqlParameter(String name, int type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public int getSqlType() {
		return type;
	}

}