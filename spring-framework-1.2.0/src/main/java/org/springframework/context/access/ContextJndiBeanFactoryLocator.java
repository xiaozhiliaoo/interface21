/*
 * Created on Jan 26, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.access;

import javax.naming.NamingException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.BootstrapException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.util.StringUtils;

/**
 * BeanFactoryLocator implementation that creates the BeanFactory from one or
 * more classpath locations specified in one JNDI environment variable.
 *
 * <p>This default implementation creates a ClassPathXmlApplicationContext.
 * Subclasses may override createBeanFactory for custom instantiation.
 *
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 */
public class ContextJndiBeanFactoryLocator extends JndiLocatorSupport implements BeanFactoryLocator {

	/**
	 * Any number of these characters are considered delimiters between
	 * multiple bean factory config paths in a single String value.
	 */
	public static final String BEAN_FACTORY_PATH_DELIMITERS = ",; \t\n";

	/**
	 * Load/use a bean factory, as specified by a factoryKey which is a JNDI
	 * address, of the form <code>java:comp/env/ejb/BeanFactoryPath</code>. The
	 * contents of this JNDI location must be a string containing one or more
	 * classpath resource names (separated by any of the delimiters
	 * '<code>,; \t\n</code>' if there is more than one. The resulting
	 * BeanFactory (or subclass) will be created from the combined resources.
	 */
	public BeanFactoryReference useBeanFactory(String factoryKey) throws BeansException {
		String beanFactoryPath = null;
		try {
			beanFactoryPath = (String) lookup(factoryKey);
			if (logger.isInfoEnabled()) {
				logger.info("Bean factory path from JNDI environment variable [" + factoryKey +
						"] is: " + beanFactoryPath);
			}
			String[] paths = StringUtils.tokenizeToStringArray(beanFactoryPath, BEAN_FACTORY_PATH_DELIMITERS);
			return createBeanFactory(paths);
		}
		catch (NamingException ex) {
			throw new BootstrapException("Define an environment variable [" + factoryKey + "] containing " +
					"the class path locations of XML bean definition files", ex);
		}
	}

	/**
	 * Actually create the BeanFactory, given an array of class path resource strings
	 * which should be combined. This is split out as a separate method so that subclasses
	 * can override the actual type uses (to be an ApplicationContext, for example).
	 * @param resources an array of Strings representing classpath resource names
	 * @return the created BeanFactory, wrapped in a BeanFactoryReference
	 */
	protected BeanFactoryReference createBeanFactory(String[] resources) throws BeansException {
		return new ContextBeanFactoryReference(new ClassPathXmlApplicationContext(resources));
	}

}
