package com.bridgelabz.fundoo.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class MessagePublisherImpl implements MessagePublisher {
	@Autowired
	private AmqpTemplate amqpTemplate;
	@Value("${spring.rabbitmq.template.exchange}")
	private String exchange;
	@Value("${spring.rabbitmq.user.routingKey}")
	private String userRoutingKey;
	private static final Logger logger = LoggerFactory.getLogger(MessagePublisherImpl.class);
	
	@Override
	public void publishMessage(SimpleMailMessage mail) {
		logger.info("published message = " + mail);
		logger.info("exchange = "+exchange);
		logger.info("routingKey = "+userRoutingKey);
		amqpTemplate.convertAndSend(exchange, userRoutingKey, mail);
	}

}
