package com.bridgelabz.fundoo.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bridgelabz.fundoo.user.model.User;

@Component
public class MessageConsumerImpl implements MessageConsumer {
	@Autowired
	private MailService mailService;
    private User mailRecipient;
    private String mailSubject;
    private static final Logger logger = LoggerFactory.getLogger(MessageConsumerImpl.class);
	
    @Override
	@RabbitListener(queues="${spring.rabbitmq.template.default-receive-queue}")
	public void recieveMessage(String message) {
		logger.info("consumed message = "+ message);
		mailService.sendEmail(mailRecipient.getEmail(),mailSubject,message);
	}
	
	@Override
	public void emailDetails(User to, String subject) {
		this.mailRecipient = to;
		this.mailSubject = subject;
	}
}
