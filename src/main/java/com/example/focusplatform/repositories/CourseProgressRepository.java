package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.Course;
import com.example.focusplatform.entities.CourseProgress;
import com.example.focusplatform.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    boolean existsByStudentAndCourse(User student, Course course);

    // FIX: Changed to findFirstBy to prevent NonUniqueResultException
    Optional<CourseProgress> findFirstByStudentAndCourse(User student, Course course);
}