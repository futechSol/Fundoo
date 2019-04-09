package com.bridgelabz.fundoo.note.controller;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.bridgelabz.fundoo.exception.NoteException;
import com.bridgelabz.fundoo.exception.UserException;
import com.bridgelabz.fundoo.note.dto.LabelDTO;
import com.bridgelabz.fundoo.note.dto.NoteDTO;
import com.bridgelabz.fundoo.note.dto.ReminderDTO;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.note.service.NoteService;
import com.bridgelabz.fundoo.response.Response;
import com.bridgelabz.fundoo.user.dto.LoginDTO;
import com.bridgelabz.fundoo.util.ResponseInfo;

@RestController
public class NoteController {
	private static final Logger logger = LoggerFactory.getLogger(NoteController.class);
	@Autowired
	private NoteService noteService;

	@PostMapping(value = "users/notes")
	public ResponseEntity<Response> create(@RequestBody NoteDTO noteDTO, @RequestHeader String token) {
		logger.info("NoteDTO : " + noteDTO);
		logger.trace("Note Creation");
		Response response =null;
		if(noteDTO.getTitle().equals("") && noteDTO.getDescription().equals("")) {
			response = ResponseInfo.getResponse(-700, "title and  description both can't be empty");
		}else
			response = noteService.create(noteDTO, token);
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping(value = "users/notes/{id}")
	public ResponseEntity<Response> update(@Valid @RequestBody NoteDTO noteDTO, @RequestHeader String token, @PathVariable long id) {
		logger.info("NoteDTO : " + noteDTO);
		logger.trace("Note updating");
		Response response = noteService.update(noteDTO, token, id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@DeleteMapping(value = "users/notes/{id}")
	public ResponseEntity<Response> delete(@RequestHeader String token, @PathVariable long id) {
		logger.info("Token: " + token);
		logger.info("NoteId : " + id);
		logger.trace("Note Deleting");
		Response response = noteService.delete(token, id);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping(value = "users/notes")
	public ResponseEntity<Object> getAllNotes(@RequestHeader String token) {
		logger.info("token : " + token);
		logger.trace("Geting all notes");
		Object obj;
		try {
			obj = noteService.getAllNotes(token);
		}
		catch(UserException e){
			obj = ResponseInfo.getResponse(e.getErrorCode(),e.getMessage());
		}
		return new ResponseEntity<>(obj,HttpStatus.OK);
	}

	@GetMapping(value = "users/notes/{id}")
	public ResponseEntity<Object> getNote(@RequestHeader String token, @PathVariable long id) {
		logger.info("token: " + token);
		logger.trace("Get Note By id");
		Object obj;
		try {
			obj = noteService.getNote(token, id);
			Note note = (Note)obj;
			return new ResponseEntity<>(note,HttpStatus.OK);
		}
		catch(UserException e){
			obj = ResponseInfo.getResponse(e.getErrorCode(),e.getMessage());
		}
		catch(NoteException e){
			obj = ResponseInfo.getResponse(e.getErrorCode(),e.getMessage());
		}
		return new ResponseEntity<>(obj,HttpStatus.OK);
	}

	@PutMapping(value = "users/notes/pin/{id}")
	public ResponseEntity<Response> pinNote(@RequestHeader String token, @PathVariable long id){
		logger.info("token: " + token);
		logger.trace("Pin Note By id");
		Response response = noteService.pinNote(token, id);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@PutMapping(value = "users/notes/trash/{id}")
	public ResponseEntity<Response> trashNote(@RequestHeader String token, @PathVariable long id){
		logger.info("token: " + token);
		logger.trace("Pin Note By id");
		Response response = noteService.trashNote(token, id);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@PutMapping(value = "users/notes/archive/{id}")
	public ResponseEntity<Response> archiveNote(@RequestHeader String token, @PathVariable long id){
		logger.info("token: " + token);
		logger.trace("Archive Note By id");
		Response response = noteService.archiveNote(token, id);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}

	@PostMapping(value = "users/notes/{noteId}/labels")
	public ResponseEntity<Response> addLabel(@RequestHeader String token, @PathVariable long noteId, @RequestBody LabelDTO labelDTO){
		logger.info("token: " + token);
		logger.trace("add label to note");
		Response response;
		if(labelDTO.getName().equals("") || labelDTO.getName() == null)
			response = ResponseInfo.getResponse(-800, "label can't not be empty");
		else
			response = noteService.addLabel(token, noteId, labelDTO);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	@PutMapping(value = "users/notes/{noteId}/labels")
	public ResponseEntity<Response> removeLabel(@RequestHeader String token, @PathVariable long noteId, @RequestBody LabelDTO labelDTO){
		logger.info("token: " + token);
		logger.trace("remove label to note");
		Response response;
		if(labelDTO.getName().equals("") || labelDTO.getName() == null)
			response = ResponseInfo.getResponse(-800, "label can't not be empty");
		else
			response = noteService.removeLabel(token, noteId, labelDTO);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	
	@PostMapping(value = "users/notes/{noteId}/reminder")
	public ResponseEntity<Response> addReminder(@RequestHeader String token, @PathVariable long noteId, @RequestBody ReminderDTO reminderDTO){
		logger.info("token: " + token);
		logger.trace("add reminder to note");
		Response response = noteService.addReminder(token, noteId, reminderDTO.getReminder());
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	@PutMapping(value = "users/notes/{noteId}/reminder")
	public ResponseEntity<Response> removeReminder(@RequestHeader String token, @PathVariable long noteId){
		logger.info("token: " + token);
		logger.trace("add reminder to note");
		Response response = noteService.removeReminder(token, noteId);
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	@PostMapping(value = "users/notes/{noteId}/collaborator")
	public ResponseEntity<Response> addCollaborator(@RequestHeader String token, @PathVariable long noteId, @RequestBody LoginDTO email){
		logger.info("token :    "+token);
		logger.info("noteId :   "+ noteId);
		logger.info("email      "+ email.getEmail());
		logger.trace("add collaborator");
		Response response = noteService.addCollaborator(noteId, token, email.getEmail());
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
	@PutMapping(value = "users/notes/{noteId}/collaborator")
	public ResponseEntity<Response> removeCollaborator(@RequestHeader String token, @PathVariable long noteId, @RequestBody LoginDTO email){
		logger.info("token :    "+token);
		logger.info("noteId :   "+ noteId);
		logger.info("email      "+ email.getEmail());
		logger.trace("add collaborator");
		Response response = noteService.removeCollaborator(noteId, token, email.getEmail());
		return new ResponseEntity<>(response,HttpStatus.OK);
	}
}

