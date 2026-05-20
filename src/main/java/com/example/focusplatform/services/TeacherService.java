package com.example.focusplatform.services;

import com.example.focusplatform.dto.*;
import com.example.focusplatform.entities.Classroom;
import com.example.focusplatform.entities.Course;
import com.example.focusplatform.entities.CourseContent;
import com.example.focusplatform.entities.Question;
import com.example.focusplatform.entities.User;
import com.example.focusplatform.repositories.ClassroomRepository;
import com.example.focusplatform.repositories.CourseContentRepository;
import com.example.focusplatform.repositories.CourseRepository;
import com.example.focusplatform.repositories.QuestionRepository;
import com.example.focusplatform.repositories.UserRepository;
import org.springframework.stereotype.Service;

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

    public TeacherService(ClassroomRepository classroomRepository,
                          CourseRepository courseRepository,
                          UserRepository userRepository,
                          CourseContentRepository contentRepository,
                          QuestionRepository questionRepository) {
        this.classroomRepository = classroomRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.contentRepository = contentRepository;
        this.questionRepository = questionRepository;
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
        // Generate a random 6-character alphanumeric code
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
        question.setCorrectAnswer(request.getCorrectAnswer());
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

        // Convert the full Classroom entities into lightweight DTOs
        return classrooms.stream().map(classroom -> {
            ClassroomSummaryDTO dto = new ClassroomSummaryDTO();
            dto.setId(classroom.getId());
            dto.setName(classroom.getName());
            dto.setInviteCode(classroom.getInviteCode());
            dto.setTeacherName(classroom.getTeacher().getName());
            return dto;
        }).collect(Collectors.toList());
    }

    public List<Course> getCoursesByClassroom(Long classroomId) {
        return courseRepository.findByClassroomId(classroomId);
    }

    public List<Question> getQuestionsByCourse(Long courseId) {
        return questionRepository.findByCourseId(courseId);
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

        if (updates.containsKey("title")) {
            course.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("description")) {
            course.setDescription((String) updates.get("description"));
        }

        return courseRepository.save(course);
    }

    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new RuntimeException("Course not found");
        }
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

        if (updates.containsKey("name")) {
            classroom.setName((String) updates.get("name"));
        }

        return classroomRepository.save(classroom);
    }

    public void deleteClassroom(Long id) {
        if (!classroomRepository.existsById(id)) {
            throw new RuntimeException("Classroom not found");
        }
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
        question.setCorrectAnswer(request.getCorrectAnswer());

        return questionRepository.save(question);
    }

    public Question patchQuestion(Long id, Map<String, Object> updates) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (updates.containsKey("questionText")) {
            question.setQuestionText((String) updates.get("questionText"));
        }
        if (updates.containsKey("options")) {
            question.setOptions((String) updates.get("options"));
        }
        if (updates.containsKey("correctAnswer")) {
            question.setCorrectAnswer((String) updates.get("correctAnswer"));
        }

        return questionRepository.save(question);
    }

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new RuntimeException("Question not found");
        }
        questionRepository.deleteById(id);
    }
}