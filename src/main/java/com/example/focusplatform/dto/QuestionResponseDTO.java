package com.example.focusplatform.dto;

import com.example.focusplatform.entities.Question;
import lombok.Data;

/**
 * Teacher-facing question payload (includes correct option for editing).
 */
@Data
public class QuestionResponseDTO {
    private Long id;
    private String questionText;
    private String options;
    /** Correct choice key: A, B, C, or D */
    private String correctOption;
    private Long courseId;

    public static QuestionResponseDTO from(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptions(question.getOptions());
        dto.setCorrectOption(question.getCorrectAnswer());
        dto.setCourseId(question.getCourse() != null ? question.getCourse().getId() : null);
        return dto;
    }
}
