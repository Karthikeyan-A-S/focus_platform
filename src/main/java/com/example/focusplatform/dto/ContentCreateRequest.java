package com.example.focusplatform.dto;

import lombok.Data;

@Data
public class ContentCreateRequest {
    private String contentType;
    private String bodyText;
    private Long courseId;
}