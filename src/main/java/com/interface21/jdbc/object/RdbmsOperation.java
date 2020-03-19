package com.interface21.jdbc.object;

import java.util.LinkedList;
import java.util.List;
import java14.java.util.logging.Logger;

import javax.sql.DataSource;

import com.interface21.beans.factory.InitializingBean;
import com.interface21.dao.DataAccessException;
import com.interface21.dao.InvalidDataAccessApiUsageException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.SqlParameter;

/** 
 * Multithreaded, reusable object representing a JDBC operation.
 * <br/><b>Not</b> a command, as a command isn't be reusable.
 * However, execute methods may take commands as arguments.
 * <br/>Subclasses should be Java beans, allowing easy configuration.
 * <br/>Throws runtime exceptions, defined in the com.interface21.dao
 * package (and as thrown by the com.interface21.jdbc.core package,
 * which the classes in this package use).
 * <br/>Subclasses should set DataSource, sql and add
 * parameters, before invoking the compile() method.
 * The order in which parameters are added is significant.
 * The appropriate execute or update method can then be invoked.
 * @author Rod Johnson
 * @version $Id: RdbmsOperation.java,v 1.3 2002/08/24 17:49:27 Rod Johnson Exp $
 */
public abstract class RdbmsOperation implements InitializingBean {
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());

	/**
	 * DataSource to use
	 */
	private DataSource dataSource;
	
	/**
	 * List of SqlParameter objects
	 */
	private List declaredParameters = new LinkedList();
	
	/**
	 * SQL statement
	 */
	private String sql;
	
	/**
	 * Has this operation been compiled? Compilation means at
	 * least checking that a DataSource and sql have been provided,
	 * but subclasses may also implement their own custom validation.
	 */
	private boolean compiled;
	
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/**
	 *  Construct a new RdbmsOperation
	 */
	protected RdbmsOperation() {
	}
	
	
	//---------------------------------------------------------------------
	//  Public methods
	//---------------------------------------------------------------------
	/**
	 * Add anonymous parameters.
	 * Parameter ordering is significant. This method is an alternative
	 * to the declareParameter() method, which should normally
	 * be preferred.
	 * @throws InvalidDataAccessApiUsageException if the operation is already compiled
	 */
	protected void setTypes(int[] types) throws InvalidDataAccessApiUsageException {
		if (compiled)
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once query is compiled");
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				declareParameter(new SqlParameter(types[i]));
			}
		}
	}
	
	
	/**
	 * Declare a parameter
	 * @param param SqlParameter to add
	 * @throws InvalidDataAccessApiUsageException if the operation is already compiled
	 */
	public void declareParameter(SqlParameter param) throws InvalidDataAccessApiUsageException {
		if (compiled)
			throw new InvalidDataAccessApiUsageException("Cannot add parameters once query is compiled");
		declaredParameters.add(param);
	}
	
	
	/**
	 * @return a list of the declared Parameter objects
	 */
	protected List getDeclaredParameters() {
		return declaredParameters;
	}
	
	/**
	 * Subclasses can override this to supply dynamic SQL if they wish,
	 * but SQL is normally set by calling the setSql() method
	 * or in a subclass constructor.
	 */
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}
	 
	/**
	 * Gets the DataSource in use
	 * @return Returns a ConnectionFactory
	 */
	protected final DataSource getDataSource() {
		return this.dataSource;
	}
	
	/**
	 * Sets the DataSource
	 * @param dataSource The DataSource to set
	 */
	public final void setDataSource(DataSource dataSource) {		
		this.dataSource = dataSource;
	}
	
	
	//---------------------------------------------------------------------
	// Implementation of InitializingBean
	//---------------------------------------------------------------------
	/**
	 * Ensures compilation if used in a bean factory
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet()  {
		compile();
	}
	
	/**
	 * Is this operation compiled
	 * @return whether this operation is compiled, and ready to use.
	 */
	public boolean isCompiled() {
		return compiled;
	}
	
	
	/**
	 * Compile this query.
	 * Ignore subsequent attempts to compile
	 * @throws InvalidDataAccessApiUsageException if the object hasn't
	 * been correctly initialized, for example if no DataSource has been provided.
	 */
	public final void compile() throws InvalidDataAccessApiUsageException {
		if (!isCompiled()) { 
			if (this.sql == null)
				throw new InvalidDataAccessApiUsageException("Sql must be set in class " + getClass().getName());
			if (this.dataSource == null)
				throw new InvalidDataAccessApiUsageException("DataSource must be set in class " + getClass().getName());
			// Invoke subclass compile
			compileInternal();
			this.compiled = true;
			logger.info("Compiled OK");
		}
	}	// compile
	
	
	/**
	 * Subclasses must implement to perform their own compilation.
	 * Invoked after this class's compilation is complete.
	 * Subclasses can assume that sql has been supplied and that
	 * a DataSource has been supplied.
	 * @throws InvalidDataAccessApiUsageException if the subclass
	 * hasn't been properly configured.
	 */
	protected abstract void compileInternal() throws InvalidDataAccessApiUsageException;


	/**
	 * Validate the parameters passed to an execute method
	 * based on declared parameters.
	 * Subclasses should invoke this method before every execute() or update()
	 * method.
	 * @param parameters parameters supplied. May be null.
	 * @throws RdbmsOperationException if the parameters are invalid
	 */
	protected final void validateParameters(Object[] parameters) throws InvalidDataAccessApiUsageException {
		if (!compiled)
			throw new InvalidDataAccessApiUsageException("SQL operation must be compiled before execution");
			
		if (parameters != null) {
			if (declaredParameters == null)
				throw new InvalidDataAccessApiUsageException("Didn't expect any parameters: none was declared");
			if (parameters.length != declaredParameters.size())
				throw new InvalidDataAccessApiUsageException(parameters.length + " parameters were supplied, but none was declared in class " + getClass().getName());
		}
		else {
			// No parameters were supplied
			if (!declaredParameters.isEmpty())
				throw new InvalidDataAccessApiUsageException(declaredParameters.size() + " parameters must be supplied");
		}
	}	// validateParameters

}	// RdbmsOperation
