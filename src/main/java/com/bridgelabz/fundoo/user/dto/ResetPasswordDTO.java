package com.bridgelabz.fundoo.user.dto;

import java.io.Serializable;
import javax.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

public class ResetPasswordDTO implements Serializable{
private static final long serialVersionUID = 1L;
	
@NotEmpty(message = "Enter password ...!")
@Length(min = 8, max = 32, message = "Password must be min 6 and max 32 chars long")
private String password;
	
	/**
	 * default constructor
	 */
	public ResetPasswordDTO() {	}
	
	//Setters and Getters

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
