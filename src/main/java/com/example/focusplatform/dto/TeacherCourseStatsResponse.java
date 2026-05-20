package com.example.focusplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeacherCourseStatsResponse {
    private Long courseId;
    private String courseTitle;
    /** All students enrolled in the course classroom (includes zero-attempt students). */
    private long totalEnrolledStudents;
    private List<StudentCourseStatEntry> students;

    @Data
    @Builder
    public static class StudentCourseStatEntry {
        private int rank;
        private Long studentId;
        private String studentName;
        private String studentEmail;
        private long questionsAttempted;
        private long correctCount;
        private long wrongCount;
        private long totalTimeMs;
    }
}
