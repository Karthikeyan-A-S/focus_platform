package com.example.focusplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentAnalyticsResponse {
    private long totalQuestionsAttempted;
    private long totalCorrect;
    private long totalWrong;
    private long totalTimeMs;
    private List<CourseAnalyticsItem> byCourse;

    @Data
    @Builder
    public static class CourseAnalyticsItem {
        private Long courseId;
        private String courseTitle;
        private long questionsAttempted;
        private long correctCount;
        private long wrongCount;
        private long problemsSolved;
        private long totalTimeMs;
    }
}
