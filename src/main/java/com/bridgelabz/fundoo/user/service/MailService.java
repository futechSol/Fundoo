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
     * sends the mail to a specific user
     * @param mail SimpleMailMessage instance
     */
	public void sendEmail(SimpleMailMessage mail) {
		javaMailSender.send(mail);
	}
	
	/**
	 * returns a SimpleMailMessage instance with the given parameters
	 * @param to
	 * @param subject
	 * @param text
	 * @return
	 */
	public SimpleMailMessage getMailMessageObject(String to, String subject, String text) {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(to);
		mail.setSubject(subject);
		mail.setText(text);
		mail.setFrom(this.environment.getProperty("spring.mail.username"));
		return mail;
	}
}
