package com.example.focusplatform.services;

import com.example.focusplatform.dto.QuizAnswerRequest;
import com.example.focusplatform.entities.CourseSession;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.entities.QuizResponse;
import com.example.focusplatform.repositories.CourseProgressRepository;
import com.example.focusplatform.repositories.CourseSessionRepository;
import com.example.focusplatform.repositories.QuestionRepository;
import com.example.focusplatform.repositories.QuizResponseRepository;
import com.example.focusplatform.util.QuestionOptionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class QuizService {

    private final QuizResponseRepository quizResponseRepository;
    private final CourseSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRecordingService attemptRecordingService;
    private final CourseProgressRepository courseProgressRepository;

    public QuizService(QuizResponseRepository quizResponseRepository,
                       CourseSessionRepository sessionRepository,
                       QuestionRepository questionRepository,
                       AttemptRecordingService attemptRecordingService,
                       CourseProgressRepository courseProgressRepository) {
        this.quizResponseRepository = quizResponseRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.attemptRecordingService = attemptRecordingService;
        this.courseProgressRepository = courseProgressRepository;
    }

    @Transactional
    public boolean submitAnswer(QuizAnswerRequest request) {
        // 1. Fetch the active timer session
        CourseSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // --- FIXED: THE SECURITY LOCK ---
        // Changed to existsByStudentIdAndCourseId to match your repository exactly!
        if (courseProgressRepository.existsByStudentIdAndCourseId(session.getStudent().getId(), session.getCourse().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You have already completed this course. Further submissions are locked.");
        }
        // ------------------------------

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
        boolean isCorrect = QuestionOptionUtil.optionsMatch(
                question.getCorrectAnswer(),
                request.getAnswer());

        LocalDateTime answeredAt = LocalDateTime.now();
        LocalDateTime from = quizResponseRepository
                .findTopBySessionIdOrderByAttemptTimestampDesc(session.getId())
                .map(QuizResponse::getAttemptTimestamp)
                .orElse(session.getStartTime());
        long timeTakenMs = Duration.between(from, answeredAt).toMillis();

        QuizResponse response = new QuizResponse();
        response.setSession(session);
        response.setQuestion(question);
        response.setStudentAnswer(QuestionOptionUtil.normalizeSelectedOption(request.getAnswer()));
        response.setIsCorrect(isCorrect);
        response.setAttemptTimestamp(answeredAt);

        quizResponseRepository.save(response);

        attemptRecordingService.recordAttempt(
                session.getStudent(),
                session.getCourse(),
                question,
                isCorrect,
                timeTakenMs);

        return isCorrect;
    }
}