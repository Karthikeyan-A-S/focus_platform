package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    // For students joining a class
    Optional<Classroom> findByInviteCode(String inviteCode);

    // NEW: For teachers viewing their dashboard
    List<Classroom> findByTeacher(User teacher);
    List<Classroom> findByStudentsContaining(User student);
}