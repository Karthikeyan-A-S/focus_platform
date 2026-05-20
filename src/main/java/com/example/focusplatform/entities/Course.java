package com.example.focusplatform.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    // "Back" side — Course points to Classroom, Classroom owns the list
    @JsonBackReference("classroom-courses")
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Classroom classroom;

    // "Managed" side — Course owns contents list
    @JsonManagedReference("course-contents")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CourseContent> contents;

    // "Managed" side — Course owns questions list
    @JsonManagedReference("course-questions")
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Question> questions;
}