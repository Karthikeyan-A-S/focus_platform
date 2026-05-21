package com.example.focusplatform.controllers;

import com.example.focusplatform.entities.User;
import com.example.focusplatform.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfileById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Build a secure profile map (Never send the password hash!)
        Map<String, Object> publicProfile = new HashMap<>();
        publicProfile.put("id", user.getId());
        publicProfile.put("name", user.getName());
        publicProfile.put("email", user.getEmail());
        publicProfile.put("role", user.getRole());

        return ResponseEntity.ok(publicProfile);
    }
}