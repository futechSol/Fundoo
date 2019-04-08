package com.bridgelabz.fundoo.user.dto;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

/********************************************************************************************
 * Purpose : to hold the login details
 *           
 * @author BridgeLabz/Sudhakar
 * @version 1.0
 * @since 27-02-2019
 *********************************************************************************************/
public class LoginDTO implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@NotEmpty(message = "Enter email address")
	@Pattern(regexp = "^[\\\\w-\\\\+]+(\\\\.[\\\\w]+)*@[\\\\w-]+(\\\\.[\\\\w]+)*(\\\\.[a-z]{2,})$", message="enetr valid email address..!")
	private String email;
	@NotEmpty(message = "Enter password ...!")
	@Length(min = 8, max = 32, message = "Password must be min 6 and max 32 chars long")
	private String password;
	
	/**
	 * default constructor
	 */
	public LoginDTO() {	}
	
	//Setters and Getters

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
