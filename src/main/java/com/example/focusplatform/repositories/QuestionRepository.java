package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    // NEW: Fetch all questions belonging to a specific course
    List<Question> findByCourseId(Long courseId);
}