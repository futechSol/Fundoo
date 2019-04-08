package com.bridgelabz.fundoo.note.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bridgelabz.fundoo.note.model.Label;
import com.bridgelabz.fundoo.user.model.User;

public interface LabelRepository extends JpaRepository<Label, Long> {
   Optional<Label> findByNameAndUserAllIgnoreCase(String name, User user);
   Optional<Label> findByIdAndUser(long id, User user);
   Set<Label> findAllByUser(User user);
}
