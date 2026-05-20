package com.example.focusplatform.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links this answer to the specific student's attempt
    @ManyToOne
    @JoinColumn(name = "progress_id")
    private CourseProgress courseProgress;

    // Links this answer to the specific question asked
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    // What the student actually clicked
    private String selectedOption;

    // Whether it was right or wrong (avoid "is*" boolean field names — Lombok/Hibernate property quirks)
    @Column(name = "is_correct", nullable = false)
    private boolean correct;
}