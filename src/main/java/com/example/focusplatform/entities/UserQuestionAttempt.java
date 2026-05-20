package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_question_attempts", indexes = {
        @Index(name = "idx_attempt_student_course", columnList = "student_id,course_id"),
        @Index(name = "idx_attempt_course", columnList = "course_id")
})
public class UserQuestionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private boolean correct;

    /** Time spent on this question in milliseconds. */
    @Column(name = "time_taken_ms")
    private Long timeTakenMs;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;
}
