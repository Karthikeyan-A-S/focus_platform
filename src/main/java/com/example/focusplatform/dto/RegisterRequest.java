package com.example.focusplatform.dto;

import com.example.focusplatform.entities.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role; // STUDENT or TEACHER
}