package com.interface21.context.support;

import java.io.IOException;
import java.io.InputStream;

import com.interface21.context.ApplicationContext;
import com.interface21.context.ApplicationContextException;
import com.interface21.util.StringUtils;

/**
 * Standalone XML application context useful for test harnesses.
 * Does CSV paths:
 * parent first
 * @author Rod Johnson
 */
public abstract class XmlApplicationContextSupport extends AbstractXmlApplicationContext {

	private InputStream is;

	public XmlApplicationContextSupport(String paths) throws ApplicationContextException, IOException {
		this(StringUtils.commaDelimitedListToStringArray(paths));
	}

	public XmlApplicationContextSupport(String[] paths) throws ApplicationContextException, IOException {
		if (paths.length == 0)
			throw new ApplicationContextException("must have paths");

		String thisPath = paths[paths.length - 1];
		logger.fine("Trying to open XML application context file '" + thisPath + "'");
	//	fis = new FileInputStream(thisPath);
	is = getInputStream(thisPath);
		logger.fine("Opened XML application context file '" + thisPath + "' OK");

		// Recurse
		if (paths.length > 1) {
			// There were parent(s)
			String[] parentPaths = new String[paths.length - 1];
			System.arraycopy(paths, 0, parentPaths, 0, paths.length - 1);
			logger.fine("Setting parent context for paths: [" + StringUtils.arrayToDelimitedString(parentPaths, ","));
			ApplicationContext parent = new FileSystemXmlApplicationContext(parentPaths);
			setParent(parent);
		}
		refresh();
	}
	
	protected abstract InputStream getInputStream(String path) throws IOException;

	/**
	* @see AbstractXMLApplicationContext#getInputStreamForBeanFactory()
	*/
	protected final InputStream getInputStreamForBeanFactory() throws IOException {
		return is;
	}

}