package com.example.focusplatform.services;

// 1. All the new imports added here!
import com.example.focusplatform.dto.ClassroomCreateRequest;
import com.example.focusplatform.dto.ContentCreateRequest;
import com.example.focusplatform.dto.CourseCreateRequest;
import com.example.focusplatform.dto.QuestionCreateRequest;
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

import java.util.UUID;

@Service
public class TeacherService {

    private final ClassroomRepository classroomRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    // 2. We actually declare the repositories here
    private final CourseContentRepository contentRepository;
    private final QuestionRepository questionRepository;

    // 3. We inject them into the constructor here
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
}