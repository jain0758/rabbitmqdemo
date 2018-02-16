package com.messaging.subscribe;

import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageKeyGenerator;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

@Configuration
public class ConsumerMessagingConfiguration {

	@Value("${rabbit.user}")
	private String userName;

	@Value("${rabbit.password}")
	private String password;

	@Value("${rabbit.address}")
	private String address;

	@Value("${rabbit.virtualhost}")
	private String virtualHost;
//
//	@Value("${ccs.dead.letter.exchange}")
//	private String deadLetterExchange;

	private static final int RETRY_COUNT = 3;

	private static final int WAIT_ONE_SECOND = 1000;

	@Bean(name = "ccsConnectionFactory")
	@Primary
	public ConnectionFactory ccsConnectionFactory() {

		final CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
		connectionFactory.setUsername(userName);
		connectionFactory.setPassword(password);
		connectionFactory.setAddresses(address);
		connectionFactory.setVirtualHost(virtualHost);
		return connectionFactory;
	}

	@Bean(name = "ccsRabbitAdmin")
	@Primary
	public RabbitAdmin ccsRabbitAdmin(@Qualifier("ccsConnectionFactory") final ConnectionFactory connectionFactory) {

		return new RabbitAdmin(connectionFactory);
	}

	@Bean(name = "ccsRabbitTransactionManager")
	@Primary
	public RabbitTransactionManager ccsRabbitTransactionManager(
			@Qualifier("ccsConnectionFactory") final ConnectionFactory connectionFactory) {

		return new RabbitTransactionManager(connectionFactory);
	}

	@Bean(name = "consumerTemplate")
	@Primary
	public RabbitTemplate consumerTemplate(
			@Qualifier("ccsConnectionFactory") final ConnectionFactory connectionFactory) {

		final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setExchange("ccs.consumer.exchange");
		rabbitTemplate.setRoutingKey("CONSUMER*");
		return rabbitTemplate;
	}

	@Bean(name = "consumerExchange")
	public TopicExchange consumerExchange(@Qualifier("ccsRabbitAdmin") final RabbitAdmin rabbitAdmin) {

		final TopicExchange exchange = new TopicExchange("ccs.consumer.exchange", true, false);
		exchange.setAdminsThatShouldDeclare(rabbitAdmin);

		return exchange;
	}

	@Bean(name = "statefulRetryOperationsInterceptor")
	public MethodInterceptor statefulRetryOperationsInterceptor() {
		return RetryInterceptorBuilder.stateful().backOffOptions(WAIT_ONE_SECOND, 1, WAIT_ONE_SECOND)
				.maxAttempts(RETRY_COUNT).recoverer(new RejectAndDontRequeueRecoverer())
				.messageKeyGenerator(new MessageKeyGenerator() {

					@Override
					public Object getKey(Message message) {
						if (StringUtils.isEmpty(message.getMessageProperties().getMessageId())) {
							return "invalid";
						}
						return message.getMessageProperties().getMessageId();

					}
				}).build();

	}
}
