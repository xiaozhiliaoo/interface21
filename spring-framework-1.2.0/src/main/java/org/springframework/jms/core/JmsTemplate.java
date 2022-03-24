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

package org.springframework.jms.core;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TransactionInProgressException;

import org.springframework.core.Constants;
import org.springframework.jms.JmsException;
import org.springframework.jms.connection.ConnectionHolder;
import org.springframework.jms.support.JmsAccessor;
import org.springframework.jms.support.JmsUtils;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helper class that simplifies JMS access code. This class requires a
 * JMS >= 1.1 provider, because it builds on the domain-independent API.
 * <b>Use the {@link JmsTemplate102 JmsTemplate102} subclass for
 * JMS 1.0.2 providers.</b>
 *
 * <p>If you want to use dynamic destination creation, you must specify
 * the type of JMS destination to create, using the "pubSubDomain" property.
 * For other operations, this is not necessary, in contrast to when working
 * with JmsTemplate102. Point-to-Point (Queues) is the default domain.
 *
 * <p>Default settings for JMS sessions are not transacted and auto-acknowledge.
 * As defined by the J2EE specification, the transaction and acknowledgement
 * parameters are ignored when a JMS Session is created inside an active
 * transaction, no matter if a JTA transaction or a Spring-managed transaction.
 *
 * <p>This template uses a DynamicDestinationResolver and a SimpleMessageConverter
 * as default strategies for resolving a destination name or converting a message,
 * respectively.
 *
 * @author Mark Pollack
 * @author Juergen Hoeller
 * @since 1.1
 * @see #setConnectionFactory
 * @see #setPubSubDomain
 * @see JmsTemplate102
 * @see #setDestinationResolver
 * @see #setMessageConverter
 * @see DynamicDestinationResolver
 * @see SimpleMessageConverter
 * @see Destination
 * @see Session
 * @see MessageProducer
 * @see MessageConsumer
 */
public class JmsTemplate extends JmsAccessor implements JmsOperations {

	/**
	 * Default timeout for receive operations:
	 * -1 indicates a blocking receive without timeout.
	 */
	public static final long DEFAULT_RECEIVE_TIMEOUT = -1;

	/** Constants instance for javax.jms.Session */
	private static final Constants constants = new Constants(Session.class);


	private boolean pubSubDomain = false;

	private boolean sessionTransacted = false;

	private int sessionAcknowledgeMode = Session.AUTO_ACKNOWLEDGE;


	private Object defaultDestination;

	private DestinationResolver destinationResolver;

	private MessageConverter messageConverter;


	private boolean messageIdEnabled = true;

	private boolean messageTimestampEnabled = true;

	private boolean pubSubNoLocal = false;

	private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;


	private boolean explicitQosEnabled = false;

	private int deliveryMode = Message.DEFAULT_DELIVERY_MODE;

	private int priority = Message.DEFAULT_PRIORITY;

	private long timeToLive = Message.DEFAULT_TIME_TO_LIVE;


	/**
	 * Create a new JmsTemplate for bean-style usage.
	 * <p>Note: The ConnectionFactory has to be set before using the instance.
	 * This constructor can be used to prepare a JmsTemplate via a BeanFactory,
	 * typically setting the ConnectionFactory via setConnectionFactory.
	 * @see #setConnectionFactory
	 */
	public JmsTemplate() {
		initDefaultStrategies();
	}

	/**
	 * Create a new JmsTemplate, given a ConnectionFactory.
	 * @param connectionFactory the ConnectionFactory to obtain connections from
	 */
	public JmsTemplate(ConnectionFactory connectionFactory) {
		this();
		setConnectionFactory(connectionFactory);
		afterPropertiesSet();
	}

	/**
	 * Initialize the default implementations for the template's strategies:
	 * DynamicDestinationResolver and SimpleMessageConverter.
	 * @see #setDestinationResolver
	 * @see #setMessageConverter
	 * @see DynamicDestinationResolver
	 * @see SimpleMessageConverter
	 */
	protected void initDefaultStrategies() {
		setDestinationResolver(new DynamicDestinationResolver());
		setMessageConverter(new SimpleMessageConverter());
	}


	/**
	 * Configure the JmsTemplate with knowledge of the JMS domain used.
	 * Default is Point-to-Point (Queues).
	 * <p>For JmsTemplate102, this tells the JMS provider which class hierarchy to use
	 * in the implementation of the various execute methods. For JmsTemplate itself,
	 * it does not affect execute methods. In both implementations, it tells what type
	 * of destination to create if dynamic destinations are enabled.
	 * @param pubSubDomain true for Publish/Subscribe domain (Topics),
	 * false for Point-to-Point domain (Queues)
	 * @see #setDestinationResolver
	 */
	public void setPubSubDomain(boolean pubSubDomain) {
		this.pubSubDomain = pubSubDomain;
	}

	/**
	 * Return whether the Publish/Subscribe domain (Topics) is used.
	 * Otherwise, the Point-to-Point domain (Queues) is used.
	 */
	public boolean isPubSubDomain() {
		return pubSubDomain;
	}

	/**
	 * Set the transaction mode that is used when creating a JMS session to send a message.
	 * <p>Note that that within a JTA transaction, the parameters to
	 * create<Queue|Topic>Session(boolean transacted, int acknowledgeMode) method are not
	 * taken into account. Depending on the J2EE transaction context, the container
	 * makes its own decisions on these values. See section 17.3.5 of the EJB Spec.
	 * @param sessionTransacted the transaction mode
	 * @see Connection#createSession
	 */
	public void setSessionTransacted(boolean sessionTransacted) {
		this.sessionTransacted = sessionTransacted;
	}

	/**
	 * Return whether the JMS sessions used for sending a message are transacted.
	 */
	public boolean isSessionTransacted() {
		return sessionTransacted;
	}

	/**
	 * Set the JMS acknowledgement mode by the name of the corresponding constant
	 * in the JMS Session interface, e.g. "CLIENT_ACKNOWLEDGE".
	 * @param constantName name of the constant
	 * @see Session#AUTO_ACKNOWLEDGE
	 * @see Session#CLIENT_ACKNOWLEDGE
	 * @see Session#DUPS_OK_ACKNOWLEDGE
	 * @see Connection#createSession
	 */
	public void setSessionAcknowledgeModeName(String constantName) {
		setSessionAcknowledgeMode(constants.asNumber(constantName).intValue());
	}

	/**
	 * Set the JMS acknowledgement mode that is used when creating a JMS session to send
	 * a message. Vendor extensions to the acknowledgment mode can be set here as well.
	 * <p>Note that that inside an EJB the parameters to
	 * create(Queue|Topic)Session(boolean transacted, int acknowledgeMode) method are not
	 * taken into account. Depending on the transaction context in the EJB, the container
	 * makes its own decisions on these values. See section 17.3.5 of the EJB Spec.
	 * @param sessionAcknowledgeMode the acknowledgement mode
	 * @see Session#AUTO_ACKNOWLEDGE
	 * @see Session#CLIENT_ACKNOWLEDGE
	 * @see Session#DUPS_OK_ACKNOWLEDGE
	 * @see Connection#createSession
	 */
	public void setSessionAcknowledgeMode(int sessionAcknowledgeMode) {
		this.sessionAcknowledgeMode = sessionAcknowledgeMode;
	}

	/**
	 * Return the acknowledgement mode for JMS sessions.
	 */
	public int getSessionAcknowledgeMode() {
		return sessionAcknowledgeMode;
	}


	/**
	 * Set the destination to be used on send operations that do not
	 * have a destination parameter.
	 * <p>Alternatively, specify a "defaultDestinationName", to be
	 * dynamically resolved via the DestinationResolver.
	 * @see #send(MessageCreator)
	 * @see #convertAndSend(Object)
	 * @see #convertAndSend(Object, MessagePostProcessor)
	 * @see #setDefaultDestinationName(String)
	 */
	public void setDefaultDestination(Destination destination) {
		this.defaultDestination = destination;
	}

	/**
	 * Return the destination to be used on send operations that do not
	 * have a destination parameter.
	 */
	public Destination getDefaultDestination() {
		return (this.defaultDestination instanceof Destination ? (Destination) this.defaultDestination : null);
	}

	/**
	 * Set the destination name to be used on send operations that do not
	 * have a destination parameter. The specified name will be dynamically
	 * resolved via the DestinationResolver.
	 * <p>Alternatively, specify a JMS Destination object as "defaultDestination".
	 * @see #send(MessageCreator)
	 * @see #convertAndSend(Object)
	 * @see #convertAndSend(Object, MessagePostProcessor)
	 * @see #setDestinationResolver
	 * @see #setDefaultDestination(Destination)
	 */
	public void setDefaultDestinationName(String defaultDestinationName) {
		this.defaultDestination = defaultDestinationName;
	}

	/**
	 * Return the destination name to be used on send operations that do not
	 * have a destination parameter.
	 */
	public String getDefaultDestinationName() {
		return (this.defaultDestination instanceof String ? (String) this.defaultDestination : null);
	}

	/**
	 * Set the destination resolver for this template. Used to resolve
	 * destination names and to support dynamic destination functionality.
	 * <p>The default resolver is a DynamicDestinationResolver. Specify a
	 * JndiDestinationResolver for resolving destination names as JNDI locations.
	 * @see DynamicDestinationResolver
	 * @see org.springframework.jms.support.destination.JndiDestinationResolver
	 */
	public void setDestinationResolver(DestinationResolver destinationResolver) {
		this.destinationResolver = destinationResolver;
	}

	/**
	 * Get the destination resolver for this template.
	 */
	public DestinationResolver getDestinationResolver() {
		return destinationResolver;
	}

	/**
	 * Set the message converter for this template. Used to resolve
	 * Object parameters to convertAndSend methods and Object results
	 * from receiveAndConvert methods.
	 * <p>The default converter is a SimpleMessageConverter, which is able
	 * to handle BytesMessages, TextMessages and ObjectMessages.
	 * @see #convertAndSend
	 * @see #receiveAndConvert
	 * @see SimpleMessageConverter
	 */
	public void setMessageConverter(MessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	/**
	 * Return the message converter for this template.
	 */
	public MessageConverter getMessageConverter() {
		return messageConverter;
	}


	/**
	 * Set whether message IDs are enabled. Default is true.
	 * <p>This is only a hint to the JMS producer.
	 * See the JMS javadocs for details.
	 * @see MessageProducer#setDisableMessageID
	 */
	public void setMessageIdEnabled(boolean messageIdEnabled) {
		this.messageIdEnabled = messageIdEnabled;
	}

	/**
	 * Return whether message IDs are enabled.
	 */
	public boolean isMessageIdEnabled() {
		return messageIdEnabled;
	}

	/**
	 * Set whether message timestamps are enabled. Default is true.
	 * <p>This is only a hint to the JMS producer.
	 * See the JMS javadocs for details.
	 * @see MessageProducer#setDisableMessageTimestamp
	 */
	public void setMessageTimestampEnabled(boolean messageTimestampEnabled) {
		this.messageTimestampEnabled = messageTimestampEnabled;
	}

	/**
	 * Return whether message timestamps are enabled.
	 */
	public boolean isMessageTimestampEnabled() {
		return messageTimestampEnabled;
	}

	/**
	 * Set whether to inhibit the delivery of messages published by its own connection.
	 * Default is false.
	 * @see javax.jms.TopicSession#createSubscriber(javax.jms.Topic, String, boolean)
	 */
	public void setPubSubNoLocal(boolean pubSubNoLocal) {
		this.pubSubNoLocal = pubSubNoLocal;
	}

	/**
	 * Return whether to inhibit the delivery of messages published by its own connection.
	 */
	public boolean isPubSubNoLocal() {
		return pubSubNoLocal;
	}

	/**
	 * Set the timeout to use for receive calls.
	 * The default is -1, which means no timeout.
	 * @see MessageConsumer#receive(long)
	 * @see MessageConsumer#receive
	 */
	public void setReceiveTimeout(long receiveTimeout) {
		this.receiveTimeout = receiveTimeout;
	}

	/**
	 * Return the timeout to use for receive calls.
	 */
	public long getReceiveTimeout() {
		return receiveTimeout;
	}


	/**
	 * Set if the QOS values (deliveryMode, priority, timeToLive)
	 * should be used for sending a message.
	 * @see #setDeliveryMode
	 * @see #setPriority
	 * @see #setTimeToLive
	 */
	public void setExplicitQosEnabled(boolean explicitQosEnabled) {
		this.explicitQosEnabled = explicitQosEnabled;
	}

	/**
	 * If true, then the values of deliveryMode, priority, and timeToLive
	 * will be used when sending a message. Otherwise, the default values,
	 * that may be set administratively, will be used.
	 * @return true if overriding default values of QOS parameters
	 * (deliveryMode, priority, and timeToLive)
	 * @see #setDeliveryMode
	 * @see #setPriority
	 * @see #setTimeToLive
	 */
	public boolean isExplicitQosEnabled() {
		return explicitQosEnabled;
	}

	/**
	 * Set the delivery mode to use when sending a message.
	 * <p>Since a default value may be defined administratively,
	 * this is only used when isExplicitQosEnabled equals true.
	 * @param deliveryMode the delivery mode to use
	 * @see #isExplicitQosEnabled
	 * @see Message#DEFAULT_DELIVERY_MODE
	 * @see MessageProducer#send(Message, int, int, long)
	 */
	public void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	/**
	 * Return the delivery mode to use when sending a message.
	 */
	public int getDeliveryMode() {
		return deliveryMode;
	}

	/**
	 * Set the priority of a message when sending.
	 * <p>Since a default value may be defined administratively,
	 * this is only used when isExplicitQosEnabled equals true.
	 * @see #isExplicitQosEnabled
	 * @see Message#DEFAULT_PRIORITY
	 * @see MessageProducer#send(Message, int, int, long)
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Return the priority of a message when sending.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Set the time-to-live of the message when sending.
	 * <p>Since a default value may be defined administratively,
	 * this is only used when isExplicitQosEnabled equals true.
	 * @param timeToLive the message's lifetime (in milliseconds)
	 * @see #isExplicitQosEnabled
	 * @see Message#DEFAULT_TIME_TO_LIVE
	 * @see MessageProducer#send(Message, int, int, long)
	 */
	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * Return the time-to-live of the message when sending.
	 */
	public long getTimeToLive() {
		return timeToLive;
	}


	private void checkDefaultDestination() throws IllegalStateException {
		if (this.defaultDestination == null) {
			throw new IllegalStateException(
					"No defaultDestination or defaultDestinationName specified. Check configuration of JmsTemplate.");
		}
	}

	private void checkMessageConverter() throws IllegalStateException {
		if (getMessageConverter() == null) {
			throw new IllegalStateException("No messageConverter registered. Check configuration of JmsTemplate.");
		}
	}

	/**
	 * Resolve the given destination name into a JMS Destination,
	 * via this template's DestinationResolver.
	 * @param session the current JMS Session
	 * @param destinationName the name of the destination
	 * @return the located Destination
	 * @throws JMSException if resolution failed
	 * @see #setDestinationResolver
	 */
	protected Destination resolveDestinationName(Session session, String destinationName) throws JMSException {
		return getDestinationResolver().resolveDestinationName(session, destinationName, isPubSubDomain());
	}


	/**
	 * Create a JMS Connection via this template's ConnectionFactory.
	 * <p>This implementation uses JMS 1.1 API.
	 * @return the new JMS Connection
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Connection createConnection() throws JMSException {
		return getConnectionFactory().createConnection();
	}

	/**
	 * Create a JMS Session for the given Connection.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param con the JMS Connection to create a Session for
	 * @return the new JMS Session
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected Session createSession(Connection con) throws JMSException {
		return con.createSession(isSessionTransacted(), getSessionAcknowledgeMode());
	}

	/**
	 * Create a JMS MessageProducer for the given Session and Destination,
	 * configuring it to disable message ids and/or timestamps (if necessary).
	 * <p>Delegates to <code>doCreateProducer</code> for creation of the raw
	 * JMS MessageProducer, which needs to be specific to JMS 1.1 or 1.0.2.
	 * @param session the JMS Session to create a MessageProducer for
	 * @param destination the JMS Destination to create a MessageProducer for
	 * @return the new JMS MessageProducer
	 * @throws JMSException if thrown by JMS API methods
	 * @see #doCreateProducer
	 * @see #setMessageIdEnabled
	 * @see #setMessageTimestampEnabled
	 */
	protected MessageProducer createProducer(Session session, Destination destination) throws JMSException {
		MessageProducer producer = doCreateProducer(session, destination);
		if (!isMessageIdEnabled()) {
			producer.setDisableMessageID(true);
		}
		if (!isMessageTimestampEnabled()) {
			producer.setDisableMessageTimestamp(true);
		}
		return producer;
	}

	/**
	 * Create a raw JMS MessageProducer for the given Session and Destination.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to create a MessageProducer for
	 * @param destination the JMS Destination to create a MessageProducer for
	 * @return the new JMS MessageProducer
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected MessageProducer doCreateProducer(Session session, Destination destination) throws JMSException {
		return session.createProducer(destination);
	}

	/**
	 * Create a JMS MessageConsumer for the given Session and Destination.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to create a MessageConsumer for
	 * @param destination the JMS Destination to create a MessageConsumer for
	 * @return the new JMS MessageConsumer
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected MessageConsumer createConsumer(Session session, Destination destination) throws JMSException {
		if (isPubSubNoLocal()) {
			return session.createConsumer(destination, null, true);
		}
		else {
			return session.createConsumer(destination);
		}
	}

	/**
	 * Create a JMS MessageConsumer for the given Session and Destination.
	 * <p>This implementation uses JMS 1.1 API.
	 * @param session the JMS Session to create a MessageConsumer for
	 * @param destination the JMS Destination to create a MessageConsumer for
	 * @return the new JMS MessageConsumer
	 * @throws JMSException if thrown by JMS API methods
	 */
	protected MessageConsumer createConsumer(Session session, Destination destination, String messageSelector)
			throws JMSException {

		if (isPubSubNoLocal()) {
			return session.createConsumer(destination, messageSelector, true);
		}
		else {
			return session.createConsumer(destination, messageSelector);
		}
	}

	private void commitIfNecessary(Session session) throws JMSException {
		try {
			session.commit();
		}
		catch (TransactionInProgressException ex) {
			// Ignore -> can only happen in case of a JTA transaction.
		}
		catch (javax.jms.IllegalStateException ex) {
			// Ignore -> can only happen in case of a JTA transaction.
		}
	}


	/**
	 * Execute the action specified by the given action object within a
	 * JMS Session. Generalized version of execute(SessionCallback),
	 * allowing to start the JMS Connection on the fly.
	 * <p>Use execute(SessionCallback) for the general case. Starting
	 * the JMS Connection is just necessary for receiving messages,
	 * which is preferably achieve through the <code>receive</code> methods.
	 * @param action callback object that exposes the session
	 * @return the result object from working with the session
	 * @throws JmsException if there is any problem
	 * @see #execute(SessionCallback)
	 * @see #receive
	 */
	public Object execute(SessionCallback action, boolean startConnection) throws JmsException {
		Connection con = null;
		Session session = null;
		try {
			Connection conToUse = null;
			Session sessionToUse = null;
			ConnectionHolder conHolder =
					(ConnectionHolder) TransactionSynchronizationManager.getResource(getConnectionFactory());
			if (conHolder != null) {
				conToUse = conHolder.getConnection();
				if (startConnection) {
					conToUse.start();
				}
				sessionToUse = conHolder.getSession();
			}
			else {
				con = createConnection();
				if (startConnection) {
					con.start();
				}
				session = createSession(con);
				conToUse = con;
				sessionToUse = session;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Executing callback on JMS session [" + sessionToUse +
						"] from connection [" + conToUse + "]");
			}
			return action.doInJms(sessionToUse);
		}
		catch (JMSException ex) {
			throw convertJmsAccessException(ex);
		}
		finally {
			JmsUtils.closeSession(session);
			JmsUtils.closeConnection(con);
		}
	}

	public Object execute(SessionCallback action) throws JmsException {
		return execute(action, false);
	}

	public Object execute(final ProducerCallback action) throws JmsException {
		return execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				MessageProducer producer = createProducer(session, null);
				return action.doInJms(session, producer);
			}
		});
	}


	public void send(MessageCreator messageCreator) throws JmsException {
		checkDefaultDestination();
		if (getDefaultDestination() != null) {
			send(getDefaultDestination(), messageCreator);
		}
		else {
			send(getDefaultDestinationName(), messageCreator);
		}
	}

	public void send(final Destination destination, final MessageCreator messageCreator) throws JmsException {
		execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				doSend(session, destination, messageCreator);
				return null;
			}
		});
	}

	public void send(final String destinationName, final MessageCreator messageCreator) throws JmsException {
		execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				Destination destination = resolveDestinationName(session, destinationName);
				doSend(session, destination, messageCreator);
				return null;
			}
		});
	}

	protected void doSend(Session session, Destination destination, MessageCreator messageCreator)
			throws JMSException {

		MessageProducer producer = createProducer(session, destination);
		Message message = messageCreator.createMessage(session);
		if (logger.isDebugEnabled()) {
			logger.debug("Sending created message [" + message + "]");
		}
		doSend(producer, message);
		// Check commit - avoid commit call within a JTA transaction.
		if (session.getTransacted() && isSessionTransacted() &&
				!TransactionSynchronizationManager.hasResource(getConnectionFactory())) {
			// Transacted session created by this template -> commit.
			commitIfNecessary(session);
		}
	}

	protected void doSend(MessageProducer producer, Message message) throws JMSException {
		if (isExplicitQosEnabled()) {
			producer.send(message, getDeliveryMode(), getPriority(), getTimeToLive());
		}
		else {
			producer.send(message);
		}
	}


	public void convertAndSend(Object message) throws JmsException {
		checkDefaultDestination();
		if (getDefaultDestination() != null) {
			convertAndSend(getDefaultDestination(), message);
		}
		else {
			convertAndSend(getDefaultDestinationName(), message);
		}
	}

	public void convertAndSend(Destination destination, final Object message) throws JmsException {
		checkMessageConverter();
		send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return getMessageConverter().toMessage(message, session);
			}
		});
	}

	public void convertAndSend(String destinationName, final Object message) throws JmsException {
		checkMessageConverter();
		send(destinationName, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return getMessageConverter().toMessage(message, session);
			}
		});
	}

	public void convertAndSend(Object message, MessagePostProcessor postProcessor) throws JmsException {
		checkDefaultDestination();
		if (getDefaultDestination() != null) {
			convertAndSend(getDefaultDestination(), message, postProcessor);
		}
		else {
			convertAndSend(getDefaultDestinationName(), message, postProcessor);
		}
	}

	public void convertAndSend(
			Destination destination, final Object message, final MessagePostProcessor postProcessor)
			throws JmsException {

		checkMessageConverter();
		send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				Message msg = getMessageConverter().toMessage(message, session);
				return postProcessor.postProcessMessage(msg);
			}
		});
	}

	public void convertAndSend(
			String destinationName, final Object message, final MessagePostProcessor postProcessor)
	    throws JmsException {

		checkMessageConverter();
		send(destinationName, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				Message msg = getMessageConverter().toMessage(message, session);
				return postProcessor.postProcessMessage(msg);
			}
		});
	}


	public Message receive() throws JmsException {
		checkDefaultDestination();
		if (getDefaultDestination() != null) {
			return receive(getDefaultDestination());
		}
		else {
			return receive(getDefaultDestinationName());
		}
	}

	public Message receive(final Destination destination) throws JmsException {
		return (Message) execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				return doReceive(session, destination);
			}
		}, true);
	}

	public Message receive(final String destinationName) throws JmsException {
		return (Message) execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				Destination destination = resolveDestinationName(session, destinationName);
				return doReceive(session, destination);
			}
		}, true);
	}

	public Message receiveSelected(String messageSelector) throws JmsException {
		checkDefaultDestination();
		if (getDefaultDestination() != null) {
			return receiveSelected(getDefaultDestination(), messageSelector);
		}
		else {
			return receiveSelected(getDefaultDestinationName(), messageSelector);
		}
	}

	public Message receiveSelected(final Destination destination, final String messageSelector) throws JmsException {
		return (Message) execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				return doReceiveSelected(session, destination, messageSelector);
			}
		}, true);
	}

	public Message receiveSelected(final String destinationName, final String messageSelector) throws JmsException {
		return (Message) execute(new SessionCallback() {
			public Object doInJms(Session session) throws JMSException {
				Destination destination = resolveDestinationName(session, destinationName);
				return doReceiveSelected(session, destination, messageSelector);
			}
		}, true);
	}

	protected Message doReceive(Session session, Destination destination) throws JMSException {
		return doReceive(session, createConsumer(session, destination));
	}

	protected Message doReceiveSelected(Session session, Destination destination, String messageSelector)
			throws JMSException {
		return doReceive(session, createConsumer(session, destination, messageSelector));
	}

	protected Message doReceive(Session session, MessageConsumer consumer) throws JMSException {
		try {
			// Use transaction timeout (if available).
			long timeout = getReceiveTimeout();
			ConnectionHolder conHolder =
					(ConnectionHolder) TransactionSynchronizationManager.getResource(getConnectionFactory());
			if (conHolder != null && conHolder.hasTimeout()) {
				timeout = conHolder.getTimeToLiveInMillis();
			}
			Message message = (timeout >= 0) ?
					consumer.receive(timeout) : consumer.receive();
			if (session.getTransacted()) {
				// Commit necessary - but avoid commit call within a JTA transaction.
				if (isSessionTransacted() && conHolder == null) {
					// Transacted session created by this template -> commit.
					commitIfNecessary(session);
				}
			}
			else if (isClientAcknowledge(session)) {
				// Manually acknowledge message, if any.
				if (message != null) {
					message.acknowledge();
				}
			}
			return message;
		}
		finally {
			JmsUtils.closeMessageConsumer(consumer);
		}
	}

	protected boolean isClientAcknowledge(Session session) throws JMSException {
		return (session.getAcknowledgeMode() == Session.CLIENT_ACKNOWLEDGE);
	}


	public Object receiveAndConvert() throws JmsException {
		checkMessageConverter();
		return doConvertFromMessage(receive());
	}

	public Object receiveAndConvert(Destination destination) throws JmsException {
		checkMessageConverter();
		return doConvertFromMessage(receive(destination));
	}

	public Object receiveAndConvert(String destinationName) throws JmsException {
		checkMessageConverter();
		return doConvertFromMessage(receive(destinationName));
	}

	public Object receiveSelectedAndConvert(String messageSelector) throws JmsException {
		checkMessageConverter();
		return doConvertFromMessage(receiveSelected(messageSelector));
	}

	public Object receiveSelectedAndConvert(Destination destination, String messageSelector) throws JmsException {
		checkMessageConverter();
		return doConvertFromMessage(receiveSelected(destination, messageSelector));
	}

	public Object receiveSelectedAndConvert(String destinationName, String messageSelector) throws JmsException {
		checkMessageConverter();
		return doConvertFromMessage(receiveSelected(destinationName, messageSelector));
	}

	protected Object doConvertFromMessage(Message message) {
		if (message != null) {
			try {
				return getMessageConverter().fromMessage(message);
			}
			catch (JMSException ex) {
				throw convertJmsAccessException(ex);
			}
		}
		return null;
	}

}
