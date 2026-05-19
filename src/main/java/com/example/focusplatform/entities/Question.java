package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String questionText;

    // The true answer, kept strictly on the backend
    private String correctAnswer;

    // Store options as a JSON string or comma-separated values
    @Column(columnDefinition = "TEXT")
    private String options;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}