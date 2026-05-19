package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "course_progress")
@Data
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User student;

    @ManyToOne
    private Course course;

    private Double score;
    private boolean isCompleted;
}