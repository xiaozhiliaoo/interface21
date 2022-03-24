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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;

/**
 * BeanPostProcessor implementation that passes the ApplicationContext to
 * beans that implement the ResourceLoaderAware, ApplicationEventPublisherAware,
 * MessageSourceAware and/or ApplicationContextAware interfaces.
 * If all of them are implemented, they are satisfied in the given order.
 *
 * <p>Application contexts will automatically register this with their
 * underlying bean factory. Applications do not use this directly.
 *
 * @author Juergen Hoeller
 * @since 10.10.2003
 * @see ResourceLoaderAware
 * @see ApplicationEventPublisherAware
 * @see MessageSourceAware
 * @see ApplicationContextAware
 * @see AbstractApplicationContext#refresh
 */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

	protected final Log logger = LogFactory.getLog(getClass());

	private final ApplicationContext applicationContext;

	/**
	 * Create a new ApplicationContextAwareProcessor for the given context.
	 */
	public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ResourceLoaderAware) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking setResourceLoader on ResourceLoaderAware bean '" + beanName + "'");
			}
			((ResourceLoaderAware) bean).setResourceLoader(this.applicationContext);
		}
		if (bean instanceof ApplicationEventPublisherAware) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking setApplicationEventPublisher on ApplicationEventPublisherAware bean '" +
						beanName + "'");
			}
			((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(this.applicationContext);
		}
		if (bean instanceof MessageSourceAware) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking setMessageSource on MessageSourceAware bean '" + beanName + "'");
			}
			((MessageSourceAware) bean).setMessageSource(this.applicationContext);
		}
		if (bean instanceof ApplicationContextAware) {
			if (logger.isDebugEnabled()) {
				logger.debug("Invoking setApplicationContext on ApplicationContextAware bean '" + beanName + "'");
			}
			((ApplicationContextAware) bean).setApplicationContext(this.applicationContext);
		}
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String name) {
		return bean;
	}

}
