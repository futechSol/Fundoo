package com.bridgelabz.fundoo.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.note.model.NoteContainer;
import com.bridgelabz.fundoo.note.service.NoteElasticSearch;

@Component
public class MessageConsumerImpl implements MessageConsumer {
	@Autowired
	private MailService mailService;
	@Autowired
	private NoteElasticSearch noteElasticSearch;
	private static final Logger logger = LoggerFactory.getLogger(MessageConsumerImpl.class);

	@Override
	@RabbitListener(queues="${spring.rabbitmq.user.queue}")
	public void recieveUserMail(SimpleMailMessage mail) {
		logger.info("consumed message = "+ mail.toString());
		mailService.sendEmail(mail);
	}

	@Override
	@RabbitListener(queues = "${spring.rabbitmq.note.queue}")
	public void recieveNoteData(NoteContainer noteContainer) {
		logger.info("Note operation : " + noteContainer.getNoteOperation());
		Note note = noteContainer.getNote(); 
		switch(noteContainer.getNoteOperation()) 
		{
			case CREATE : noteElasticSearch.insertNote(note);
			break;
			case UPDATE : noteElasticSearch.updateNoteById(note);
			break;
			case DELETE : noteElasticSearch.deleteNoteById(String.valueOf(note.getId()));
			break;
		}
	}
}
