package java14.java.util.logging.log4j;

import java14.java.util.logging.Level;
import java14.java.util.logging.LogRecord;
import java14.java.util.logging.Logger;
import org.apache.log4j.Priority;

/**
 * Implementation 
 * NB: Doesn't correctly pick up stack traces when caller
 * is in same package
 * @author  Rod Johnson
 * @since June 1, 2001
 */
public class Log4jLogger extends Logger {

	/**
	 * callerFQCN parameter to Log4j.Category.log(callerFQN, ...)
	 * Used to exclude this class and the whole package from
	 * stack traces.
	 */
	private static String WRAPPER_FQN = java14.java.util.logging.Logger.class.getName();

	//private org.apache.log4j.Logger logger;
	private org.apache.log4j.Category logger;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates a new WebLogicEJBLogger */
	public Log4jLogger(String name, org.apache.log4j.Category logger) {
		super(name);
		this.logger = logger;

	}

	//---------------------------------------------------------------------
	// Implementation of abstract methods
	//---------------------------------------------------------------------	
	/**
	 * @see Logger#doLog(LogRecord)
	 */
	protected void doLog(LogRecord r) {
		// We use the most generic Log4j method
		// to ensure it picks up the correct stack trace,
		// rather than taking the application logging line to be this call

		/*
		Throwable t = new Throwable();
		t.printStackTrace();
		String classname = com.interface21.logging.Logger.class.getName();
		//classname = getClass().getSuperclass().getName();
		System.out.println("classname is '" + classname + "'");
		
		LocationInfo li = new LocationInfo(t, classname);
			System.out.println(li.getFileName() + "." + li.getMethodName() + ":" + li.getLineNumber());
			*/
		logger.log(WRAPPER_FQN, toPriority(r.getLevel()), r.getMessage(), r.getThrown());
	}

	/**
	 * @see Logger#isLoggable(Level)
	 */
	public boolean isLoggable(Level level) {
		return logger.isEnabledFor(toPriority(level));
	}

	/** Log the given message with the given severity
	 * @param message message to log
	 * @param severity log level
	 */
	private Priority toPriority(Level level) {
		if (level == Level.SEVERE) {
			return Priority.ERROR;
		}
		else if (level == Level.WARNING) {
			return Priority.WARN;
		}
		else if (level == Level.INFO) {
			return Priority.INFO;
		}
		else if (level == Level.FINE) {
			return Priority.DEBUG;
		}
		else if (level == Level.FINER) {
			return Priority.DEBUG;
		}
		else if (level == Level.FINEST) {
			return Priority.DEBUG;
		}
		else if (level == Level.CONFIG) {
			return Priority.INFO;
		}
		return Priority.INFO;
	}

} // class Log4jLogger