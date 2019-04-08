package com.bridgelabz.fundoo.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@PropertySource("classpath:application.properties")
@Service
public class MailService {

	@Autowired
	private JavaMailSender javaMailSender;
	@Autowired 
	private Environment environment; 
	/**
	 * sending the mail to activate user
	 * @param user User instance to be activated
	 */
	public void sendEmail(String to, String subject, String message) {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(to);
		// get 'from' value from the property file
		mail.setFrom(this.environment.getProperty("spring.mail.username"));
		mail.setSubject(subject);
		mail.setText(message);
		javaMailSender.send(mail);
	}
}
