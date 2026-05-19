package com.example.focusplatform.dto;

import com.example.focusplatform.entities.Role;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private Role role;
}