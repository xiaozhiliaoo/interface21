package java14.java.util.logging;

import com.interface21.beans.factory.NoSuchBeanDefinitionException;
import com.interface21.beans.factory.support.ListableBeanFactoryImpl;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Loads LoggerFactory from bean
 * <p>
 * Could make serializable, with transient fields to underlying
 * log adapter? could then reconstitute on other side
 * but really want this?
 */
public abstract class Logger {

	private static final String ENTERING = "ENTRY";
	private static final String EXITING = "RETURN";
	private static final String THROWING_MESSAGE = "THROW";

	private String name;

	private Filter filter;

	public Logger(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}

	private static LoggerFactory $loggerFactory;
	
	private static Logger $initializingLogger;
	
	// WHY NOT USE A STATIC INITIALIZER?

	/**
	 * Why do Sun never use separate factory classes!????
	 * Synching shouldn't be an issue: this should be called once
	 * per class
	 */
	public static synchronized Logger getLogger(String name) {
		// We need this check to stop an infinite loop with BeanFactory impl:
		// bean factory will use this logger
		
		// CHECK CALLER!? slow
		if ($initializingLogger != null) {
			return $initializingLogger;
		}
		
		if ($loggerFactory == null) {
			$initializingLogger = new ConsoleLogger("com.interface21.logging.Logger", Level.WARNING);
			
			System.out.println("Initializing logger factory...");

			// Distinguish between failure to load a resource bundle
			// and failure to load properties
			try {
				ResourceBundle rb  = ResourceBundle.getBundle("java14/java/util/logging");
	
				// We must pass a class loader context to the ListableBeanFactoryImpl
				// class, which may have been loaded by a different class Loader.
				ListableBeanFactoryImpl lbf = new ListableBeanFactoryImpl(new Object().getClass());
				lbf.registerBeanDefinitions(rb, null);
				$loggerFactory = (LoggerFactory) lbf.getBean("loggerFactory");
				System.out.println("Initialized LoggerFactory bean [" + $loggerFactory + "]");
			}
			catch (MissingResourceException ex) {
				System.out.println("WARNING: no logging.properties file found on the classpath");
			}
			catch (NoSuchBeanDefinitionException ex) {
				System.out.println("WARNING: no bean named 'loggerFactory' defined in logging.properties");
			}
			catch (Exception ex) {
				System.out.println("WARNING: error instantiating and configuring 'loggerFactory' bean defined in logging.properties: " + ex);
			}

			if ($loggerFactory == null) {
				$loggerFactory = new ConsoleLoggerFactory();
				System.out.println("WARNING: no valid LoggerFactory specified: using console logging");
			}
			$initializingLogger = null;
		}	// if creating logger factory
		
		return $loggerFactory.getLogger(name);
	}

	public void config(String msg) {
		log(Level.CONFIG, msg);
	}

	public void entering(String sourceClass, String sourceMethod) {
		logp(Level.FINER, sourceClass, sourceMethod, ENTERING);
	}

	public void entering(String sourceClass, String sourceMethod, Object param) {
		logp(Level.FINER, sourceClass, sourceMethod, ENTERING + " {0}", new Object[] { param });
	}

	public void entering(String sourceClass, String sourceMethod, Object[] params) {
		StringBuffer buf = new StringBuffer(ENTERING);
		for (int index = 0; index < params.length; index++) {
			buf.append(" {");
			buf.append(index);
			buf.append("}");
		}
		logp(Level.FINER, sourceClass, sourceMethod, buf.toString(), params);
	}

	public void exiting(String sourceClass, String sourceMethod) {
		logp(Level.FINER, sourceClass, sourceMethod, EXITING);
	}

	public void exiting(String sourceClass, String sourceMethod, Object result) {
		logp(Level.FINER, sourceClass, sourceMethod, EXITING + " {0}", new Object[] { result });
	}

	public void info(String msg) {
		log(Level.INFO, msg);
	}

	public void fine(String msg) {
		log(Level.FINE, msg);
	}

	public void finer(String msg) {
		log(Level.FINER, msg);
	}

	public void finest(String msg) {
		log(Level.FINEST, msg);
	}

	public void severe(String msg) {
		log(Level.SEVERE, msg);
	}

	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		logp(Level.FINER, sourceClass, sourceMethod, THROWING_MESSAGE, thrown);
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
		if (isLoggable(level)) {
			LogRecord record = new LogRecord(level, msg);
			record.setSourceClassName(sourceClass);
			record.setSourceMethodName(sourceMethod);
			log(record);
		}
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param) {
		if (isLoggable(level)) {
			LogRecord record = new LogRecord(level, msg);
			record.setSourceClassName(sourceClass);
			record.setSourceMethodName(sourceMethod);
			record.setParameters(new Object[] { param });
			log(record);
		}
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
		if (isLoggable(level)) {
			LogRecord record = new LogRecord(level, msg);
			record.setSourceClassName(sourceClass);
			record.setSourceMethodName(sourceMethod);
			record.setParameters(params);
			log(record);
		}
	}

	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
		if (isLoggable(level)) {
			LogRecord record = new LogRecord(level, msg);
			record.setSourceClassName(sourceClass);
			record.setSourceMethodName(sourceMethod);
			record.setThrown(thrown);
			log(record);
		}
	}

	public void warning(String msg) {
		log(Level.WARNING, msg);
	}

	public void log(Level level, String msg) {
		if (isLoggable(level)) {
			LogRecord record = new LogRecord(level, msg);
			log(record);
		}
	}

	public void log(LogRecord record) {
		if (isLoggable(record.getLevel())) {
			record.setLoggerName(getName());
			if (this.filter == null || this.filter.isLoggable(record)) {
				doLog(record);
			}
		}
	}

	public abstract boolean isLoggable(Level level);

	protected abstract void doLog(LogRecord r);

}