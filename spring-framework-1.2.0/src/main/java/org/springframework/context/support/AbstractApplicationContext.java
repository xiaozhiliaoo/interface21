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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.ConfigurableBeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Abstract implementation of the ApplicationContext interface.
 * Doesn't mandate the type of storage used for configuration, but implements
 * common context functionality. Uses the Template Method design pattern,
 * requiring concrete subclasses to implement abstract methods.
 *
 * <p>In contrast to a plain bean factory, an ApplicationContext is supposed
 * to detect special beans defined in its bean factory: Therefore, this class
 * automatically registers BeanFactoryPostProcessors, BeanPostProcessors
 * and ApplicationListeners that are defined as beans in the context.
 *
 * <p>A MessageSource may also be supplied as a bean in the context, with
 * the name "messageSource"; else, message resolution is delegated to the
 * parent context. Furthermore, a multicaster for application events can
 * be supplied as "applicationEventMulticaster" bean in the context; else,
 * a SimpleApplicationEventMulticaster is used.
 *
 * <p>Implements resource loading through extending DefaultResourceLoader.
 * Therefore, treats plain resource paths as class path resources (only
 * supporting full class path resource names that include the package path,
 * e.g. "mypackage/myresource.dat").
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since January 21, 2001
 * @see #refreshBeanFactory
 * @see #getBeanFactory
 * @see BeanFactoryPostProcessor
 * @see BeanPostProcessor
 * @see ApplicationListener
 * @see #MESSAGE_SOURCE_BEAN_NAME
 * @see #APPLICATION_EVENT_MULTICASTER_BEAN_NAME
 * @see SimpleApplicationEventMulticaster
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader
		implements ConfigurableApplicationContext, DisposableBean {

	/**
	 * Name of the MessageSource bean in the factory.
	 * If none is supplied, message resolution is delegated to the parent.
	 * @see MessageSource
	 */
	public static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

	/**
	 * Name of the ApplicationEventMulticaster bean in the factory.
	 * If none is supplied, a default SimpleApplicationEventMulticaster is used.
	 * @see ApplicationEventMulticaster
	 * @see SimpleApplicationEventMulticaster
	 */
	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

	static {
		// Eagerly load the ContextClosedEvent class to avoid weird classloader issues
		// on application shutdown in WebLogic 8.1. (Reported by Dustin Woods.)
		ContextClosedEvent.class.getName();
	}


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/** Logger used by this class. Available to subclasses. */
	protected final Log logger = LogFactory.getLog(getClass());

	/** Parent context */
	private ApplicationContext parent;

	/** BeanFactoryPostProcessors to apply on refresh */
	private final List beanFactoryPostProcessors = new ArrayList();

	/** Display name */
	private String displayName = getClass().getName() + ";hashCode=" + hashCode();

	/** System time in milliseconds when this context started */
	private long startupTime;

	/** ResourcePatternResolver used by this context */
	private ResourcePatternResolver resourcePatternResolver;

	/** MessageSource we delegate our implementation of this interface to */
	private MessageSource messageSource;

	/** Helper class used in event publishing */
	private ApplicationEventMulticaster applicationEventMulticaster;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create a new AbstractApplicationContext with no parent.
	 */
	public AbstractApplicationContext() {
		this(null);
	}

	/**
	 * Create a new AbstractApplicationContext with the given parent context.
	 * @param parent the parent context
	 */
	public AbstractApplicationContext(ApplicationContext parent) {
		this.parent = parent;
		this.resourcePatternResolver = getResourcePatternResolver();
	}


	//---------------------------------------------------------------------
	// Implementation of ApplicationContext
	//---------------------------------------------------------------------

	/**
	 * Return the parent context, or null if there is no parent
	 * (that is, this context is the root of the context hierarchy).
	 */
	public ApplicationContext getParent() {
		return parent;
	}

	/**
	 * Set a friendly name for this context.
	 * Typically done during initialization of concrete context implementations.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Return a friendly name for this context.
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Return the timestamp (ms) when this context was first loaded.
	 */
	public long getStartupDate() {
		return startupTime;
	}

	/**
	 * Publish the given event to all listeners.
	 * <p>Note: Listeners get initialized after the MessageSource, to be able
	 * to access it within listener implementations. Thus, MessageSource
	 * implementation cannot publish events.
	 * @param event event to publish (may be application-specific or a
	 * standard framework event)
	 */
	public void publishEvent(ApplicationEvent event) {
		if (logger.isDebugEnabled()) {
			logger.debug("Publishing event in context [" + getDisplayName() + "]: " + event.toString());
		}
		getApplicationEventMulticaster().multicastEvent(event);
		if (this.parent != null) {
			this.parent.publishEvent(event);
		}
	}

	/**
	 * Return the internal MessageSource used by the context.
	 * @return the internal MessageSource (never null)
	 * @throws IllegalStateException if the context has not been initialized yet
	 */
	private ApplicationEventMulticaster getApplicationEventMulticaster() throws IllegalStateException {
		if (this.applicationEventMulticaster == null) {
			throw new IllegalStateException("ApplicationEventMulticaster not initialized - " +
					"call 'refresh' before multicasting events via the context: " + this);
		}
		return this.applicationEventMulticaster;
	}


	//---------------------------------------------------------------------
	// Implementation of ConfigurableApplicationContext
	//---------------------------------------------------------------------

	public void setParent(ApplicationContext parent) {
		this.parent = parent;
	}

	public void addBeanFactoryPostProcessor(BeanFactoryPostProcessor beanFactoryPostProcessor) {
		this.beanFactoryPostProcessors.add(beanFactoryPostProcessor);
	}

	/**
	 * Return the list of BeanPostProcessors that will get applied
	 * to beans created with this factory.
	 */
	public List getBeanFactoryPostProcessors() {
		return beanFactoryPostProcessors;
	}

	public void refresh() throws BeansException, IllegalStateException {
		this.startupTime = System.currentTimeMillis();

		// Tell subclass to refresh the internal bean factory.
		refreshBeanFactory();
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();

		// Populate the bean factory with context-specific resource editors.
		ConfigurableBeanFactoryUtils.registerResourceEditors(beanFactory, this);

		// Configure the bean factory with context semantics.
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
		beanFactory.ignoreDependencyType(ResourceLoader.class);
		beanFactory.ignoreDependencyType(ApplicationEventPublisher.class);
		beanFactory.ignoreDependencyType(MessageSource.class);
		beanFactory.ignoreDependencyType(ApplicationContext.class);

		// Allows post-processing of the bean factory in context subclasses.
		postProcessBeanFactory(beanFactory);

		// Invoke factory processors registered with the context instance.
		for (Iterator it = getBeanFactoryPostProcessors().iterator(); it.hasNext();) {
			BeanFactoryPostProcessor factoryProcessor = (BeanFactoryPostProcessor) it.next();
			factoryProcessor.postProcessBeanFactory(beanFactory);
		}

		if (logger.isInfoEnabled()) {
			if (getBeanDefinitionCount() == 0) {
				logger.info("No beans defined in application context [" + getDisplayName() + "]");
			}
			else {
				logger.info(getBeanDefinitionCount() + " beans defined in application context [" + getDisplayName() + "]");
			}
		}

		// Invoke factory processors registered as beans in the context.
		invokeBeanFactoryPostProcessors();

		// Register bean processors that intercept bean creation.
		registerBeanPostProcessors();

		// Initialize message source for this context.
		initMessageSource();

		// Initialize event multicaster for this context.
		initApplicationEventMulticaster();

		// Initialize other special beans in specific context subclasses.
		onRefresh();

		// Check for listener beans and register them.
		registerListeners();

		// iIstantiate singletons this late to allow them to access the message source.
		beanFactory.preInstantiateSingletons();

		// Last step: publish corresponding event.
		publishEvent(new ContextRefreshedEvent(this));
	}

	/**
	 * Return the ResourcePatternResolver to use for resolving location patterns
	 * into Resource instances. Default is PathMatchingResourcePatternResolver,
	 * supporting Ant-style location patterns.
	 * <p>Can be overridden in subclasses, for extended resolution strategies,
	 * for example in a web environment.
	 * <p><b>Do not call this when needing to resolve a location pattern.</b>
	 * Call the context's <code>getResources</code> method instead, which
	 * will delegate to the ResourcePatternResolver.
	 * @see #getResources
	 * @see PathMatchingResourcePatternResolver
	 */
	protected ResourcePatternResolver getResourcePatternResolver() {
		return new PathMatchingResourcePatternResolver(this);
	}

	/**
	 * Modify the application context's internal bean factory after its standard
	 * initialization. All bean definitions will have been loaded, but no beans
	 * will have been instantiated yet. This allows for registering special
	 * BeanPostProcessors etc in certain ApplicationContext implementations.
	 * @param beanFactory the bean factory used by the application context
	 * @throws BeansException in case of errors
	 */
	protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
	}

	/**
	 * Instantiate and invoke all registered BeanFactoryPostProcessor beans,
	 * respecting explicit order if given.
	 * Must be called before singleton instantiation.
	 */
	private void invokeBeanFactoryPostProcessors() throws BeansException {
		String[] factoryProcessorNames = getBeanNamesForType(BeanFactoryPostProcessor.class, true, false);
		// Separate between BeanFactoryPostProcessor that implement the Ordered
		// interface and those that do not.
		List orderedFactoryProcessors = new ArrayList();
		List nonOrderedFactoryProcessorNames = new ArrayList();
		for (int i = 0; i < factoryProcessorNames.length; i++) {
			if (Ordered.class.isAssignableFrom(getType(factoryProcessorNames[i]))) {
				orderedFactoryProcessors.add(getBean(factoryProcessorNames[i]));
			}
			else {
				nonOrderedFactoryProcessorNames.add(factoryProcessorNames[i]);
			}
		}
		// First, invoke the BeanFactoryPostProcessors that implement Ordered.
		Collections.sort(orderedFactoryProcessors, new OrderComparator());
		for (Iterator it = orderedFactoryProcessors.iterator(); it.hasNext();) {
			BeanFactoryPostProcessor factoryProcessor = (BeanFactoryPostProcessor) it.next();
			factoryProcessor.postProcessBeanFactory(getBeanFactory());
		}
		// Second, invoke all other BeanFactoryPostProcessors, one by one.
		for (Iterator it = nonOrderedFactoryProcessorNames.iterator(); it.hasNext();) {
			String factoryProcessorName = (String) it.next();
			((BeanFactoryPostProcessor) getBean(factoryProcessorName)).postProcessBeanFactory(getBeanFactory());
		}
	}

	/**
	 * Instantiate and invoke all registered BeanPostProcessor beans,
	 * respecting explicit order if given.
	 * <p>Must be called before any instantiation of application beans.
	 */
	private void registerBeanPostProcessors() throws BeansException {
		Map beanProcessorMap = getBeansOfType(BeanPostProcessor.class, true, false);
		List beanProcessors = new ArrayList(beanProcessorMap.values());
		Collections.sort(beanProcessors, new OrderComparator());
		for (Iterator it = beanProcessors.iterator(); it.hasNext();) {
			getBeanFactory().addBeanPostProcessor((BeanPostProcessor) it.next());
		}
	}

	/**
	 * Initialize the MessageSource.
	 * Use parent's if none defined in this context.
	 */
	private void initMessageSource() throws BeansException {
		if (containsLocalBean(MESSAGE_SOURCE_BEAN_NAME)) {
			this.messageSource = (MessageSource) getBean(MESSAGE_SOURCE_BEAN_NAME);
			// Make MessageSource aware of parent MessageSource.
			if (this.parent != null && this.messageSource instanceof HierarchicalMessageSource) {
				MessageSource parentMessageSource = getInternalParentMessageSource();
				((HierarchicalMessageSource) this.messageSource).setParentMessageSource(parentMessageSource);
			}
			if (logger.isInfoEnabled()) {
				logger.info("Using MessageSource [" + this.messageSource + "]");
			}
		}
		else {
			// Use empty MessageSource to be able to accept getMessage calls.
			DelegatingMessageSource dms = new DelegatingMessageSource();
			dms.setParentMessageSource(getInternalParentMessageSource());
			this.messageSource = dms;
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate MessageSource with name '" + MESSAGE_SOURCE_BEAN_NAME +
						"': using default [" + this.messageSource + "]");
			}
		}
	}

	/**
	 * Initialize the ApplicationEventMulticaster.
	 * Uses SimpleApplicationEventMulticaster if none defined in the context.
	 * @see SimpleApplicationEventMulticaster
	 */
	private void initApplicationEventMulticaster() throws BeansException {
		if (containsLocalBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
			this.applicationEventMulticaster =
					(ApplicationEventMulticaster) getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME);
			if (logger.isInfoEnabled()) {
				logger.info("Using ApplicationEventMulticaster [" + this.applicationEventMulticaster + "]");
			}
		}
		else {
			this.applicationEventMulticaster = new SimpleApplicationEventMulticaster();
			if (logger.isInfoEnabled()) {
				logger.info("Unable to locate ApplicationEventMulticaster with name '" +
						APPLICATION_EVENT_MULTICASTER_BEAN_NAME +
						"': using default [" + this.applicationEventMulticaster + "]");
			}
		}
	}

	/**
	 * Return whether the local bean factory of this context contains a bean
	 * of the given name, ignoring beans defined in ancestor contexts.
	 * <p>Needs to check both bean definitions and manually registered singletons.
	 * We cannot use <code>containsBean</code> here, as we do not want a bean
	 * from an ancestor bean factory.
	 */
	protected final boolean containsLocalBean(String beanName) {
		return (containsBeanDefinition(beanName) || getBeanFactory().containsSingleton(beanName));
	}

	/**
	 * Template method which can be overridden to add context-specific refresh work.
	 * Called on initialization of special beans, before instantiation of singletons.
	 * @throws BeansException in case of errors during refresh
	 * @see #refresh
	 */
	protected void onRefresh() throws BeansException {
		// For subclasses: do nothing by default.
	}

	/**
	 * Add beans that implement ApplicationListener as listeners.
	 * Doesn't affect other listeners, which can be added without being beans.
	 */
	private void registerListeners() throws BeansException {
		Collection listeners = getBeansOfType(ApplicationListener.class, true, false).values();
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			addListener((ApplicationListener) it.next());
		}
	}

	/**
	 * Subclasses can invoke this method to register a listener.
	 * Any beans in the context that are listeners are automatically added.
	 * @param listener the listener to register
	 */
	protected void addListener(ApplicationListener listener) {
		getApplicationEventMulticaster().addApplicationListener(listener);
	}

	/**
	 * Publishes a ContextClosedEvent and destroys the singletons
	 * in the bean factory of this application context.
	 * @see ContextClosedEvent
	 */
	public void close() {
		if (logger.isInfoEnabled()) {
			logger.info("Closing application context [" + getDisplayName() + "]");
		}

		try {
			// Publish shutdown event.
			publishEvent(new ContextClosedEvent(this));
		}
		finally {
			// Destroy all cached singletons in this context, invoking
			// DisposableBean.destroy and/or the specified "destroy-method".
			ConfigurableListableBeanFactory beanFactory = getBeanFactory();
			if (beanFactory != null) {
				beanFactory.destroySingletons();
			}
		}
	}

	/**
	 * DisposableBean callback for destruction of this instance.
	 * Only called when the ApplicationContext itself is running
	 * as a bean in another BeanFactory or ApplicationContext,
	 * which is rather unusual.
	 * <p>The <code>close</code> method is the native way to
	 * shut down an ApplicationContext.
	 * @see #close
	 * @see org.springframework.beans.factory.access.SingletonBeanFactoryLocator
	 */
	public void destroy() {
		close();
	}


	//---------------------------------------------------------------------
	// Implementation of BeanFactory
	//---------------------------------------------------------------------

	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	public Object getBean(String name, Class requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().isSingleton(name);
	}

	public Class getType(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getType(name);
	}

	public String[] getAliases(String name) throws NoSuchBeanDefinitionException {
		return getBeanFactory().getAliases(name);
	}


	//---------------------------------------------------------------------
	// Implementation of ListableBeanFactory
	//---------------------------------------------------------------------

	public boolean containsBeanDefinition(String name) {
		return getBeanFactory().containsBeanDefinition(name);
	}

	public int getBeanDefinitionCount() {
		return getBeanFactory().getBeanDefinitionCount();
	}

	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	public String[] getBeanDefinitionNames(Class type) {
		return getBeanFactory().getBeanDefinitionNames(type);
	}

	public String[] getBeanNamesForType(Class type) {
		return getBeanFactory().getBeanNamesForType(type);
	}

	public String[] getBeanNamesForType(Class type, boolean includePrototypes, boolean includeFactoryBeans) {
		return getBeanFactory().getBeanNamesForType(type, includePrototypes, includeFactoryBeans);
	}

	public Map getBeansOfType(Class type) throws BeansException {
		return getBeanFactory().getBeansOfType(type);
	}

	public Map getBeansOfType(Class type, boolean includePrototypes, boolean includeFactoryBeans)
			throws BeansException {
		return getBeanFactory().getBeansOfType(type, includePrototypes, includeFactoryBeans);
	}


	//---------------------------------------------------------------------
	// Implementation of HierarchicalBeanFactory
	//---------------------------------------------------------------------

	public BeanFactory getParentBeanFactory() {
		return getParent();
	}

	/**
	 * Return the internal bean factory of the parent context if it implements
	 * ConfigurableApplicationContext; else, return the parent context itself.
	 * @see ConfigurableApplicationContext#getBeanFactory
	 */
	protected BeanFactory getInternalParentBeanFactory() {
		return (getParent() instanceof ConfigurableApplicationContext) ?
				((ConfigurableApplicationContext) getParent()).getBeanFactory() : (BeanFactory) getParent();
	}


	//---------------------------------------------------------------------
	// Implementation of MessageSource
	//---------------------------------------------------------------------

	public String getMessage(String code, Object args[], String defaultMessage, Locale locale) {
		return getMessageSource().getMessage(code, args, defaultMessage, locale);
	}

	public String getMessage(String code, Object args[], Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(code, args, locale);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		return getMessageSource().getMessage(resolvable, locale);
	}

	/**
	 * Return the internal MessageSource used by the context.
	 * @return the internal MessageSource (never null)
	 * @throws IllegalStateException if the context has not been initialized yet
	 */
	private MessageSource getMessageSource() throws IllegalStateException {
		if (this.messageSource == null) {
			throw new IllegalStateException("MessageSource not initialized - " +
					"call 'refresh' before accessing messages via the context: " + this);
		}
		return this.messageSource;
	}

	/**
	 * Return the internal message source of the parent context if it is an
	 * AbstractApplicationContext too; else, return the parent context itself.
	 */
	protected MessageSource getInternalParentMessageSource() {
		return (getParent() instanceof AbstractApplicationContext) ?
		    ((AbstractApplicationContext) getParent()).messageSource : getParent();
	}


	//---------------------------------------------------------------------
	// Implementation of ResourcePatternResolver
	//---------------------------------------------------------------------

	public Resource[] getResources(String locationPattern) throws IOException {
		return this.resourcePatternResolver.getResources(locationPattern);
	}


	//---------------------------------------------------------------------
	// Abstract methods that must be implemented by subclasses
	//---------------------------------------------------------------------

	/**
	 * Subclasses must implement this method to perform the actual configuration load.
	 * The method is invoked by refresh before any other initialization work.
	 * <p>A subclass will either create a new bean factory and hold a reference to it,
	 * or return a single bean factory instance that it holds. In the latter case, it will
	 * usually throw an IllegalStateException if refreshing the context more than once.
	 * @throws BeansException if initialization of the bean factory failed
	 * @throws IllegalStateException if already initialized and multiple refresh
	 * attempts are not supported
	 * @see #refresh
	 */
	protected abstract void refreshBeanFactory() throws BeansException, IllegalStateException;

	/**
	 * Subclasses must return their internal bean factory here.
	 * They should implement the lookup efficiently, so that it can be called
	 * repeatedly without a performance penalty.
	 * @return this application context's internal bean factory
	 * @throws IllegalStateException if the context does not hold an internal
	 * bean factory yet (usually if <code>refresh</code> has never been called)
	 * @see #refresh
	 */
	public abstract ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException;


	/**
	 * Return information about this context.
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append(": ");
		sb.append("display name [").append(this.displayName).append("]; ");
		sb.append("startup date [").append(new Date(this.startupTime)).append("]; ");
		if (this.parent == null) {
			sb.append("root of context hierarchy");
		}
		else {
			sb.append("child of [").append(this.parent).append(']');
		}
		return sb.toString();
	}

}
