package com.example.focusplatform.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    // "Back" side — CourseContent points to Course, Course owns the list
    @JsonBackReference("course-contents")
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}