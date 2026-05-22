package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.EnrollmentRequest;
import com.example.focusplatform.dto.QuizSubmitRequest;
import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.CourseContent;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.services.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
    @PostMapping("/courses/{courseId}/start")
    public ResponseEntity<String> startCourse(@PathVariable Long courseId, Authentication authentication) {
        try {
            studentService.markCourseAsStarted(authentication.getName(), courseId);
            return ResponseEntity.ok("Course started. Time recorded!");
        } catch (ResponseStatusException e) {
            // Re-throw so Spring returns the correct HTTP status (409)
            throw e;
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Fetch Course Content
    @GetMapping("/courses/{courseId}/content")
    public ResponseEntity<List<CourseContent>> getCourseContent(@PathVariable Long courseId) {
        return ResponseEntity.ok(studentService.getCourseContent(courseId));
    }

    // Fetch Quiz Questions
    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<List<Question>> getCourseQuestions(@PathVariable Long courseId) {
        return ResponseEntity.ok(studentService.getCourseQuestions(courseId));
    }


    @PostMapping("/submit")
    public ResponseEntity<String> submitQuiz(@RequestBody QuizSubmitRequest request, Authentication authentication) {
        try {
            String result = studentService.submitQuiz(authentication.getName(), request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            // FIX: Return 409 specifically for already-completed so frontend can detect it
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("already completed")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    // Add this inside your StudentController class
    @GetMapping("/my-classrooms")
    public ResponseEntity<List<Classroom>> getMyClassrooms(Authentication authentication) {
        return ResponseEntity.ok(studentService.getMyClassrooms(authentication.getName()));
    }
}