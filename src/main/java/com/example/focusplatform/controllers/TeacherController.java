package com.example.focusplatform.controllers;

// 1. Existing DTO and Entity Imports
import com.example.focusplatform.dto.ClassroomCreateRequest;
import com.example.focusplatform.dto.CourseCreateRequest;
import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.Course;

// 2. NEW Imports added to fix your compilation errors
import com.example.focusplatform.dto.ContentCreateRequest;
import com.example.focusplatform.dto.QuestionCreateRequest;
import com.example.focusplatform.entities.CourseContent;
import com.example.focusplatform.entities.Question;

// 3. Spring and Security Imports
import com.example.focusplatform.services.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @PostMapping("/classrooms")
    public ResponseEntity<Classroom> createClassroom(@RequestBody ClassroomCreateRequest request, Authentication authentication) {
        // Extract the teacher's email directly from their JWT token
        String teacherEmail = authentication.getName();
        Classroom newClass = teacherService.createClassroom(teacherEmail, request);
        return ResponseEntity.ok(newClass);
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody CourseCreateRequest request) {
        Course newCourse = teacherService.createCourse(request);
        return ResponseEntity.ok(newCourse);
    }

    @PostMapping("/contents")
    public ResponseEntity<CourseContent> createContent(@RequestBody ContentCreateRequest request) {
        return ResponseEntity.ok(teacherService.createContent(request));
    }

    @PostMapping("/questions")
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionCreateRequest request) {
        return ResponseEntity.ok(teacherService.createQuestion(request));
    }
}