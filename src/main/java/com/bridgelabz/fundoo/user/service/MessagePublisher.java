package com.bridgelabz.fundoo.user.service;

import org.springframework.mail.SimpleMailMessage;

import com.bridgelabz.fundoo.note.model.NoteContainer;

public interface MessagePublisher {
	void publishUserMail(SimpleMailMessage mail);
	void publishNoteData(NoteContainer noteContainer);
}
