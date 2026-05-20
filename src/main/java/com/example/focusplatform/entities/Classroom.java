package com.example.focusplatform.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "classes")
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String inviteCode;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    // Students — ignore to avoid User ↔ Classroom loop
    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "enrollments",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    private List<User> students;

    // "Managed" side — Classroom owns this list, Course points back with @JsonBackReference
    @JsonManagedReference("classroom-courses")
    @OneToMany(mappedBy = "classroom", cascade = CascadeType.ALL)
    private List<Course> courses;
}