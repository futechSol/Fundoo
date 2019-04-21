package com.bridgelabz.fundoo.note.model;

import java.io.Serializable;

public class NoteContainer implements Serializable{
	
	private static final long serialVersionUID = 1L;
    
	private NoteOperation noteOperation;
    private Note note;
    
    public NoteContainer() {}
    
	public NoteOperation getNoteOperation() {
		return noteOperation;
	}
	public void setNoteOperation(NoteOperation noteOperation) {
		this.noteOperation = noteOperation;
	}
	public Note getNote() {
		return note;
	}
	public void setNote(Note note) {
		this.note = note;
	}

	@Override
	public String toString() {
		return "NoteContainer [noteOperation=" + noteOperation + ", note=" + note + "]";
	}
}
