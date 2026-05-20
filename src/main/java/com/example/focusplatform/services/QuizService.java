package com.example.focusplatform.services;

import com.example.focusplatform.dto.QuizAnswerRequest;
import com.example.focusplatform.entities.CourseSession;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.entities.QuizResponse;
import com.example.focusplatform.repositories.CourseSessionRepository;
import com.example.focusplatform.repositories.QuestionRepository;
import com.example.focusplatform.repositories.QuizResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class QuizService {

    private final QuizResponseRepository quizResponseRepository;
    private final CourseSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;

    public QuizService(QuizResponseRepository quizResponseRepository,
                       CourseSessionRepository sessionRepository,
                       QuestionRepository questionRepository) {
        this.quizResponseRepository = quizResponseRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public boolean submitAnswer(QuizAnswerRequest request) {
        // 1. Fetch the active timer session
        CourseSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getEndTime() != null) {
            throw new RuntimeException("Cannot submit answers to a closed session");
        }

        // 2. Fetch the question to get the real answer
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // 3. Grade the answer (ignoring upper/lower case)
        if (request.getAnswer() == null || question.getCorrectAnswer() == null) {
            throw new RuntimeException("Answer text is required");
        }
        if (!question.getCourse().getId().equals(session.getCourse().getId())) {
            throw new RuntimeException("Question does not belong to this quiz session");
        }
        boolean isCorrect = question.getCorrectAnswer().trim().equalsIgnoreCase(request.getAnswer().trim());

        // 4. Save the result to the database for analytics later
        QuizResponse response = new QuizResponse();
        response.setSession(session);
        response.setQuestion(question);
        response.setStudentAnswer(request.getAnswer());
        response.setIsCorrect(isCorrect);
        response.setAttemptTimestamp(LocalDateTime.now());

        quizResponseRepository.save(response);

        return isCorrect;
    }
}