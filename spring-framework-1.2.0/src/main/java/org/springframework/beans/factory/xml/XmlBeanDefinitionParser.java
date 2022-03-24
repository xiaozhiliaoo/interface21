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

package org.springframework.beans.factory.xml;

import org.w3c.dom.Document;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.core.io.Resource;

/**
 * Strategy interface for parsing XML bean definitions.
 * Used by XmlBeanDefinitionReader for actually parsing a DOM document.
 *
 * <p>Instantiated per document to parse: Implementations can hold state in
 * instance variables during the execution of the registerBeanDefinitions
 * method, for example global settings that are defined for all bean
 * definitions in the document.
 *
 * @author Juergen Hoeller
 * @since 18.12.2003
 * @see XmlBeanDefinitionReader#setParserClass
 */
public interface XmlBeanDefinitionParser {

	/**
	 * Parse bean definitions from the given DOM document,
	 * and register them with the given bean factory.
	 * @param reader the bean definition reader, containing the bean factory
	 * to work on and the bean class loader to use. Can also be used to load
	 * further bean definition files referenced by the given document.
	 * @param doc the DOM document
	 * @param resource descriptor of the original XML resource
	 * (useful for displaying parse errors)
	 * @throws BeanDefinitionStoreException in case of parsing errors
	 */
	int registerBeanDefinitions(BeanDefinitionReader reader, Document doc, Resource resource)
			throws BeanDefinitionStoreException;

}
