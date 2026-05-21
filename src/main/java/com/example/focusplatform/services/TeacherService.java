package com.example.focusplatform.services;

import com.example.focusplatform.dto.*;
import com.example.focusplatform.entities.*;
import com.example.focusplatform.exception.AccessDeniedException;
import com.example.focusplatform.exception.ResourceNotFoundException;
import com.example.focusplatform.repositories.ClassroomRepository;
import com.example.focusplatform.repositories.CourseContentRepository;
import com.example.focusplatform.repositories.CourseProgressRepository;
import com.example.focusplatform.repositories.CourseRepository;
import com.example.focusplatform.repositories.CourseSessionRepository;
import com.example.focusplatform.repositories.QuestionRepository;
import com.example.focusplatform.repositories.QuizResponseRepository;
import com.example.focusplatform.repositories.UserQuestionAttemptRepository;
import com.example.focusplatform.repositories.UserRepository;
import com.example.focusplatform.util.QuestionOptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final ClassroomRepository classroomRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseContentRepository contentRepository;
    private final QuestionRepository questionRepository;

    // --- Repositories needed for safe cascading deletes ---
    private final UserQuestionAttemptRepository attemptRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final QuizResponseRepository quizResponseRepository;

    public TeacherService(ClassroomRepository classroomRepository,
                          CourseRepository courseRepository,
                          UserRepository userRepository,
                          CourseContentRepository contentRepository,
                          QuestionRepository questionRepository,
                          UserQuestionAttemptRepository attemptRepository,
                          CourseProgressRepository courseProgressRepository,
                          CourseSessionRepository courseSessionRepository,
                          QuizResponseRepository quizResponseRepository) {
        this.classroomRepository = classroomRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.courseProgressRepository = courseProgressRepository;
        this.courseSessionRepository = courseSessionRepository;
        this.quizResponseRepository = quizResponseRepository;
    }

    // ==========================================
    // CREATION METHODS
    // ==========================================

    public Classroom createClassroom(String teacherEmail, ClassroomCreateRequest request) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Classroom classroom = new Classroom();
        classroom.setName(request.getName());
        classroom.setTeacher(teacher);
        classroom.setInviteCode(UUID.randomUUID().toString().substring(0, 6).toUpperCase());

        return classroomRepository.save(classroom);
    }

    public Course createCourse(CourseCreateRequest request) {
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setClassroom(classroom);

        return courseRepository.save(course);
    }

    public CourseContent createContent(ContentCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseContent content = new CourseContent();
        content.setContentType(request.getContentType());
        content.setBodyText(request.getBodyText());
        content.setCourse(course);

        return contentRepository.save(content);
    }

    public Question createQuestion(QuestionCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setCorrectAnswer(normalizeCorrectOption(request.resolvedCorrectOption()));
        question.setOptions(request.getOptions());
        question.setCourse(course);

        return questionRepository.save(question);
    }

    // ==========================================
    // FETCH METHODS
    // ==========================================

    public List<ClassroomSummaryDTO> getClassroomsByTeacher(String email) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<Classroom> classrooms = classroomRepository.findByTeacher(teacher);

        return classrooms.stream().map(classroom -> {
            ClassroomSummaryDTO dto = new ClassroomSummaryDTO();
            dto.setId(classroom.getId());
            dto.setName(classroom.getName());
            dto.setInviteCode(classroom.getInviteCode());
            dto.setTeacherName(classroom.getTeacher().getName());
            return dto;
        }).toList(); // Replaced collect(Collectors.toList())
    }

    public List<Course> getCoursesByClassroom(Long classroomId) {
        return courseRepository.findByClassroomId(classroomId);
    }

    public List<Question> getQuestionsByCourse(Long courseId) {
        return questionRepository.findByCourseId(courseId);
    }

    public List<StudentSummaryDTO> getClassroomStudents(String teacherEmail, Long classroomId) {
        Classroom classroom = requireTeacherClassroom(teacherEmail, classroomId);
        if (classroom.getStudents() == null || classroom.getStudents().isEmpty()) {
            return List.of();
        }
        return classroom.getStudents().stream()
                .sorted(Comparator.comparing(User::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(StudentSummaryDTO::from)
                .toList(); // Replaced collect(Collectors.toList())
    }

    public void removeStudentFromClassroom(String teacherEmail, Long classroomId, Long studentId) {
        Classroom classroom = requireTeacherClassroom(teacherEmail, classroomId);
        userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        if (classroom.getStudents() == null || !classroom.getStudents().removeIf(s -> s.getId().equals(studentId))) {
            throw new ResourceNotFoundException("Student is not enrolled in this classroom");
        }
        classroomRepository.save(classroom);
    }

    private Classroom requireTeacherClassroom(String teacherEmail, Long classroomId) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        if (classroom.getTeacher() == null || !classroom.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("You do not own this classroom");
        }
        return classroom;
    }

    // ==========================================
    // UPDATE & DELETE: COURSES
    // ==========================================

    public Course updateCourse(Long id, CourseCreateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        return courseRepository.save(course);
    }

    public Course patchCourse(Long id, Map<String, Object> updates) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (updates.containsKey("title"))       course.setTitle((String) updates.get("title"));
        if (updates.containsKey("description")) course.setDescription((String) updates.get("description"));

        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found");
        }
        deleteCourseData(id);
        courseRepository.deleteById(id);
    }

    // ==========================================
    // UPDATE & DELETE: CLASSROOMS
    // ==========================================

    public Classroom updateClassroom(Long id, ClassroomCreateRequest request) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        classroom.setName(request.getName());
        return classroomRepository.save(classroom);
    }

    public Classroom patchClassroom(Long id, Map<String, Object> updates) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        if (updates.containsKey("name")) classroom.setName((String) updates.get("name"));

        return classroomRepository.save(classroom);
    }

    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classroom not found"));

        List<Course> courses = courseRepository.findByClassroomId(id);
        for (Course course : courses) {
            deleteCourseData(course.getId());
        }
        courseRepository.deleteAll(courses);

        classroom.getStudents().clear();
        classroomRepository.save(classroom);

        classroomRepository.deleteById(id);
    }

    // ==========================================
    // UPDATE & DELETE: QUESTIONS
    // ==========================================

    public Question updateQuestion(Long id, QuestionCreateRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        question.setQuestionText(request.getQuestionText());
        question.setOptions(request.getOptions());
        question.setCorrectAnswer(normalizeCorrectOption(request.resolvedCorrectOption()));

        return questionRepository.save(question);
    }

    public Question patchQuestion(Long id, Map<String, Object> updates) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (updates.containsKey("questionText")) question.setQuestionText((String) updates.get("questionText"));
        if (updates.containsKey("options"))      question.setOptions((String) updates.get("options"));
        if (updates.containsKey("correctOption")) {
            question.setCorrectAnswer(normalizeCorrectOption((String) updates.get("correctOption")));
        } else if (updates.containsKey("correctAnswer")) {
            question.setCorrectAnswer(normalizeCorrectOption((String) updates.get("correctAnswer")));
        }

        return questionRepository.save(question);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found");
        }
        deleteQuestionData(id);
        questionRepository.deleteById(id);
    }

    // ==========================================
    // UPDATE & DELETE: CONTENT
    // ==========================================

    public CourseContent updateContent(Long contentId, ContentCreateRequest request) {
        // FIX: Replaced CourseContentRepository with the injected contentRepository variable
        CourseContent content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found with id: " + contentId));

        content.setBodyText(request.getBodyText());
        return contentRepository.save(content);
    }

    public void deleteContent(Long contentId) {
        // FIX: Replaced courseContentRepository with the injected contentRepository variable
        if (!contentRepository.existsById(contentId)) {
            throw new RuntimeException("Content not found with id: " + contentId);
        }
        contentRepository.deleteById(contentId);
    }

    // ==========================================
    // PRIVATE HELPERS — cascading delete logic
    // ==========================================

    private void deleteQuestionData(Long questionId) {
        studentAnswerRepository_deleteByQuestionId(questionId);
        quizResponseRepository.deleteByQuestionId(questionId);
        attemptRepository.deleteByQuestionId(questionId);
    }

    private void deleteCourseData(Long courseId) {
        List<Long> questionIds = questionRepository.findByCourseId(courseId)
                .stream().map(Question::getId).toList();

        for (Long qid : questionIds) {
            studentAnswerRepository_deleteByQuestionId(qid);
            quizResponseRepository.deleteByQuestionId(qid);
        }

        quizResponseRepository.deleteBySessionCourseId(courseId);
        attemptRepository.deleteByCourseId(courseId);
        courseSessionRepository.deleteByCourseId(courseId);
        courseProgressRepository.deleteByCourseId(courseId);
        contentRepository.deleteByCourseId(courseId);
        questionRepository.deleteByCourseId(courseId);
    }

    /*
     * Inline shim for StudentAnswer deletion.
     * Changed from /** to /* to avoid IDE parsing warnings for nested annotations.
     */
    private void studentAnswerRepository_deleteByQuestionId(Long questionId) {
        courseProgressRepository.deleteStudentAnswersByQuestionId(questionId);
    }

    private static String normalizeCorrectOption(String value) {
        try {
            return QuestionOptionUtil.normalizeCorrectOption(value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Search for students by name for the autocomplete dropdown
    public List<StudentSummaryDTO> searchStudents(String query) {
        return userRepository.findByNameContainingIgnoreCaseAndRole(query, Role.STUDENT)
                .stream()
                .map(StudentSummaryDTO::from)
                .toList();
    }

    // Add a specific student to the classroom
    public void addStudentToClassroom(String teacherEmail, Long classroomId, Long studentId) {
        Classroom classroom = requireTeacherClassroom(teacherEmail, classroomId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Only add them if they aren't already in the classroom
        if (!classroom.getStudents().contains(student)) {
            classroom.getStudents().add(student);
            classroomRepository.save(classroom);
        }
    }
}