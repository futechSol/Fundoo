package com.bridgelabz.fundoo.user.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Length;

/********************************************************************************************
 * Purpose : UserDTO class to map the details of the user to the User instance
 * 
 * @author BridgeLabz/Sudhakar
 * @version 1.0
 * @since 26-02-2019
 *********************************************************************************************/
public class UserDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	@Column(nullable=false)
	@NotEmpty(message = "Enter firstName")
	@Length(min = 3, max = 32, message = "firstName must be of min 3 to 32 characters long")
	private String firstName;
	@NotEmpty(message = "Enter lastName")
	@Length(min = 3, max = 32, message = "lastName must be of min 3 to 32 characters long")
	private String lastName;
	@NotEmpty(message = "Enter mobile number")
	@Pattern(regexp="[7-9] {1}[0-9]{9}",message = "Enter 10-digit mobile number")//^[7-9][0-9]{9}$
	private String phoneNumber;
	@NotEmpty(message = "Enter email address")
	@Pattern(regexp = "^[\\\\w-\\\\+]+(\\\\.[\\\\w]+)*@[\\\\w-]+(\\\\.[\\\\w]+)*(\\\\.[a-z]{2,})$", message="enetr valid email address..!")
	private String email;
	@NotEmpty(message = "Enter password ...!")
	@Length(min = 8, max = 32, message = "Password must be min 6 and max 32 chars long")
	private String password;

	/**
	 * default constructor
	 */
	public UserDTO() {
		
	}

	// setters and getters

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "UserDTO [ firstName = " + firstName + ", lastName = " + lastName + ", phoneNumber = " + phoneNumber
				+ ", email = " + email + ", password = password";
	}
}
