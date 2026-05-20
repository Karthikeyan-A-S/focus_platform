package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.*;
import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.Course;
import com.example.focusplatform.entities.CourseContent;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.services.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    // ── POST Methods (Creation) ─────────────────────────────────────────────

    @PostMapping("/classrooms")
    public ResponseEntity<Classroom> createClassroom(
            Principal principal,
            @RequestBody ClassroomCreateRequest request) {
        Classroom classroom = teacherService.createClassroom(principal.getName(), request);
        return ResponseEntity.ok(classroom);
    }

    @PostMapping("/courses")
    public ResponseEntity<Course> createCourse(@RequestBody CourseCreateRequest request) {
        Course course = teacherService.createCourse(request);
        return ResponseEntity.ok(course);
    }

    @PostMapping("/content")
    public ResponseEntity<CourseContent> createContent(@RequestBody ContentCreateRequest request) {
        CourseContent content = teacherService.createContent(request);
        return ResponseEntity.ok(content);
    }

    @PostMapping("/questions")
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionCreateRequest request) {
        Question question = teacherService.createQuestion(request);
        return ResponseEntity.ok(question);
    }

    // ── GET Methods (Fetching Data) ─────────────────────────────────────────

    // Fetch all classrooms belonging to the logged-in teacher
    @GetMapping("/classrooms")
    public ResponseEntity<List<ClassroomSummaryDTO>> getTeacherClassrooms(Principal principal) {
        List<ClassroomSummaryDTO> classrooms = teacherService.getClassroomsByTeacher(principal.getName());
        return ResponseEntity.ok(classrooms);
    }

    // Fetch all courses for a specific classroom
    @GetMapping("/classrooms/{classroomId}/courses")
    public ResponseEntity<List<Course>> getCoursesByClassroom(@PathVariable Long classroomId) {
        List<Course> courses = teacherService.getCoursesByClassroom(classroomId);
        return ResponseEntity.ok(courses);
    }

    // Fetch all questions for a specific course
    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<List<Question>> getQuestionsByCourse(@PathVariable Long courseId) {
        List<Question> questions = teacherService.getQuestionsByCourse(courseId);
        return ResponseEntity.ok(questions);
    }
}