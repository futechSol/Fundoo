package com.bridgelabz.fundoo.note.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;

public class LabelDTO implements Serializable{
	private static final long serialVersionUID = 1L;
	@NotEmpty(message = "Enter label name")
	private String name;
    
	public LabelDTO() {}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "LabelDTO [ name = " + name + "]"; 
	}
}
