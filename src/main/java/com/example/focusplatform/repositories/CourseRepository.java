package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    // NEW: Fetch all courses that belong to a specific classroom
    List<Course> findByClassroomId(Long classroomId);
}