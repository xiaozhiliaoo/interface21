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

package org.springframework.beans.factory.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Abstract base class for bean definition readers which implement
 * the BeanDefinitionReader interface.
 *
 * <p>Provides common properties like the bean factory to work on
 * and the class loader to use for loading bean classes.
 *
 * @author Juergen Hoeller
 * @since 11.12.2003
 * @see BeanDefinitionReaderUtils
 */
public abstract class AbstractBeanDefinitionReader implements BeanDefinitionReader {

	protected final Log logger = LogFactory.getLog(getClass());

	private final BeanDefinitionRegistry beanFactory;

	private ClassLoader beanClassLoader = Thread.currentThread().getContextClassLoader();

	private ResourceLoader resourceLoader = new DefaultResourceLoader();


	/**
	 * Create a new AbstractBeanDefinitionReader for the given factory.
	 * @param beanFactory the bean factory to work on
	 */
	protected AbstractBeanDefinitionReader(BeanDefinitionRegistry beanFactory) {
		this.beanFactory = beanFactory;
	}

	public BeanDefinitionRegistry getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Set the ClassLoader to use for bean classes.
	 * Default is the thread context class loader.
	 * <p>Setting this to null suggests to not load bean classes but just register
	 * bean definitions with class names, for example when just registering beans
	 * in a registry but not actually instantiating them in a factory.
	 * @see Thread#getContextClassLoader
	 */
	public void setBeanClassLoader(ClassLoader beanClassLoader) {
		this.beanClassLoader = beanClassLoader;
	}

	public ClassLoader getBeanClassLoader() {
		return beanClassLoader;
	}

	/**
	 * Set the ResourceLoader to use for resource locations.
	 * Default is DefaultResourceLoader.
	 * <p>Can also be a ResourcePatternResolver, additionally capable
	 * of resolving resource patterns to Resource arrays.
	 * <p>Setting this to null suggests that absolute resource loading
	 * is not available for this bean definition reader.
	 * @see DefaultResourceLoader
	 * @see org.springframework.core.io.support.ResourcePatternResolver
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}


	public int loadBeanDefinitions(Resource[] resources) throws BeansException {
		int counter = 0;
		for (int i = 0; i < resources.length; i++) {
			counter += loadBeanDefinitions(resources[i]);
		}
		return counter;
	}

}
