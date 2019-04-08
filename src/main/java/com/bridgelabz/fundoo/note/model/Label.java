package com.bridgelabz.fundoo.note.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.bridgelabz.fundoo.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity(name = "Label")
@Table(name = "Label")
public class Label implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name;
	private LocalDateTime createdDate;
	private LocalDateTime modifiedDate;
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JsonIgnore
	private User user;
    @JsonIgnore
	@ManyToMany(mappedBy = "labels")//, cascade = { CascadeType.PERSIST,CascadeType.MERGE})
	//@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<Note> notes;

	/**
	 * default constructor
	 */
	public Label() {
	}
	// getters and setters

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDateTime getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(LocalDateTime modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Set<Note> getNotes() {
		return notes;
	}

	public void setNotes(Set<Note> notes) {
		this.notes = notes;
	}
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	//
	public boolean addNote(Note note) {
		if(notes == null)
			notes = new HashSet<>();
		return notes.add(note);
	}
	
	public boolean removeNote(Note note) {
		return notes.remove(note);
	} 
   //
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Label) {
			Label label = (Label) obj;
			if (this.name.equals(label.getName()))
				return true;
			else
				return false;
		}
		throw new IllegalArgumentException("Can't compare non-Label objects");
	}

	@Override
	public int hashCode() {
		return (name).hashCode();
	}
}
