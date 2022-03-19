/*
 * (c) Copyright 2002 Rod Johnson.
 */
 
package java14.java.util.logging;


/**
 * Default logger
 * @version 
 * @author Rod Johnson
 */
public class ConsoleLoggerFactory implements LoggerFactory{
	
	private Level level = Level.ALL;
	
	public ConsoleLoggerFactory() {
	}
	
	public Logger getLogger(String name) {
		return new ConsoleLogger(name, this.level);
	}
	
	/**
	 * Single bean property exposed:
	 * String: CONFIG, SEVERE, WARNING, INFO, FINE, 
	 * FINER, FINEST or ALL.
	 * Relies on Level class to parse
	 */
	public void setLevel(String slevel) {
		this.level = Level.parse(slevel.toUpperCase());
		System.out.println("Console logging level will be set to " + level);
	}
	

}
