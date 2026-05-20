package com.example.focusplatform.dto;

import com.example.focusplatform.entities.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentSummaryDTO {
    private Long id;
    private String name;
    private String email;

    public static StudentSummaryDTO from(User user) {
        return StudentSummaryDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}
