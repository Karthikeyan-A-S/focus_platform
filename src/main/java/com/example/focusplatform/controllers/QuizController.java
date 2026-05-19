package com.example.focusplatform.controllers;

import com.example.focusplatform.dto.QuizAnswerRequest;
import com.example.focusplatform.dto.QuizAnswerResponse;
import com.example.focusplatform.services.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/submit")
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