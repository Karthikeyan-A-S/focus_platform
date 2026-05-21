package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.Course;
import com.example.focusplatform.entities.CourseProgress;
import com.example.focusplatform.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    boolean existsByStudentAndCourse(User student, Course course);

    Optional<CourseProgress> findFirstByStudentAndCourse(User student, Course course);

    @Modifying
    @Transactional
    void deleteByCourseId(Long courseId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudentAnswer sa WHERE sa.question.id = :questionId")
    void deleteStudentAnswersByQuestionId(@Param("questionId") Long questionId);
}