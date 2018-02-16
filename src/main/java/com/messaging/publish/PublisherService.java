package com.messaging.publish;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PublisherService
{
	private static final Logger LOG = LoggerFactory.getLogger(PublisherService.class);
	
	final String DIRECT_DEBIT = "Direct_Debit";

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void publishMessage(Message message, String eventName, String routingKey, String version)
	{
		publish(routingKey, buildAmqpMessage(message, eventName, routingKey, version));
	}
	
	private void publish(String routingKey, org.springframework.amqp.core.Message messageAmqp)
	{
		try
		{
			rabbitTemplate.send(routingKey, messageAmqp);
		} catch (AmqpException amqpException)
		{
			LOG.error("Error while publishing event.", amqpException);
			LOG.error("Error for connection factory " + rabbitTemplate.getConnectionFactory().getHost() + ":" + rabbitTemplate.getConnectionFactory().getPort() + " virtual host " + rabbitTemplate.getConnectionFactory().getVirtualHost());
		}
	}

	private org.springframework.amqp.core.Message buildAmqpMessage(Message message, String eventName, String routingKey, String version)
	{
		MessageProperties messageProperties = createMessageProperties(eventName);
		message.setMessageMetaData(buildMessageHeader(eventName, routingKey, version, messageProperties));
		return rabbitTemplate.getMessageConverter().toMessage(message, messageProperties);
	}

	private Map<String, String> buildMessageHeader(String eventName, String routingKey, String version, MessageProperties messageProperties)
	{
		Map<String, String> messageHeader = createMetaData(eventName, messageProperties.getMessageId(), routingKey, version);
		for (Map.Entry<String, String> entry : messageHeader.entrySet())
		{
			String key = entry.getKey();
			String value = entry.getValue();
			messageProperties.setHeader(key, value);
		}
		return messageHeader;
	}

	private MessageProperties createMessageProperties(String eventName)
	{
		MessageProperties mProperties = new MessageProperties();
		mProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
		String messageId = null;
		if (eventName != null)
		{
			messageId = eventName + "_" + UUID.randomUUID();
			mProperties.setMessageId(messageId);
			mProperties.setHeader(MessageProperty.MESSAGE_ID.value(), messageId);
		}
		return mProperties;
	}

	private Map<String, String> createMetaData(String eventName, String messageId, String routingKey, String version)
	{
		Map<String, String> messageMetaData = new HashMap<String, String>();
		messageMetaData.put(MessageProperty.EVENT_NAME.value(), eventName);
		messageMetaData.put(MessageProperty.EVENT_ORIGINATOR.value(), DIRECT_DEBIT);
		messageMetaData.put(MessageProperty.VERSION.value(), version);
		messageMetaData.put(MessageProperty.MESSAGE_ID.value(), messageId);
		messageMetaData.put(MessageProperty.ROUTING_KEY.value(), routingKey);
		return messageMetaData;
	}
}
