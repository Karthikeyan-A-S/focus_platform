package com.example.focusplatform.dto;

import com.example.focusplatform.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;          // NEW
    private String name;
    private String email;     // NEW
    private Role role;
}