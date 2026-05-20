package com.example.focusplatform.services;

import com.example.focusplatform.dto.QuizSubmitRequest;
import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.Course;
import com.example.focusplatform.entities.CourseContent;
import com.example.focusplatform.entities.CourseProgress;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.entities.User;
import com.example.focusplatform.repositories.ClassroomRepository;
import com.example.focusplatform.repositories.CourseContentRepository;
import com.example.focusplatform.repositories.CourseProgressRepository;
import com.example.focusplatform.repositories.CourseRepository;
import com.example.focusplatform.repositories.QuestionRepository;
import com.example.focusplatform.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QuestionRepository questionRepository;
    private final CourseProgressRepository courseProgressRepository;
    // NEW: We need this to fetch the reading material!
    private final CourseContentRepository contentRepository;

    public StudentService(ClassroomRepository classroomRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          QuestionRepository questionRepository,
                          CourseProgressRepository courseProgressRepository,
                          CourseContentRepository contentRepository) {
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.questionRepository = questionRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.contentRepository = contentRepository;
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

    // --- NEW: The missing method to get content ---
    public List<CourseContent> getCourseContent(Long courseId) {
        return contentRepository.findByCourseId(courseId);
    }

    // --- NEW: The missing method to get questions ---
    public List<Question> getCourseQuestions(Long courseId) {
        return questionRepository.findByCourseId(courseId);
    }

    @Transactional
    public String submitQuiz(String studentEmail, QuizSubmitRequest request) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        int correctAnswers = 0;
        int totalQuestions = request.getAnswers().size();

        if (totalQuestions == 0) return "No answers submitted!";

        // Loop through the submitted answers and grade them
        for (Map.Entry<Long, String> entry : request.getAnswers().entrySet()) {
            Question q = questionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Question not found"));

            if (q.getCorrectAnswer().equalsIgnoreCase(entry.getValue())) {
                correctAnswers++;
            }
        }

        // Calculate percentage score
        double score = ((double) correctAnswers / totalQuestions) * 100;

        // Save progress to database
        CourseProgress progress = new CourseProgress();
        progress.setStudent(student);
        progress.setCourse(course);
        progress.setScore(score);
        progress.setCompleted(true);
        courseProgressRepository.save(progress);

        return "Course completed! You scored: " + score + "%";
    }
}