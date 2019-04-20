package com.bridgelabz.fundoo.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumerImpl implements MessageConsumer {
	@Autowired
	private MailService mailService;
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerImpl.class);
	
    @Override
	@RabbitListener(queues="${spring.rabbitmq.user.queue}")
	public void recieveMessage(SimpleMailMessage mail) {
		logger.info("consumed message = "+ mail.toString());
		mailService.sendEmail(mail);
	}
}
