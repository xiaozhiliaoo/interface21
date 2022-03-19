
package com.interface21.beans.factory;

import com.interface21.beans.BeansException;

/**
 * Interface to be implemented by objects that hold a number of
 * bean definitions, each uniquely identified by a String name. An independent
 * instance of any of these objects can be obtained (the Prototype
 * design pattern), or a single shared instance can be obtained 
 * (a superior alternative to the Singleton design pattern).
 * <p/>The point of this approach is that the BeanFactory is a central
 * registry of application components(应用组件注册中心), and centralizes the configuring
 * of application components (no more do individual objects need to
 * read properties files, for example).
 * <br/>Normally the BeanFactory will load bean definitions stored in
 * a configuration source (such as an XML document),
 * and uses the com.interface21.beans package to configure the beans. However,
 * an implementation could simply return Java objects it creates as
 * necessary directly in Java code.
 * @author  Rod Johnson
 * @since 13 April 2001
 * @version $RevisionId$
 */
public interface BeanFactory {

	
	/** 
	 * This method allows a bean factory to be used as a replacement for
	 * the Singleton design pattern, by always returning the 
	 * same instance of the given bean name.
	 * OR INDEPENDENT
	 * DEPENDS ION DEFINITION
	 * Note that callers should retian references to returned objects. There is
	 * no guarantee that this method will be implemented to be efficient (for example,
	 * it will probably be synchronized).
	 * @param name name of the bean to return
	 * @return the shared instance of the bean
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 */
    Object getBean(String name) throws BeansException;
	
	/**
	 * Return a shared instance of the given bean. Analogous to getBeanInstance(name, requiredType).
	 * @name name of the instance to return
	 * @param requiredType type the bean must match
	 * @return the shared instance of the given bean
	 * @throws BeanNotOfRequiredTypeException if the bean is not of the required type
	 * @throws NoSuchBeanDefinitionException if there's no such bean definition
	 */
	Object getBean(String name, Class requiredType) throws BeansException; 		 
    
}	// interface BeanFactory

