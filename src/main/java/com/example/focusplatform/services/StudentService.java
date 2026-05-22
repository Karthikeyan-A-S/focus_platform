package com.example.focusplatform.services;

import com.example.focusplatform.dto.QuizSubmitRequest;
import com.example.focusplatform.entities.*;
import com.example.focusplatform.repositories.ClassroomRepository;
import com.example.focusplatform.repositories.CourseContentRepository;
import com.example.focusplatform.repositories.CourseProgressRepository;
import com.example.focusplatform.repositories.CourseRepository;
import com.example.focusplatform.repositories.CourseSessionRepository;
import com.example.focusplatform.repositories.QuestionRepository;
import com.example.focusplatform.repositories.UserRepository;
import com.example.focusplatform.util.QuestionOptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseContentRepository contentRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final AttemptRecordingService attemptRecordingService;

    public StudentService(ClassroomRepository classroomRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          QuestionRepository questionRepository,
                          CourseProgressRepository courseProgressRepository,
                          CourseContentRepository contentRepository,
                          CourseSessionRepository courseSessionRepository,
                          AttemptRecordingService attemptRecordingService) {
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.contentRepository = contentRepository;
        this.courseSessionRepository = courseSessionRepository;
        this.attemptRecordingService = attemptRecordingService;
    }

    @Transactional
    public Classroom enrollInClassroom(String studentEmail, String inviteCode) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Classroom classroom = classroomRepository.findByInviteCode(inviteCode.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid invite code"));

        if (!classroom.getStudents().contains(student)) {
            classroom.getStudents().add(student);
            classroomRepository.save(classroom);
        }
        return classroom;
    }

    public List<CourseContent> getCourseContent(Long courseId) {
        return contentRepository.findByCourseId(courseId);
    }

    public List<Question> getCourseQuestions(Long courseId) {
        return questionRepository.findByCourseId(courseId);
    }

    @Transactional
    public void markCourseAsStarted(String studentEmail, Long courseId) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check if they already started it so we don't overwrite their original start time
        boolean alreadyStarted = courseProgressRepository.existsByStudentAndCourse(student, course);

        if (!alreadyStarted) {
            CourseProgress progress = new CourseProgress();
            progress.setStudent(student);
            progress.setCourse(course);
            progress.setStartedAt(LocalDateTime.now()); // Notes the exact current time!

            courseProgressRepository.save(progress);
        }
    }

    @Transactional
    public long startQuizSession(String studentEmail, Long courseId) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseSession session = new CourseSession();
        session.setStudent(student);
        session.setCourse(course);
        session.setStartTime(LocalDateTime.now());

        return courseSessionRepository.save(session).getId();
    }

    @Transactional
    public String submitQuiz(String studentEmail, QuizSubmitRequest request) {
        if (request.getCourseId() == null) {
            throw new RuntimeException("courseId is required");
        }

        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // 1. FIX: Fetch the first progress record to prevent NonUniqueResultException
        CourseProgress progress = courseProgressRepository.findFirstByStudentAndCourse(student, course)
                .orElseThrow(() -> new RuntimeException("Course progress not found. Did you start the course?"));


        // 2. Prevent re-submission if they already completed it
        if (progress.isCompleted()) {
            throw new RuntimeException("You have already completed this course.");
        }

        // 3. Stamp the submission time
        progress.setSubmittedAt(LocalDateTime.now());

        // 4. Calculate the duration
        if (progress.getStartedAt() != null) {
            Duration duration = Duration.between(progress.getStartedAt(), progress.getSubmittedAt());
            progress.setDurationSeconds(duration.getSeconds());
        }

        // 5. Calculate Score & Record Individual Answers
        int correctAnswers = 0;
        int totalQuestions = request.getAnswers() != null ? request.getAnswers().size() : 0;

        long perQuestionMs = 0L;
        if (totalQuestions > 0 && progress.getDurationSeconds() != null && progress.getDurationSeconds() > 0) {
            perQuestionMs = (progress.getDurationSeconds() * 1000L) / totalQuestions;
        }

        if (totalQuestions > 0) {
            for (Map.Entry<Long, String> entry : request.getAnswers().entrySet()) {
                Question q = questionRepository.findById(entry.getKey())
                        .orElseThrow(() -> new RuntimeException("Question not found"));

                if (!q.getCourse().getId().equals(course.getId())) {
                    throw new RuntimeException("Question does not belong to this course");
                }

                if (entry.getValue() == null || entry.getValue().isBlank()) {
                    throw new RuntimeException("Missing answer for question " + entry.getKey());
                }

                String selectedOption;
                try {
                    selectedOption = QuestionOptionUtil.normalizeSelectedOption(entry.getValue().trim());
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(
                            "Invalid answer for question " + entry.getKey() + ": submit A, B, C, or D.");
                }
                boolean isCorrect = q.getCorrectAnswer() != null
                        && QuestionOptionUtil.optionsMatch(q.getCorrectAnswer(), selectedOption);

                if (isCorrect) {
                    correctAnswers++;
                }

                // --- NEW: Save the specific answer to the database! ---
                StudentAnswer answerRecord = new StudentAnswer();
                answerRecord.setCourseProgress(progress);
                answerRecord.setQuestion(q);
                answerRecord.setSelectedOption(selectedOption);
                answerRecord.setCorrect(isCorrect);

                // Add it to the progress list
                progress.getStudentAnswers().add(answerRecord);

                attemptRecordingService.recordAttempt(student, course, q, isCorrect, perQuestionMs);
            }
        }

        int score = totalQuestions == 0 ? 0 : (int) (((double) correctAnswers / totalQuestions) * 100);

        progress.setQuizScore(score);
        progress.setCompleted(true);

        // 6. Save the updated progress (Because of CascadeType.ALL, this automatically saves all the StudentAnswers too!)
        courseProgressRepository.save(progress);

        return "Quiz submitted! You took " + progress.getDurationSeconds() + " seconds and scored " + score + "%";
    }

    // Get classrooms for the student dashboard
    public List<Classroom> getMyClassrooms(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // This leverages the @ManyToMany mapping to automatically fetch classrooms where this student is enrolled
        return classroomRepository.findByStudentsContaining(student);
    }
}