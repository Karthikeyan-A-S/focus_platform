package com.example.focusplatform.dto;

import lombok.Data;

@Data
public class QuizAnswerRequest {
    private Long sessionId;
    private Long questionId;
    private String answer;
}