package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.EnrollmentRequest;
import com.example.focusplatform.dto.QuizSubmitRequest;
import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.CourseContent;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.services.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    // --- Track when a student starts the course ---
    @PostMapping("/courses/{courseId}/start")
    public ResponseEntity<String> startCourse(@PathVariable Long courseId, Authentication authentication) {
        String studentEmail = authentication.getName();
        studentService.markCourseAsStarted(studentEmail, courseId);
        return ResponseEntity.ok("Course started. Time recorded!");
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
        String studentEmail = authentication.getName();
        String result = studentService.submitQuiz(studentEmail, request);
        return ResponseEntity.ok(result);
    }
    // Add this inside your StudentController class
    @GetMapping("/my-classrooms")
    public ResponseEntity<List<Classroom>> getMyClassrooms(Authentication authentication) {
        return ResponseEntity.ok(studentService.getMyClassrooms(authentication.getName()));
    }
}