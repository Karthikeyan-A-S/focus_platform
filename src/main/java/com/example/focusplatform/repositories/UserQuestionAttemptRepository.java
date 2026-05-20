package com.example.focusplatform.repositories;

import com.example.focusplatform.entities.UserQuestionAttempt;
import com.example.focusplatform.repository.projection.CourseBreakdownProjection;
import com.example.focusplatform.repository.projection.StudentCourseAggregateProjection;
import com.example.focusplatform.repository.projection.StudentOverallAggregateProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQuestionAttemptRepository extends JpaRepository<UserQuestionAttempt, Long> {

    @Query("""
            SELECT
                COALESCE(COUNT(a), 0) AS questionsAttempted,
                COALESCE(SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END), 0) AS correctCount,
                COALESCE(SUM(CASE WHEN a.correct = false THEN 1 ELSE 0 END), 0) AS wrongCount,
                COALESCE(SUM(a.timeTakenMs), 0) AS totalTimeMs
            FROM UserQuestionAttempt a
            WHERE a.student.id = :studentId
            """)
    Optional<StudentOverallAggregateProjection> aggregateOverallByStudent(@Param("studentId") Long studentId);

    @Query("""
            SELECT
                a.course.id AS courseId,
                a.course.title AS courseTitle,
                COUNT(a) AS questionsAttempted,
                SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END) AS correctCount,
                SUM(CASE WHEN a.correct = false THEN 1 ELSE 0 END) AS wrongCount,
                SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END) AS problemsSolved,
                COALESCE(SUM(a.timeTakenMs), 0) AS totalTimeMs
            FROM UserQuestionAttempt a
            WHERE a.student.id = :studentId
            GROUP BY a.course.id, a.course.title
            ORDER BY a.course.title
            """)
    List<CourseBreakdownProjection> aggregateByStudentGroupedByCourse(@Param("studentId") Long studentId);

    @Query("""
            SELECT
                a.student.id AS studentId,
                a.student.name AS studentName,
                a.student.email AS studentEmail,
                COUNT(a) AS questionsAttempted,
                SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END) AS correctCount,
                SUM(CASE WHEN a.correct = false THEN 1 ELSE 0 END) AS wrongCount,
                COALESCE(SUM(a.timeTakenMs), 0) AS totalTimeMs
            FROM UserQuestionAttempt a
            WHERE a.course.id = :courseId
            GROUP BY a.student.id, a.student.name, a.student.email
            """)
    List<StudentCourseAggregateProjection> aggregateByCourse(@Param("courseId") Long courseId);
}
