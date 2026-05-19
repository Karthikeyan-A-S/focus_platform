package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {
    List<QuizResponse> findBySessionId(Long sessionId);
}