package java14.java.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Implementation of EJBLogger for WebLogic.
 * @author  Rod Johnson
 * @since June 1, 2001
 */
public class NullLogger extends Logger {


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates a new WebLogicEJBLogger */
	public NullLogger(String name) {
		super(name);
	}

	//---------------------------------------------------------------------
	// Implementation of EJBLogger
	//---------------------------------------------------------------------	
	/**
	 * @see Logger#doLog(LogRecord)
	 */
	public void doLog(LogRecord r) {
	}

	/**
	 * @see Logger#isLoggable(Level)
	 */
	public boolean isLoggable(Level level) {
		return false;
	}

	
} // class ConsoleLogger