package com.example.focusplatform.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntryResponse {
    private int rank;
    private Long studentId;
    private String studentName;
    private long questionsAttempted;
    private long correctCount;
    private long totalTimeMs;
}
