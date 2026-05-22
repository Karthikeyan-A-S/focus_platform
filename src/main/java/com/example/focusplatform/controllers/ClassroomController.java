package com.example.focusplatform.controllers;

import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.User;
import com.example.focusplatform.repositories.ClassroomRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    private final ClassroomRepository classroomRepository;

    public ClassroomController(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @GetMapping("/{classroomId}/participants")
    public ResponseEntity<List<Map<String, Object>>> getClassroomParticipants(@PathVariable Long classroomId) {

        // 1. Fetch the classroom
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        List<Map<String, Object>> participants = new ArrayList<>();

        // 2. Add the Teacher (with a label so students know who it is)
        Map<String, Object> teacherMap = new HashMap<>();
        teacherMap.put("id", classroom.getTeacher().getId());
        teacherMap.put("name", classroom.getTeacher().getName() + " (Teacher)");
        participants.add(teacherMap);

        // 3. Add all Enrolled Students
        for (User student : classroom.getStudents()) {
            Map<String, Object> studentMap = new HashMap<>();
            studentMap.put("id", student.getId());
            studentMap.put("name", student.getName());
            participants.add(studentMap);
        }

        return ResponseEntity.ok(participants);
    }
}