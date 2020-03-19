package com.interface21.jms;


import javax.jms.MessageListener;
import javax.jms.TopicConnection;

import com.interface21.beans.BeanWrapper;
import com.interface21.beans.BeanWrapperImpl;
import com.interface21.beans.FatalBeanException;
import com.interface21.beans.factory.support.AbstractRootBeanDefinition;

/** 
* Example:
* we set the bean class name which will be exposed,
* and then take another property to be the concrete
* class we'll construct and proxy to.
* 
* listenerBean property must be set
* topicName: JNDI name
* topicConnectionFactory: JNDI name
* messageSelector? optional
*/
public class JmsListenerBeanDefinition extends AbstractRootBeanDefinition {
	
	private String topicConnectionFactoryJndiName;
	
	private TopicConnection topicConnection;
	
	private String topicName;
	
	private String messageSelector;

	public void setListenerBean(String name) throws Exception {
		if (!isSingleton())
			throw new Exception("MessageListener custom definitions must be singletons, not prototypes");
			
		// IF WE DO THIS IT COMES FROM EJB CLASS LOADER: not found
		//setBeanClassName(name); 
		
		setBeanClass(Class.forName(name));
		
		// Check it's an interface
		if (!MessageListener.class.isAssignableFrom(getBeanClass()))
			throw new Exception("listenerBean must implement javax.jms.MessageLister: " + name + " doesn't");
	}
	
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	
	public String getTopicName() {
		return topicName;
	}
	
	public void setTopicConnectionFactoryName(String topicConnectionFactoryJndiName) {
		this.topicConnectionFactoryJndiName = topicConnectionFactoryJndiName;
	}
	
	public String getTopicConnectionFactoryName() {
		return topicConnectionFactoryJndiName;
	}
	
	/**
	 * Gets the messageSelector.
	 * @return Returns a String
	 */
	public String getMessageSelector() {
		return messageSelector;
	}

	/**
	 * Sets the messageSelector.
	 * @param messageSelector The messageSelector to set
	 */
	public void setMessageSelector(String messageSelector) {
		this.messageSelector = messageSelector;
	}


	/**
	 * Subclasses may override this, to create bean
	 * wrappers differently or perform custom preprocessing
	 */
	protected BeanWrapper newBeanWrapper() {
		
		if (this.topicConnectionFactoryJndiName == null)
			throw new FatalBeanException("topicConnectoryFactoryName must be set in JmsListenerBeanDefinition", null);
		if (this.topicName == null)
			throw new FatalBeanException("TopicName must be set in JmsListenerBeanDefinition", null);
		
		MessageListener listener = null;
		try {
			 listener = (MessageListener) getBeanClass().newInstance();
		} 
		catch(InstantiationException ex) {
			throw new FatalBeanException("Cant instantiate MessageListener bean " + getBeanClass(), ex);
		}
		catch(IllegalAccessException ex) {
			throw new FatalBeanException("Cant instantiate MessageListener bean " + getBeanClass(), ex);
		}
		this.topicConnection = new JmsTemplate(this.topicConnectionFactoryJndiName).subscribeToTopicNonDurable(this.topicName, listener, this.messageSelector);
		logger.config("Subscribed [" + listener + "] to topic with name '" + topicName + "'");
		
		return new BeanWrapperImpl(listener);
		
	}
	
	
	// OR CONTEXT SHUT DOWN!?
	
	/**
	 * @see Object#finalize()
	 */
	protected void finalize() throws Throwable {
		this.topicConnection.close();
		super.finalize();
	}

} 	// class JmsListenerBeanDefinition