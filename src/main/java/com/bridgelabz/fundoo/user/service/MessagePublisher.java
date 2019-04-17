package com.bridgelabz.fundoo.user.service;

import org.springframework.mail.SimpleMailMessage;

public interface MessagePublisher {
	void publishMessage(SimpleMailMessage mail);
}
