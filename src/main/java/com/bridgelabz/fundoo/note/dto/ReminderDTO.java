package com.bridgelabz.fundoo.note.dto;

import java.io.Serializable;

public class ReminderDTO implements Serializable{

	private static final long serialVersionUID = 1L;
	private String reminder;
	private String repeatReminder;
	
	public ReminderDTO() {
		
	}

	public String getReminder() {
		return reminder;
	}

	public void setReminder(String reminder) {
		this.reminder = reminder;
	}

	public String getRepeatReminder() {
		return repeatReminder;
	}

	public void setRepeatReminder(String repeatReminder) {
		this.repeatReminder = repeatReminder;
	}
	
	@Override
	public String toString() {
		return "ReminderDTO [reminder=" + reminder + ", repeatReminder = " + repeatReminder + "]";
	}
}
