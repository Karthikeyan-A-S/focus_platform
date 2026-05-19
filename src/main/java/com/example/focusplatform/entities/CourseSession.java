package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "course_sessions")
public class CourseSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Calculated natively on the backend when the session stops
    private Long durationSeconds;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<QuizResponse> quizResponses;
}