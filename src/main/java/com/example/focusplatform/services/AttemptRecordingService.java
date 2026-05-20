package com.example.focusplatform.services;

import com.example.focusplatform.entities.Course;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.entities.User;
import com.example.focusplatform.entities.UserQuestionAttempt;
import com.example.focusplatform.repositories.UserQuestionAttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AttemptRecordingService {

    private static final Logger log = LoggerFactory.getLogger(AttemptRecordingService.class);

    private final UserQuestionAttemptRepository attemptRepository;

    public AttemptRecordingService(UserQuestionAttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Transactional
    public void recordAttempt(User student, Course course, Question question, boolean correct, Long timeTakenMs) {
        UserQuestionAttempt attempt = new UserQuestionAttempt();
        attempt.setStudent(student);
        attempt.setCourse(course);
        attempt.setQuestion(question);
        attempt.setCorrect(correct);
        attempt.setTimeTakenMs(timeTakenMs != null ? timeTakenMs : 0L);
        attempt.setAttemptedAt(LocalDateTime.now());
        attemptRepository.save(attempt);
        log.debug("Recorded attempt student={} course={} question={} correct={}",
                student.getId(), course.getId(), question.getId(), correct);
    }
}
