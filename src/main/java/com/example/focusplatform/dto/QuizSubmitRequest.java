package com.example.focusplatform.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Map;

@JsonDeserialize(using = QuizSubmitRequestDeserializer.class)
public class QuizSubmitRequest {
    private Long courseId;
    // Maps the Question ID to the selected Option String
    private Map<Long, String> answers;

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Map<Long, String> getAnswers() { return answers; }
    public void setAnswers(Map<Long, String> answers) { this.answers = answers; }
}