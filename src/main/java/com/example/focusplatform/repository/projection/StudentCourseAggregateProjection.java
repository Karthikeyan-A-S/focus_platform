package com.example.focusplatform.repository.projection;

public interface StudentCourseAggregateProjection {
    Long getStudentId();
    String getStudentName();
    String getStudentEmail();
    Long getQuestionsAttempted();
    Long getCorrectCount();
    Long getWrongCount();
    Long getTotalTimeMs();
}
