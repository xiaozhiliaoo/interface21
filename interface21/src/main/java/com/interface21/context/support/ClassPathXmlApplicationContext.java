package com.interface21.context.support;

import com.interface21.context.ApplicationContextException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Standalone XML application context useful for test harnesses.
 * @author Rod Johnson
 */
public class ClassPathXmlApplicationContext extends XmlApplicationContextSupport {

	public ClassPathXmlApplicationContext(String paths) throws ApplicationContextException, IOException {
		super(paths);
	}

	public ClassPathXmlApplicationContext(String[] paths) throws ApplicationContextException, IOException {
		super(paths);
	}
	
	protected InputStream getInputStream(String path) throws IOException {
		return getClass().getResourceAsStream(path);	
	}

}