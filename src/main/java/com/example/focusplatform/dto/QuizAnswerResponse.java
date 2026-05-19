package com.example.focusplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuizAnswerResponse {
    private boolean correct;
    private String message;
}