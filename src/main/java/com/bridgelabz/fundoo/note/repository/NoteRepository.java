package com.bridgelabz.fundoo.note.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bridgelabz.fundoo.note.model.Note;
import com.bridgelabz.fundoo.user.model.User;

public interface NoteRepository extends JpaRepository<Note, Long> {
	 Optional<Note> findByIdAndUser(long id, User user);
	 List<Note> findAllByUser(User user);
}
