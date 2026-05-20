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
import java.util.Map;

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
    public ResponseEntity<?> createQuestion(@RequestBody QuestionCreateRequest request) {
        try {
            Question question = teacherService.createQuestion(request);
            return ResponseEntity.ok(QuestionResponseDTO.from(question));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ── GET Methods (Fetching Data) ─────────────────────────────────────────

    @GetMapping("/classrooms")
    public ResponseEntity<List<ClassroomSummaryDTO>> getTeacherClassrooms(Principal principal) {
        List<ClassroomSummaryDTO> classrooms = teacherService.getClassroomsByTeacher(principal.getName());
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/classrooms/{classroomId}/courses")
    public ResponseEntity<List<Course>> getCoursesByClassroom(@PathVariable Long classroomId) {
        List<Course> courses = teacherService.getCoursesByClassroom(classroomId);
        return ResponseEntity.ok(courses);
    }

    /** Full student profiles for a classroom (not IDs only). */
    @GetMapping("/classrooms/{classroomId}/students")
    public ResponseEntity<List<StudentSummaryDTO>> getClassroomStudents(
            @PathVariable Long classroomId,
            Principal principal) {
        return ResponseEntity.ok(teacherService.getClassroomStudents(principal.getName(), classroomId));
    }

    @DeleteMapping("/classrooms/{classroomId}/students/{studentId}")
    public ResponseEntity<Void> removeStudentFromClassroom(
            @PathVariable Long classroomId,
            @PathVariable Long studentId,
            Principal principal) {
        teacherService.removeStudentFromClassroom(principal.getName(), classroomId, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/{courseId}/questions")
    public ResponseEntity<List<QuestionResponseDTO>> getQuestionsByCourse(@PathVariable Long courseId) {
        List<QuestionResponseDTO> questions = teacherService.getQuestionsByCourse(courseId).stream()
                .map(QuestionResponseDTO::from)
                .toList();
        return ResponseEntity.ok(questions);
    }

    // ── PUT / PATCH / DELETE Methods (Updating & Deleting) ──────────────────

    // -- CLASSROOM --

    @PutMapping("/classrooms/{id}")
    public ResponseEntity<Classroom> updateClassroomFull(@PathVariable Long id, @RequestBody ClassroomCreateRequest request) {
        Classroom updated = teacherService.updateClassroom(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/classrooms/{id}")
    public ResponseEntity<Classroom> updateClassroomPartial(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Classroom updated = teacherService.patchClassroom(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/classrooms/{id}")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        teacherService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    // -- COURSE --

    @PutMapping("/courses/{id}")
    public ResponseEntity<Course> updateCourseFull(@PathVariable Long id, @RequestBody CourseCreateRequest request) {
        Course updated = teacherService.updateCourse(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/courses/{id}")
    public ResponseEntity<Course> updateCoursePartial(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Course updated = teacherService.patchCourse(id, updates);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        teacherService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    // -- QUESTION --

    @PutMapping("/questions/{id}")
    public ResponseEntity<?> updateQuestionFull(@PathVariable Long id, @RequestBody QuestionCreateRequest request) {
        try {
            Question updated = teacherService.updateQuestion(id, request);
            return ResponseEntity.ok(QuestionResponseDTO.from(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/questions/{id}")
    public ResponseEntity<?> updateQuestionPartial(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            Question updated = teacherService.patchQuestion(id, updates);
            return ResponseEntity.ok(QuestionResponseDTO.from(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        teacherService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}