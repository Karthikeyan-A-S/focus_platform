package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.LeaderboardEntryResponse;
import com.example.focusplatform.dto.StudentAnalyticsResponse;
import com.example.focusplatform.dto.TeacherCourseStatsResponse;
import com.example.focusplatform.services.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/student")
    public ResponseEntity<StudentAnalyticsResponse> getStudentAnalytics(Authentication authentication) {
        return ResponseEntity.ok(analyticsService.getStudentAnalytics(authentication.getName()));
    }

    @GetMapping("/teacher/courses/{courseId}")
    public ResponseEntity<TeacherCourseStatsResponse> getTeacherCourseStats(
            @PathVariable Long courseId,
            Authentication authentication) {
        return ResponseEntity.ok(
                analyticsService.getTeacherCourseStats(authentication.getName(), courseId));
    }

    @GetMapping("/leaderboard/courses/{courseId}")
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(@PathVariable Long courseId) {
        return ResponseEntity.ok(analyticsService.getCourseLeaderboard(courseId));
    }
}
