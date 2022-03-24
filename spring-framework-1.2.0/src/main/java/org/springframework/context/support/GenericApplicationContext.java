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

package org.springframework.context.support;

import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Generic ApplicationContext implementation that holds a single internal
 * DefaultListableBeanFactory instance and does not assume a specific bean
 * definition format. Implements the BeanDefinitionRegistry interface to
 * allow for applying any bean definition readers to it.
 *
 * <p>Typical usage is to register a variety of bean definitions via the
 * BeanDefinitionRegistry interface and then call <code>refresh</code> to initialize
 * those beans with application context semantics (handling ApplicationContextAware,
 * auto-detecting BeanFactoryPostProcessors, etc).
 *
 * <p>In contrast to other ApplicationContext implementations that create a new
 * internal BeanFactory instance for each refresh, the internal BeanFactory of
 * this context is available right from the start, to be able to register bean
 * definitions on it. <code>refresh</code> may only be called once.
 *
 * <p>Usage example:
 *
 * <pre>
 * GenericApplicationContext ctx = new GenericApplicationContext();
 * XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(ctx);
 * xmlReader.loadBeanDefinitions(new ClassPathResource("applicationContext.xml"));
 * PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(ctx);
 * propReader.loadBeanDefinitions(new ClassPathResource("otherBeans.properties"));
 * ctx.refresh();
 *
 * MyBean myBean = (MyBean) ctx.getBean("myBean");
 * ...</pre>
 *
 * For the typical case of XML bean definitions, simply use ClassPathXmlApplicationContext
 * or FileSystemXmlApplicationContext, which are easier to set up - but less flexible,
 * as you can just use standard resource locations for XML bean definitions, rather than
 * mixing arbitrary bean definition formats. The equivalent in a web environment is
 * XmlWebApplicationContext, implementing the extended WebApplicationContext interface.
 *
 * <p>For custom application context implementations that are supposed to read
 * special bean definition formats in a refreshable manner, consider deriving
 * from the AbstractRefreshableApplicationContext base class. For a corresponding
 * base class that pre-implements the extended WebApplicationContext interface,
 * consider AbstractRefreshableWebApplicationContext.
 *
 * @author Juergen Hoeller
 * @since 1.1.2
 * @see #registerBeanDefinition
 * @see #refresh
 * @see BeanDefinitionRegistry
 * @see DefaultListableBeanFactory
 * @see AbstractRefreshableApplicationContext
 * @see ClassPathXmlApplicationContext
 * @see FileSystemXmlApplicationContext
 * @see org.springframework.web.context.WebApplicationContext
 * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext
 * @see org.springframework.web.context.support.XmlWebApplicationContext
 */
public class GenericApplicationContext extends AbstractApplicationContext implements BeanDefinitionRegistry {

	private final DefaultListableBeanFactory beanFactory;

	private ResourceLoader resourceLoader;

	private boolean refreshed = false;


	/**
	 * Create a new GenericApplicationContext.
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext() {
		this(new DefaultListableBeanFactory());
	}

	/**
	 * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Create a new GenericApplicationContext with the given parent.
	 * @param parent the parent application context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(ApplicationContext parent) {
		this(new DefaultListableBeanFactory(), parent);
	}

	/**
	 * Create a new GenericApplicationContext with the given DefaultListableBeanFactory.
	 * @param beanFactory the DefaultListableBeanFactory instance to use for this context
	 * @param parent the parent application context
	 * @see #registerBeanDefinition
	 * @see #refresh
	 */
	public GenericApplicationContext(DefaultListableBeanFactory beanFactory, ApplicationContext parent) {
		this.beanFactory = beanFactory;
		setParent(parent);
	}


	/**
	 * Set the parent of this application context, also setting
	 * the parent of the internal BeanFactory accordingly.
	 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#setParentBeanFactory
	 */
	public void setParent(ApplicationContext parent) {
		super.setParent(parent);
		this.beanFactory.setParentBeanFactory(getInternalParentBeanFactory());
	}

	/**
	 * Set a ResourceLoader to use for this context. If set, the context will
	 * delegate all <code>getResource</code> calls to the given ResourceLoader.
	 * If not set, default resource loading will apply.
	 * <p>The main reason to specify a custom ResourceLoader is to resolve
	 * resource paths (withour URL prefix) in a specific fashion.
	 * The default behavior is to resolve such paths as class path locations.
	 * To resolve resource paths as file system locations, specify a
	 * FileSystemResourceLoader here.
	 * <p>You can also pass in a full ResourcePatternResolver, which will
	 * be autodetected by the context and used for <code>getResources</code>
	 * calls as well. Else, default resource pattern matching will apply.
	 * @see #getResource
	 * @see org.springframework.core.io.DefaultResourceLoader
	 * @see org.springframework.core.io.FileSystemResourceLoader
	 * @see ResourcePatternResolver
	 * @see #getResources
	 */
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}


	/**
	 * This implementation delegates to this context's ResourceLoader if set,
	 * falling back to the default superclass behavior else.
	 * @see #setResourceLoader
	 */
	public Resource getResource(String location) {
		if (this.resourceLoader != null) {
			return this.resourceLoader.getResource(location);
		}
		return super.getResource(location);
	}

	/**
	 * This implementation delegates to this context's ResourceLoader if it
	 * implements the ResourcePatternResolver interface, falling back to the
	 * default superclass behavior else.
	 * @see #setResourceLoader
	 */
	public Resource[] getResources(String locationPattern) throws IOException {
		if (this.resourceLoader instanceof ResourcePatternResolver) {
			return ((ResourcePatternResolver) this.resourceLoader).getResources(locationPattern);
		}
		return super.getResources(locationPattern);
	}


	//---------------------------------------------------------------------
	// Implementations of AbstractApplicationContext's template methods
	//---------------------------------------------------------------------

	/**
	 * Do nothing: We hold a single internal BeanFactory and rely on callers
	 * to register beans through our public methods (or the BeanFactory's).
	 * @see #registerBeanDefinition
	 */
	protected void refreshBeanFactory() throws IllegalStateException {
		if (this.refreshed) {
			throw new IllegalStateException("Multiple refreshs not supported: just call 'refresh' once");
		}
		this.refreshed = true;
	}

	/**
	 * Return the single internal BeanFactory held by this context.
	 */
	public ConfigurableListableBeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	/**
	 * Return the underlying bean factory of this context,
	 * available for registering bean definitions.
	 * <p><b>NOTE:</b> You need to call <code>refresh</code> to initialize the
	 * bean factory and its contained beans with application context semantics
	 * (auto-detecting BeanFactoryPostProcessors, etc)
	 * @see #refresh
	 */
	public DefaultListableBeanFactory getDefaultListableBeanFactory() {
		return this.beanFactory;
	}


	//---------------------------------------------------------------------
	// Implementation of BeanDefinitionRegistry
	//---------------------------------------------------------------------

	public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
		return this.beanFactory.getBeanDefinition(beanName);
	}

	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeansException {
		this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
	}

	public void registerAlias(String beanName, String alias) throws BeansException {
		this.beanFactory.registerAlias(beanName, alias);
	}

}
