/**
 * Feel free to use this code (with acknowledgment) or to
 * modify it to meet your needs.
 * These packages are described in "Expert One-on-One
 * J2EE Design and Development" by Rod Johnson (Wrox, 2002).
 * 
 * For commercial support, contact rod.johnson@interface21.com.
 */

package com.interface21.jdbc.object;

import com.interface21.dao.InvalidDataAccessApiUsageException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementCreator;
import com.interface21.jdbc.core.PreparedStatementCreatorFactory;
import com.interface21.util.StringUtils;

/** 
 * RdbmsOperation using a JdbcTemplate and representing a SQL-based
 * operation such as a query or update (as opposed to a stored
 * procedure).
 * Configures a PreparedStatementCreatorFactory based on the declared
 * parameters.
 * @author Rod Johnson
 * @version $Id: SqlOperation.java,v 1.2 2002/08/24 14:08:44 Rod Johnson Exp $
 */
public abstract class SqlOperation extends RdbmsOperation {
	
	//-------------------------------------------------------------------------
	// Instance data
	//-------------------------------------------------------------------------
 	/** Callback helper */
 	private JdbcTemplate jdbcTemplate;
 		
 	/** 
 	 * Object enabling us to create PreparedStatementCreators
 	 * efficiently, based on this class's declared parameters
 	 */
 	private PreparedStatementCreatorFactory preparedStatementFactory;

	//-------------------------------------------------------------------------
	// Constructors
	//-------------------------------------------------------------------------
	/**
	 * Allow use as a bean
	 */
	public SqlOperation() {
	}


	//-------------------------------------------------------------------------
	// Bean properties
	//-------------------------------------------------------------------------
	/**
	 * Return the JdbcTemplate object used by this object
	 */
	protected final JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	
	/**
	 * Return a PreparedStatementCreator to perform an operation
	 * with this parameters
	 * @param params parameters. May be null.
	 */
	protected final PreparedStatementCreator newPreparedStatementCreator(Object[] params) {
		return this.preparedStatementFactory.newPreparedStatementCreator(params);
	}
	
	/**
	 * Overriden method to configure the PreparedStatementCreatorFactory
	 * based on our declared parameters.
	 * @see RdbmsOperation#compileInternal()
	 */
	protected final void compileInternal() {
		this.jdbcTemplate = new JdbcTemplate(getDataSource());
		// Validate parameter count
		int bindVarCount = StringUtils.countOccurrencesOf(getSql(), "?");
		if (bindVarCount != getDeclaredParameters().size())
			throw new InvalidDataAccessApiUsageException("SQL '" + getSql() + "' requires " + bindVarCount + 
				" bind variables, but " + getDeclaredParameters().size() + " variables were declared for this object");
		
		this.preparedStatementFactory = new PreparedStatementCreatorFactory(getSql(), getDeclaredParameters());
		onCompileInternal();
	}
	
	
	/**
	 * Hook method that subclasses may override to react
	 * to compilation.
	 * This implementation does nothing.
	 */
	protected void onCompileInternal() {
		logger.finest("NOP onCompileInternal");
	}

}