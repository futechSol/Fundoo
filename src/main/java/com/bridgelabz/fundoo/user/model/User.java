package com.bridgelabz.fundoo.user.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotEmpty;

import com.bridgelabz.fundoo.note.model.Note;
import com.fasterxml.jackson.annotation.JsonIgnore;

/********************************************************************************************
 * Purpose : User class to capture the details of the FundooNotes user
 *           
 * @author BridgeLabz/Sudhakar
 * @version 1.0
 * @since 26-02-2019
 *********************************************************************************************/
@Entity
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(updatable = false)
	private long id;
	@NotEmpty
	private String firstName;
	@NotEmpty
	private String lastName;
	@NotEmpty
	private String phoneNumber;
	@NotEmpty
	private String email;
	@NotEmpty
	private String password;
	private String profilePic;
	private LocalDateTime registrationDate;
	private LocalDateTime modifiedDate;
	private boolean isVerified;
	// collaborator for notes
	@JsonIgnore
	@ManyToMany(mappedBy = "collaboratedUsers")//, cascade = { CascadeType.PERSIST,CascadeType.MERGE})
	//@OnDelete(action = OnDeleteAction.CASCADE)
	private List<Note> collaboratedNotes;

	public List<Note> getCollaboratedNotes(){
		return this.collaboratedNotes;
	}
	public boolean addCollaboratedNote(Note note) {
		if(this.collaboratedNotes == null)
			this.collaboratedNotes = new ArrayList<>();
		return this.collaboratedNotes.add(note);	
	}
	public boolean removeCollaboratedNote(Note note) {
		return this.collaboratedNotes.remove(note);
	}
	//
	/**
	 * default constructor
	 */
	public User() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	public LocalDateTime getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDateTime registrationDate) {
		this.registrationDate = registrationDate;
	}

	public LocalDateTime getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(LocalDateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public void setVerified(boolean isVerified) {
		this.isVerified = isVerified;
	}
	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}
	@Override
	public String toString() {
		return "User[ id = " + id + ", firstName = " + firstName + ", lastName = " + lastName + ", phoneNumber = "
				+ phoneNumber + ", email = " + email + ", password = " + password + ", registrationDate = "
				+ registrationDate + ", modifiedDate = " + modifiedDate + ", isVerified = " + isVerified + "]";

	}
}
