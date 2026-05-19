package com.example.focusplatform.entities;
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

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Classroom classroom;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<CourseContent> contents;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Question> questions;
}