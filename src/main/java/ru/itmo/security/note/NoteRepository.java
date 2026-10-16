package ru.itmo.security.note;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findAllByOwnerUsernameOrderById(String ownerUsername);
}

