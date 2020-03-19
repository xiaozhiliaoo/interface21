package java14.java.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Implementation of EJBLogger for WebLogic.
 * @author  Rod Johnson
 * @since June 1, 2001
 */
public class ConsoleLogger extends Logger {

	// BEAN USE!?
	
	private Level level;

	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/** Creates a new WebLogicEJBLogger */
	public ConsoleLogger(String name,  Level level) {
		super(name);
		this.level = level;
	}

	//---------------------------------------------------------------------
	// Implementation of EJBLogger
	//---------------------------------------------------------------------	
	/**
	 * @see Logger#doLog(LogRecord)
	 */
	public void doLog(LogRecord r) {
		System.out.println(format(r));
	}

	/**
	 * @see Logger#isLoggable(Level)
	 */
	public boolean isLoggable(Level level) {
		return level.intValue() >= this.level.intValue();
	}

	/** Log the given message with the given severity
	 * @param message message to log
	 * @param severity log level
	 */
	private String format(LogRecord r) {
		StringBuffer sb = new StringBuffer();
		
		// Slow; for debugging only
		sb.append(r.getSourceClassName() + ":" + r.getSourceMethodName() + "(): ");
		
		if (r.getLevel() == Level.SEVERE) {
			sb.append("!! SEVERE: <" + r.getMessage() + ">");
		}
		else if (r.getLevel() == Level.WARNING) {
			sb.append("?! WARNING: <" + r.getMessage() + ">");
		}
		else if (r.getLevel() == Level.INFO) {
			sb.append("! INFO: <" + r.getMessage() + ">");
		}
		else if (r.getLevel() == Level.FINE) {
			sb.append("FINE: <" + r.getMessage() + ">");
		}
		else if (r.getLevel() == Level.FINER) {
			sb.append("shh (finer): <" + r.getMessage() + ">");
		}
		else if (r.getLevel() == Level.FINEST) {
			sb.append("sshhhh (finest): <" + r.getMessage() + ">");

		}
		else if (r.getLevel() == Level.CONFIG) {
			sb.append("!! CONFIG: <" + r.getMessage() + ">");
		}
		
		if (r.getThrown() != null) {
			StringWriter sw = new StringWriter();
			r.getThrown().printStackTrace(new PrintWriter(sw));
			sb .append("\nRoot cause: " + sw.getBuffer());
		}

		return sb.toString();
	}

} // class ConsoleLogger