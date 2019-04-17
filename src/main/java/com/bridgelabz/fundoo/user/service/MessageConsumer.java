package com.bridgelabz.fundoo.user.service;

import org.springframework.mail.SimpleMailMessage;

public interface MessageConsumer {
	 void recieveMessage(SimpleMailMessage mail);
}
