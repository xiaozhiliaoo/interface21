package com.interface21.jdbc.object;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;
import javax.sql.DataSource;

import com.interface21.dao.InvalidDataAccessApiUsageException;
import com.interface21.jdbc.core.*;
import com.interface21.jdbc.core.DataSourceUtils;
import com.interface21.jdbc.core.SQLExceptionTranslater;
import com.interface21.jdbc.core.UncategorizedSQLException;
import com.interface21.jdbc.core.SqlParameter;

/**
 * Superclass for object abstractions of RDBMS stored procedures.
 * Execute methods are protected, preventing use other than through
 * a subclass that offers tighter typing.
 * The inherited sql property is stored procedure name.
 * JDBC 3.0 introduces named parameters.
 * @author Rod Johnson
 */
public abstract class StoredProcedure extends RdbmsOperation {

	/** String of form	{call add_invoice(?, ?, ?)} 
	 * Updated after each parameter is added.
	 **/
	private String callString;
	
	/** Helper to translate SQL exceptions to DataAccessExceptions */
	private SQLExceptionTranslater exceptionTranslater;
	
	
	/**
	 * Allow use as a bean
	 */
	protected StoredProcedure() {
		this.exceptionTranslater = new SQLStateSQLExceptionTranslater();
	}
	
	/**
	 * Set the exception translater used in this class 
	 */
	public void setExceptionTranslater(SQLExceptionTranslater exceptionTranslater) {
		this.exceptionTranslater = exceptionTranslater;
	}

	/**
	 * Create a new object wrapper for a stored procedure.
	 * @param cf ConnectionFactory to use throughout the lifetime
	 * of this object
	 * @param name name of the stored procedure in the database.
	 */
	protected StoredProcedure(DataSource ds, String name) {
		setDataSource(ds);
		setSql(name);
	}

	/**
	 * Overridden method.
	 * Add a parameter.
	 * <b>NB: Calls to addParameter must be made in the same
	 * order as they appear in the database's stored procedure parameter
	 * list.</b> Names are purely used to help mapping
	 * @param p Parameter object (as defined in the Parameter
	 * inner class)
	 */
	public void declareParameter(SqlParameter p) throws InvalidDataAccessApiUsageException {
		if (p.getName() == null)
			throw new InvalidDataAccessApiUsageException("Parameters to stored procedures must have names as well as types");
		super.declareParameter(p);
	}


	/**
	 * Override of NOOP RdbmsOperation.compileInternal() to 
	 * ensure that the call string is up to date after
	 * a parameter has been added.
	 */
	protected void compileInternal() {
		List parameters = getDeclaredParameters();
		callString = "{call " + getSql() + "(";
		for (int i = 0; i < parameters.size(); i++) {
			if (i > 0)
				callString += ", ";
			callString += "?";
		}
		callString += ")}";
		logger.info("Compiled stored procedure. Call string is [" + callString + "]");
	}
	
	
	/** 
	 * Subclasses may invoke this method 
	 * to prepare type mappings etc.
	 * @return Map of input params, keyed by name
	 */
	protected Map execute(final Map inParams) {
		return execute(new ParameterMapper() {
			public Map createMap(Connection con) throws SQLException {
				return inParams;
			}
		});
	}


	/**
	 * Execute the stored procedure. All parameters
	 * must have been added before any calls are made
	 * to this method.
	 * <br/>Maps, using parameter names given in addParameter(),
	 * are used to hold input and output parameters.
	 * @param inParams Map of input parameters
	 * @return map of output parameters. In/out parameters
	 * will appear here, with their value after the stored
	 * procedure has been called.
	 */
	protected Map execute(ParameterMapper mapper) throws InvalidDataAccessApiUsageException {
		if (!isCompiled())
			throw new InvalidDataAccessApiUsageException("Stored procedure must be compiled before execution");
		
		Connection con = null;
		DataSource ds = getDataSource();
		try {
			con = ds.getConnection();
			
			Map inParams = mapper.createMap(con);
			
			CallableStatement call = con.prepareCall(this.callString);
			processInputParameters(inParams, call);

			// Execute the stored procedure
			call.execute();

			logger.info("Executing stored procedure [" + callString + "]");

			// Now get output parameters. There need not be any.
			Map outParams = extractOutputParameters(call);
			call.close();
			return outParams;
		}
		catch (SQLException ex) {
			//throw new UncategorizedSQLException("Call to stored procedure '" + getSql() + "' failed", ex);
			throw this.exceptionTranslater.translate("Call to stored procedure '" + getSql() + "'", this.callString, ex);
		}
		finally {
			DataSourceUtils.closeConnectionIfNecessary(ds, con);
		}
	} 	// execute
	

	/** 
	 * Set and register input parameters
	 * @param inParams parameters (including output parameters) to the stored procedure
	 * @param call CallableStatement representing the stored procedure
	 * @throws SQLException
	 */
	private void processInputParameters(Map inParams, CallableStatement call) throws SQLException, InvalidDataAccessApiUsageException {
		List parameters = getDeclaredParameters();
		for (int i = 0; i < parameters.size(); i++) {
			SqlParameter p = (SqlParameter) parameters.get(i);
			Object in = inParams.get(p.getName());
			if (!(p instanceof OutputParameter)) {
				// Input parameters must be supplied
				if (in == null)
					throw new InvalidDataAccessApiUsageException("Required parameter '" + p.getName() + "' is missing");
				call.setObject(i + 1, in, p.getSqlType());
			}
			else {
				// It's an output parameter.
				// It need not (but may be) supplied by the caller.
				call.registerOutParameter(i + 1, p.getSqlType());
				if (in != null) {
					call.setObject(i + 1, in, p.getSqlType());
				}
			}
		}
	}	// processInputParameters
	

	/**
	 * Extract output parameters from the completed stored procedure.
	 * @param outParams parameters to the stored procedure
	 * @param call JDBC wrapper for the stored procedure
	 * @throws SQLException
	 * @return 
	 */
	private Map extractOutputParameters(CallableStatement call) throws SQLException {
		Map outParams = new HashMap();
		List parameters = getDeclaredParameters();
		for (int i = 0; i < parameters.size(); i++) {
			SqlParameter p = (SqlParameter) parameters.get(i);
			if (p instanceof OutputParameter) {
				Object out = call.getObject(i + 1);
				outParams.put(p.getName(), out);
			}
		}
		return outParams;
	}
	
	
	/**
	 * Use when parameters need to be customized based 
	 * on the connection. We might need to do this to make
	 * use of proprietary features, available only with a specific
	 * Connection type.
	 */
	protected interface ParameterMapper {
		
		/**
		 * @return input parameters, keyed by name
		 */
		Map createMap(Connection con) throws SQLException;
	}

	/** 
	 * Subclass of SqlParameter to represent an output parameter.
	 * No additional properties: instanceof will be used to check
	 * for such types.
	 * Output parameters--like all stored procedure parameters--
	 * must have names.
	 **/
	public static class OutputParameter extends SqlParameter {
		
		public OutputParameter(String name, int type) {
			super(name, type);
		}
	}

}