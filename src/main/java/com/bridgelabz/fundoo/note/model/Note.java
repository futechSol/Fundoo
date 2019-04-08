package com.bridgelabz.fundoo.note.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.bridgelabz.fundoo.user.model.User;

@Entity
public class Note implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String title;
	@Lob
	private String description;
	private String color;
	private boolean isPinned;
	private boolean isArchived;
	private boolean isTrashed;
	private LocalDateTime createdDate;
	private LocalDateTime modifiedDate;
	private String reminder;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(nullable = false)
	@OnDelete(action = OnDeleteAction.CASCADE)
	//@JsonIgnore
	private User user;
    
	//@JsonIgnore
	@ManyToMany(cascade = {
		        CascadeType.PERSIST,
		        CascadeType.MERGE
		    })
	@JoinTable(name = "note_label", joinColumns = @JoinColumn(name = "note_id"), 
	           inverseJoinColumns = @JoinColumn(name = "label_id"))
	//@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<Label> labels;

	/**
	 * default constructor
	 */
	public Note() {
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
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

	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}

	public boolean isArchived() {
		return isArchived;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setArchived(boolean isArchived) {
		this.isArchived = isArchived;
	}

	public boolean isTrashed() {
		return isTrashed;
	}

	public void setTrashed(boolean isTrashed) {
		this.isTrashed = isTrashed;
	}
	
	public String getReminder() {
		return reminder;
	}

	public void setReminder(String reminder) {
		this.reminder = reminder;
	}
	
	public Set<Label> getLabels() {
		return labels;
	}

	public void setLabels(Set<Label> labels) {
		this.labels = labels;
	}

	public boolean addLabel(Label label) {
		if (labels == null)
			labels = new HashSet<>();
		return labels.add(label);
	}

	public boolean removeLabel(Label label) {
		return labels.remove(label);
	}

	@Override
	public String toString() {
		String str = "Note [ id = " + id + ", title = " + title + ", description = " + description + ", color = "
				+ color + ", isPinned = " + isPinned + ", isArchived = " + isArchived + ", isTrashed = " + isTrashed
				+ ", createdDate = " + createdDate + ", modifiedDate = " + modifiedDate + ", labels = ";
		if (labels != null) {
			for (Label label : labels) {
				str = str + label.getName() + ",";
			}
			str = str.substring(0, str.length() - 1);
		}
		return str + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Note) {
			Note note = (Note) obj;
			if (this.title.equals(note.title) && this.description.equals(note.description))
				return true;
			else
				return false;
		}
		throw new IllegalArgumentException("Can't compare non-Note objects");
	}

	@Override
	public int hashCode() {
		return (id + title + description + color + isPinned + isArchived + isTrashed + createdDate + modifiedDate)
				.toString().hashCode();
	}
}
