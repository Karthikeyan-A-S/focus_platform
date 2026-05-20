package com.example.focusplatform.repository.projection;

public interface StudentOverallAggregateProjection {
    Long getQuestionsAttempted();
    Long getCorrectCount();
    Long getWrongCount();
    Long getTotalTimeMs();
}
