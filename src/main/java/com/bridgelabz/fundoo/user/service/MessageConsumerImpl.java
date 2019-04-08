package com.bridgelabz.fundoo.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumerImpl implements MessageConsumer {
    private String message;
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerImpl.class);
	
    @Override
	@RabbitListener(queues="${spring.rabbitmq.template.default-receive-queue}")
	public void recieveMessage(String message) {
		logger.info("consumed message = "+ message);
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return this.message;
	}
}
