package com.example.focusplatform.dto;

import lombok.Data;

@Data
public class QuestionCreateRequest {
    private String questionText;
    private String correctAnswer;
    private String options;
    private Long courseId;
}