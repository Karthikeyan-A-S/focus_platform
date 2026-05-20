package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.QuizAnswerRequest;
import com.example.focusplatform.dto.QuizAnswerResponse;
import com.example.focusplatform.dto.QuizSubmitRequest;
import com.example.focusplatform.services.QuizService;
import com.example.focusplatform.services.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student/quiz")
public class QuizController {

    private final QuizService quizService;
    private final StudentService studentService;

    public QuizController(QuizService quizService, StudentService studentService) {
        this.quizService = quizService;
        this.studentService = studentService;
    }

    /**
     * Full quiz submission (grades, persists {@link com.example.focusplatform.entities.StudentAnswer} rows, updates course progress).
     */
    @PostMapping("/submit")
    public ResponseEntity<String> submitQuiz(@RequestBody QuizSubmitRequest request, Authentication authentication) {
        try {
            String result = studentService.submitQuiz(authentication.getName(), request);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Starts a timed quiz session so individual answers can be stored in {@code quiz_responses}.
     */
    @PostMapping("/session/start")
    public ResponseEntity<Map<String, Long>> startQuizSession(
            @RequestParam Long courseId,
            Authentication authentication) {
        long sessionId = studentService.startQuizSession(authentication.getName(), courseId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }

    /**
     * Single-question grading (requires an active session from {@code /session/start}).
     */
    @PostMapping("/answer")
    public ResponseEntity<QuizAnswerResponse> submitAnswer(@RequestBody QuizAnswerRequest request) {
        try {
            boolean isCorrect = quizService.submitAnswer(request);
            String message = isCorrect ? "Correct! Great job." : "Incorrect. Try again!";

            return ResponseEntity.ok(new QuizAnswerResponse(isCorrect, message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new QuizAnswerResponse(false, e.getMessage()));
        }
    }
}
