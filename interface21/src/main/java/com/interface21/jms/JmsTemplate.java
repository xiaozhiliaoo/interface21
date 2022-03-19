package com.interface21.jms;

import com.interface21.jndi.JndiServices;
import java14.java.util.logging.Level;
import java14.java.util.logging.Logger;

import javax.jms.*;
import javax.naming.NamingException;

public class JmsTemplate {
	
	private static final String PREFIX = "java:comp/env/";
	
	
	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private JndiServices jndiServices = new JndiServices();
	
	private TopicConnectionFactory topicConnectionFactory;
	
	private String connectionFactoryName = "jms/TopicFactory";//"ConnectionFactory";
	
	
	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------
	/**
	 * Create a new JmsTemplate
	 * @param connectionFactoryName name of the connection factory: e.g. jms/TopicFactory in JBoss.
	 * java:comp/env/ will be prepended if it doesn't already begin with it.
	 */
	public JmsTemplate(String connectionFactoryName) throws JmsException {
		if (!connectionFactoryName.startsWith(PREFIX))
			connectionFactoryName = PREFIX + connectionFactoryName;
		this.connectionFactoryName = connectionFactoryName;
		
		// 1: Lookup ConnectionFactory via JNDI
		try {
			this.topicConnectionFactory = (TopicConnectionFactory) jndiServices.lookup(this.connectionFactoryName);
			logger.config("Cached TopicConnectionFactory");
			//javax.jms.TopicConnectionFactory
		}
		catch (NamingException ex) {
			throw new JmsException("Couldn't lookup connection factory [" + this.connectionFactoryName + "]", ex);
		}
	}

	/**
	 * Constructor for JmsUtils.
	 * @param topicName like "topic/referencedata"
	 * If java:comp/env not found, will be added
	 */
	public void publish(String topicName, PubSubMessageCreator pubSubMessageCreator) throws JmsException {
		if (!topicName.startsWith(PREFIX))
			topicName = PREFIX + topicName;
			
		TopicConnection topicConnection = null;
		try {
			// Use ConnectionFactory to create JMS connection
			topicConnection = topicConnectionFactory.createTopicConnection();

			// Use Connection to create session
			// first param is Transacted
			TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// Lookup Desintation (topic) via JNDI
			Topic topic = (Topic) jndiServices.lookup( topicName);
			logger.info("Looked up topic with name '" + topicName + "'");
			//session.createt

			// Create a Message Producer 
			TopicPublisher publisher = session.createPublisher(topic);

			// Create a text message, and publish it
			Message message = pubSubMessageCreator.createMessage(session);
			logger.info("Message created was [" + message + "]");
			publisher.publish(message);

			//session.commit();
		//	session.close();
			logger.info("Message published OK");
		}
		catch (NamingException ex) {
			throw new JmsException("Couldn't get topic name [" + topicName + "]", ex);
		}
		catch (JMSException ex) {
			throw new JmsException("Couldn't get connection factory or topic", ex);
		}
		finally {
			if (topicConnection != null) {
				try {
					topicConnection.close();
				}
				catch (JMSException ex) {
					logger.logp(Level.WARNING, "com.interface21.jms.JmsTemplate", "publish", "Failed to close topic", ex);
				}
			}
		}
	}	// publish
	
	/**
	 * Create a nondurable subscription
	 * @param messageSelector if null will receive all
	 */
	public TopicConnection subscribeToTopicNonDurable(String topicName, MessageListener listener, String messageSelector) throws JmsException {
		if (!topicName.startsWith(PREFIX))
			topicName = PREFIX + topicName;
			
		TopicConnection topicConnection = null;
		try {
			// Use ConnectionFactory to create JMS connection
			topicConnection = topicConnectionFactory.createTopicConnection();

			// Use Connection to create session
			// first param is Transacted
			TopicSession session = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

			// Lookup Desintation (topic) via JNDI
			Topic topic = (Topic) jndiServices.lookup( topicName);
			logger.info("Looked up topic with name '" + topicName + "'");
			//session.createt

			TopicSubscriber topicSubscriber = session.createSubscriber(topic, messageSelector, false);
			topicSubscriber.setMessageListener(listener);
			
			topicConnection.start();
			

			//session.commit();
			//session.close();
			logger.info("MessageListener [" + listener + "] subscribed OK to topic with name '" + topicName + "'");
			
			return topicConnection;
		}
		catch (NamingException ex) {
			throw new JmsException("Couldn't get topic name [" + topicName + "]", ex);
		}
		catch (JMSException ex) {
			throw new JmsException("Couldn't get connection factory or topic", ex);
		}
		// IF WE CLOSE TOPIC CONNECTION IT WE NEVER RECEIVE ANY MESSAGES
//		finally {
//			if (topicConnection != null) {
//				try {
//					topicConnection.close();
//				}
//				catch (JMSException ex) {
//					logger.logp(Level.WARNING, "com.interface21.jms.JmsTemplate", "publish", "Failed to close topic", ex);
//				}
//			}
//		}
	}
	
	
	/**
	 * Callback interface to be implemented by objects that
	 * can create messages for publication by this class.
	 */
	public interface PubSubMessageCreator {
		Message createMessage(TopicSession topicSession) throws JMSException;
	}
	
	

}

