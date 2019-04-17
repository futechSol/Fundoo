package com.bridgelabz.fundoo.user.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

public class ForgotPasswordDTO implements Serializable{
private static final long serialVersionUID = 1L;
	
	@NotEmpty(message = "Enter email address")
	@Pattern(regexp = "^[\\\\w-\\\\+]+(\\\\.[\\\\w]+)*@[\\\\w-]+(\\\\.[\\\\w]+)*(\\\\.[a-z]{2,})$", message="enetr valid email address..!")
	private String email;
	
	/**
	 * default constructor
	 */
	public ForgotPasswordDTO() {	}
	
	//Setters and Getters

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
