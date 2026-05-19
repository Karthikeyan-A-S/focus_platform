package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "course_contents")
public class CourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentType; // e.g., "VIDEO", "PDF", "TEXT"

    @Column(columnDefinition = "TEXT")
    private String bodyText;

    private String mediaUrl;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}