package com.messaging.subscribe;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.cs.pii.converter.CustomJsonMessageConverter;
import com.example.cs.pii.pis.ote.PisOteEventProcessor;
import com.example.cs.pii.service.dto.FeatureEnablementMessage;

@Configuration
public class PisEnablementListenerConfiguration {

	@Value("${pis.enablement.queue}")
	private String pisEnablementQueueName;

	@Value("${pis.enablement.queue}" + ".dead.letter")
	private String pisEnablementDeadLetterQueueName;

	@Value("${pis.exchange}")
	private String pisExchange;

	@Value("${pis.dead.letter.exchange}")
	private String pisDeadLetterExchange;

	@Value("${pis.enablement.routing.key}")
	private String pisEnablementRoutingKey;

	// ********** Dead Letter Exchange, Queue and Binding **************

	@Bean(name = "pisDeadLetterExchange")
	public TopicExchange pisDeadLetterExchange(@Qualifier("ccsRabbitAdmin") final RabbitAdmin rabbitAdmin) {
		final TopicExchange productExchange = new TopicExchange(pisDeadLetterExchange, true, false);
		productExchange.setAdminsThatShouldDeclare(rabbitAdmin);
		return productExchange;
	}
	
	@Bean(name = "pisExchange")
	public TopicExchange pisExchange() {
		final TopicExchange productExchange = new TopicExchange(pisExchange, true, false);
		productExchange.setShouldDeclare(false);
		return productExchange;
	}

	@Bean(name = "pisEnablementEventDeadLetterQueue")
	public Queue pisEnablementEventDeadLetterQueue(@Qualifier("ccsRabbitAdmin") final RabbitAdmin rabbitAdmin) {
		final Queue queue = new Queue(pisEnablementDeadLetterQueueName, true);
		queue.setAdminsThatShouldDeclare(rabbitAdmin);
		return queue;
	}
	
	@Bean(name = "pisEnablementEventQueue")
	public Queue pisEnablementEventQueue(@Qualifier("pisDeadLetterExchange") final TopicExchange exchange,
			@Qualifier("ccsRabbitAdmin") final RabbitAdmin rabbitAdmin) {
		final Map<String, Object> arguments = new HashMap<String, Object>();
		arguments.put("x-dead-letter-exchange", exchange.getName());
		final Queue queue = new Queue(pisEnablementQueueName, true, false, false, arguments);
		queue.setAdminsThatShouldDeclare(rabbitAdmin);
		return queue;
	}


	@Bean(name = "pisEnablementEventDeadLetterQueueBinding")
	public Binding pisEnablementEventDeadLetterQueueBinding(
			@Qualifier("pisEnablementEventDeadLetterQueue") final Queue queue,
			@Qualifier("pisDeadLetterExchange") final TopicExchange exchange,
			@Qualifier("ccsRabbitAdmin") final RabbitAdmin rabbitAdmin) {

		final Binding binding = BindingBuilder.bind(queue).to(exchange).with(pisEnablementRoutingKey);
		binding.setAdminsThatShouldDeclare(rabbitAdmin);
		return binding;
	}
	
	@Bean(name = "pisEnablementEventQueueBinding")
	public Binding pisEnablementEventQueueBinding(@Qualifier("pisEnablementEventQueue") final Queue queue,
			@Qualifier("pisExchange") final TopicExchange exchange,
			@Qualifier("ccsRabbitAdmin") final RabbitAdmin rabbitAdmin) {

		final Binding binding = BindingBuilder.bind(queue).to(exchange).with(pisEnablementRoutingKey);
		binding.setAdminsThatShouldDeclare(rabbitAdmin);

		return binding;
	}

	// ********* Converter, Processor, Listener, Listener Container ***********

	@Bean
	public CustomJsonMessageConverter pisEnablementEventConverter() {
		return new CustomJsonMessageConverter(FeatureEnablementMessage.class);
	}

	@Bean
	public PisOteEventProcessor pisOteEventProcessor() {
		return new PisOteEventProcessor();
	}

	@Bean(name = "pisEnablementEventListener")
	public MessageListener pisEnablementEventListener() {
		final MessageListenerAdapter delegate = new MessageListenerAdapter(pisOteEventProcessor(),
				pisEnablementEventConverter());
		delegate.setDefaultListenerMethod("processPisOteEvent");
		return delegate;
	}


	@Bean(name = "pisEnablementEvenListenerContainer")
	public SimpleMessageListenerContainer pisEnablementEvenListenerContainer(
			@Qualifier("ccsConnectionFactory") final ConnectionFactory connectionFactory,
			@Qualifier("ccsRabbitTransactionManager") final RabbitTransactionManager transactionManager,
			@Qualifier("ccsRabbitAdmin") final RabbitAdmin rabbitAdmin,
			@Qualifier("pisEnablementEventQueue") final Queue queue,
			@Qualifier("pisEnablementEventListener") final MessageListener messageListener,
			@Qualifier("statefulRetryOperationsInterceptor") final MethodInterceptor statefulRetryOperationsInterceptor) {

		final SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setTransactionManager(transactionManager);
		container.setChannelTransacted(true);
		container.setQueues(queue);
		container.setRabbitAdmin(rabbitAdmin);
		container.setMessageListener(messageListener);
		container.setAdviceChain(new Advice[] { statefulRetryOperationsInterceptor });
		return container;
	}
}
