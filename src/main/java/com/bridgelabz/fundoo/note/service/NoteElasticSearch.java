package com.bridgelabz.fundoo.note.service;

import java.util.List;
import java.util.Map;

import org.elasticsearch.action.DocWriteResponse.Result;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.user.model.User;

public interface NoteElasticSearch {
	List<Note> searchNoteByTitle(String title);
	Result insertNote(Note note);
	Map<String, Object> updateNoteById(Note note);
	Result deleteNoteById(String id);
	Map<String, Object> getNoteById(String id);
	List<Note> searchNoteByAnyText(String queryString,User user);
}
