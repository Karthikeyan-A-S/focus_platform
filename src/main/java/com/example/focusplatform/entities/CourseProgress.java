package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    private LocalDateTime startedAt;

    // --- NEW FIELDS ---
    private LocalDateTime submittedAt;
    private Long durationSeconds;

    private boolean isCompleted = false;
    private Integer quizScore;
}