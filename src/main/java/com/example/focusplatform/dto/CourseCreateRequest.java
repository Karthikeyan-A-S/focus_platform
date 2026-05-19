package com.example.focusplatform.dto;

import lombok.Data;

@Data
public class CourseCreateRequest {
    private String title;
    private String description;
    private Long classroomId;
}