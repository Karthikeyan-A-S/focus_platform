package com.example.focusplatform.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    // Keep the correct answer hidden from API responses
    @JsonIgnore
    private String correctAnswer;

    @Column(columnDefinition = "TEXT")
    private String options;

    // "Back" side — Question points to Course, Course owns the list
    @JsonBackReference("course-questions")
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}