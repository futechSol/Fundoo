package com.bridgelabz.fundoo.note.service;

import java.util.List;

import com.bridgelabz.fundoo.note.dto.LabelDTO;
import com.bridgelabz.fundoo.note.dto.NoteDTO;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.response.Response;

public interface NoteService {
	Response create(NoteDTO noteDTO, String userToken);
	Response update(NoteDTO noteDTO, String userToken, long noteId);
	Response delete(String userToken, long noteId);
	Note getNote(String userToken, long noteId);
	List<Note> getAllNotes(String userToken);
	Response archiveNote(String userToken, long noteId);
	Response pinNote(String userToken, long noteId);
	Response trashNote(String userToken, long noteId);
	Response addLabel(String userToken, long noteId,  LabelDTO labelDTO);
	Response removeLabel(String userToken, long noteId,  LabelDTO labelDTO);
	Response addReminder(String userToken, long noteId,String reminder);
	Response removeReminder(String userToken, long noteId);
	Response addCollaborator(long noteId , String userToken, String email);
	Response removeCollaborator(long noteId , String userToken, String email);
}
