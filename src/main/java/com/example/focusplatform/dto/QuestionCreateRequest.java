package com.example.focusplatform.dto;

import lombok.Data;

@Data
public class QuestionCreateRequest {
    private String questionText;
    /** Option letter the teacher selects: A, B, C, or D */
    private String correctOption;
    /** @deprecated Use {@link #correctOption} */
    private String correctAnswer;
    private String options;
    private Long courseId;

    /** Resolves correctOption or legacy correctAnswer field. */
    public String resolvedCorrectOption() {
        if (correctOption != null && !correctOption.isBlank()) {
            return correctOption;
        }
        return correctAnswer;
    }
}