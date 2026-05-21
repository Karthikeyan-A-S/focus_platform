package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findByCourseId(Long courseId);

    @Modifying
    @Transactional
    void deleteByCourseId(Long courseId);
}