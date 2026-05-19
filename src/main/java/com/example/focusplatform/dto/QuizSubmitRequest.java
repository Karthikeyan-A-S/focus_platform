package com.example.focusplatform.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizSubmitRequest {
    private Long courseId;
    // This Map links the Question's Database ID to the Student's String Answer
    private Map<Long, String> answers;
}