/*
 * (c) Copyright 2002 Rod Johnson.
 */

package java14.java.util.logging.log4j;

import com.interface21.beans.factory.InitializingBean;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import java.io.InputStream;
import java.util.Properties;

/**
 * Default logger
 * @version 
 * @author Rod Johnson
 */
public class Log4jLoggerFactory implements java14.java.util.logging.LoggerFactory, InitializingBean {

	public Log4jLoggerFactory() {
	}
	
	// Use / prefix to look in root of package hierarchy, 
	// rather than in this package
	private String propsFile = "/log4j.properties";
	
	private boolean loadedOK;
	

	public java14.java.util.logging.Logger getLogger(String name) {
		// The 1.2 version (above) conflicts with 1.1.3 (as with JBoss) at runtime
		//return new Log4jLogger(name, org.apache.log4j.Logger.getLogger(name));
		
//		if (name.indexOf("EJB") != -1) {
//		String s = ClassLoaderAnalyzer.showClassLoaderHierarchy(this, "Log4jFactory for " + name, "\n", "    ");
//		System.out.println(s);
//		}
		
		return new Log4jLogger(name, org.apache.log4j.Category.getInstance(name));
	}
	
	
	/**
	 * Can use this to set a package that the configuration should come from
	 * log4j.properties may be supplied by another application, such as JBoss
	 */
	public void setPropertiesLocation(String propsFile) {
		this.propsFile = propsFile;
	}

	/**
	 * @see InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		System.out.println("Configuring log4j...");
		Properties props = new Properties();
		
		if (this.propsFile == null || "".equals(this.propsFile)) {
			System.out.println("Using existing Log4j configuration");
			return;
		}
		
		
		InputStream is = getClass().getResourceAsStream(propsFile);
		if (is != null) {
			props.load(is);
			PropertyConfigurator.configure(props);
			System.out.println("Configured log4j from file '" + propsFile + "' in classpath");
			loadedOK = true;
		}
		else {
			System.out.println("Cannot load file '" + propsFile + "' from classpath: using BasicConfiguration");
			BasicConfigurator.configure();
		}
	}
	
	public String toString() {
		String s = getClass().getName() + ": properties URL (taken from classpath) is '" + propsFile + "'; ";
		s += loadedOK ? "Loading succeeded" : "Loading failed (used default configuration)";
		return s;
	}

}