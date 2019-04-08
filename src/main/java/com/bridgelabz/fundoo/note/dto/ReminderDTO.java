package com.bridgelabz.fundoo.note.dto;

import java.io.Serializable;

public class ReminderDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	private String reminder;
	
	public ReminderDTO() {
		
	}

	public String getReminder() {
		return reminder;
	}

	public void setReminder(String reminder) {
		this.reminder = reminder;
	}

	@Override
	public String toString() {
		return "ReminderDTO [reminder=" + reminder + "]";
	}
}
