package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.EnrollmentRequest;
import com.example.focusplatform.dto.QuizSubmitRequest;
import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.services.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enroll(@RequestBody EnrollmentRequest request, Authentication authentication) {
        String studentEmail = authentication.getName();
        Classroom classroom = studentService.enrollInClassroom(studentEmail, request.getInviteCode());
        return ResponseEntity.ok("Successfully enrolled in classroom: " + classroom.getName());
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitQuiz(@RequestBody QuizSubmitRequest request, Authentication authentication) {
        String studentEmail = authentication.getName();
        String result = studentService.submitQuiz(studentEmail, request);
        return ResponseEntity.ok(result);
    }
}