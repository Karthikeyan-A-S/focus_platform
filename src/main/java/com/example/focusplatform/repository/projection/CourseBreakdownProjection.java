package com.example.focusplatform.repository.projection;

public interface CourseBreakdownProjection {
    Long getCourseId();
    String getCourseTitle();
    Long getQuestionsAttempted();
    Long getCorrectCount();
    Long getWrongCount();
    Long getProblemsSolved();
    Long getTotalTimeMs();
}
