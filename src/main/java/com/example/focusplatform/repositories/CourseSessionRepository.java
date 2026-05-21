package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {

    List<CourseSession> findByStudentId(Long studentId);

    @Modifying
    @Transactional
    void deleteByCourseId(Long courseId);
}