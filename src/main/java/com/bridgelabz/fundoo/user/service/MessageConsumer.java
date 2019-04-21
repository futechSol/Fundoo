package com.bridgelabz.fundoo.user.service;

import org.springframework.mail.SimpleMailMessage;

import com.bridgelabz.fundoo.note.model.NoteContainer;

public interface MessageConsumer {
	 void recieveUserMail(SimpleMailMessage mail);
	 void recieveNoteData(NoteContainer noteContainer);
}
