package com.example.focusplatform.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    // Never expose the password hash in any API response
    @JsonIgnore
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    // Ignore to avoid User ↔ Classroom ↔ User loop
    @JsonIgnore
    @ManyToMany(mappedBy = "students")
    private List<Classroom> enrolledClasses;
}