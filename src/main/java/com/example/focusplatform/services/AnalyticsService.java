package com.example.focusplatform.services;

import com.example.focusplatform.dto.LeaderboardEntryResponse;
import com.example.focusplatform.dto.StudentAnalyticsResponse;
import com.example.focusplatform.dto.TeacherCourseStatsResponse;
import com.example.focusplatform.entities.Course;
import com.example.focusplatform.entities.User;
import com.example.focusplatform.exception.AccessDeniedException;
import com.example.focusplatform.exception.ResourceNotFoundException;
import com.example.focusplatform.repository.projection.CourseBreakdownProjection;
import com.example.focusplatform.repository.projection.StudentCourseAggregateProjection;
import com.example.focusplatform.repository.projection.StudentOverallAggregateProjection;
import com.example.focusplatform.repositories.ClassroomRepository;
import com.example.focusplatform.repositories.CourseRepository;
import com.example.focusplatform.repositories.UserQuestionAttemptRepository;
import com.example.focusplatform.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ClassroomRepository classroomRepository;
    private final UserQuestionAttemptRepository attemptRepository;

    public AnalyticsService(UserRepository userRepository,
                            CourseRepository courseRepository,
                            ClassroomRepository classroomRepository,
                            UserQuestionAttemptRepository attemptRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.classroomRepository = classroomRepository;
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public StudentAnalyticsResponse getStudentAnalytics(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        StudentOverallAggregateProjection overall = attemptRepository
                .aggregateOverallByStudent(student.getId())
                .orElse(null);

        long attempted = overall != null ? nullSafe(overall.getQuestionsAttempted()) : 0;
        long correct = overall != null ? nullSafe(overall.getCorrectCount()) : 0;
        long wrong = overall != null ? nullSafe(overall.getWrongCount()) : 0;
        long totalTime = overall != null ? nullSafe(overall.getTotalTimeMs()) : 0;

        List<StudentAnalyticsResponse.CourseAnalyticsItem> byCourse = new ArrayList<>();
        for (CourseBreakdownProjection row : attemptRepository.aggregateByStudentGroupedByCourse(student.getId())) {
            byCourse.add(StudentAnalyticsResponse.CourseAnalyticsItem.builder()
                    .courseId(row.getCourseId())
                    .courseTitle(row.getCourseTitle())
                    .questionsAttempted(nullSafe(row.getQuestionsAttempted()))
                    .correctCount(nullSafe(row.getCorrectCount()))
                    .wrongCount(nullSafe(row.getWrongCount()))
                    .problemsSolved(nullSafe(row.getProblemsSolved()))
                    .totalTimeMs(nullSafe(row.getTotalTimeMs()))
                    .build());
        }

        log.info("Loaded student analytics for userId={} courses={}", student.getId(), byCourse.size());
        return StudentAnalyticsResponse.builder()
                .totalQuestionsAttempted(attempted)
                .totalCorrect(correct)
                .totalWrong(wrong)
                .totalTimeMs(totalTime)
                .byCourse(byCourse)
                .build();
    }

    @Transactional(readOnly = true)
    public TeacherCourseStatsResponse getTeacherCourseStats(String teacherEmail, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        assertTeacherOwnsCourse(teacherEmail, course);

        List<User> enrolled = loadEnrolledStudents(course);
        Map<Long, StudentCourseAggregateProjection> aggregates = attemptRepository.aggregateByCourse(courseId)
                .stream()
                .collect(Collectors.toMap(StudentCourseAggregateProjection::getStudentId, row -> row));

        List<TeacherCourseStatsResponse.StudentCourseStatEntry> students = buildEntriesForEnrolled(enrolled, aggregates);
        students = students.stream().sorted(leaderboardComparator()).toList();

        int rank = 1;
        for (TeacherCourseStatsResponse.StudentCourseStatEntry entry : students) {
            entry.setRank(rank++);
        }

        log.info("Teacher {} loaded stats for courseId={} students={}", teacherEmail, courseId, students.size());
        return TeacherCourseStatsResponse.builder()
                .courseId(course.getId())
                .courseTitle(course.getTitle())
                .totalEnrolledStudents(students.size())
                .students(students)
                .build();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getCourseLeaderboard(Long courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }

        List<StudentCourseAggregateProjection> rows = attemptRepository.aggregateByCourse(courseId);
        List<TeacherCourseStatsResponse.StudentCourseStatEntry> sorted = buildSortedStudentEntries(rows);

        List<LeaderboardEntryResponse> leaderboard = new ArrayList<>();
        int rank = 1;
        for (TeacherCourseStatsResponse.StudentCourseStatEntry row : sorted) {
            leaderboard.add(LeaderboardEntryResponse.builder()
                    .rank(rank++)
                    .studentId(row.getStudentId())
                    .studentName(row.getStudentName())
                    .questionsAttempted(row.getQuestionsAttempted())
                    .correctCount(row.getCorrectCount())
                    .totalTimeMs(row.getTotalTimeMs())
                    .build());
        }

        log.info("Built leaderboard for courseId={} entries={}", courseId, leaderboard.size());
        return leaderboard;
    }

    private List<TeacherCourseStatsResponse.StudentCourseStatEntry> buildSortedStudentEntries(
            List<StudentCourseAggregateProjection> rows) {
        return rows.stream()
                .map(this::fromAggregate)
                .sorted(leaderboardComparator())
                .toList();
    }

    private List<TeacherCourseStatsResponse.StudentCourseStatEntry> buildEntriesForEnrolled(
            List<User> enrolled,
            Map<Long, StudentCourseAggregateProjection> aggregates) {
        List<TeacherCourseStatsResponse.StudentCourseStatEntry> entries = new ArrayList<>();
        for (User student : enrolled) {
            StudentCourseAggregateProjection row = aggregates.get(student.getId());
            if (row != null) {
                entries.add(fromAggregate(row));
            } else {
                entries.add(TeacherCourseStatsResponse.StudentCourseStatEntry.builder()
                        .studentId(student.getId())
                        .studentName(student.getName())
                        .studentEmail(student.getEmail())
                        .questionsAttempted(0)
                        .correctCount(0)
                        .wrongCount(0)
                        .totalTimeMs(0)
                        .build());
            }
        }
        return entries;
    }

    private TeacherCourseStatsResponse.StudentCourseStatEntry fromAggregate(StudentCourseAggregateProjection row) {
        return TeacherCourseStatsResponse.StudentCourseStatEntry.builder()
                .studentId(row.getStudentId())
                .studentName(row.getStudentName())
                .studentEmail(row.getStudentEmail())
                .questionsAttempted(nullSafe(row.getQuestionsAttempted()))
                .correctCount(nullSafe(row.getCorrectCount()))
                .wrongCount(nullSafe(row.getWrongCount()))
                .totalTimeMs(nullSafe(row.getTotalTimeMs()))
                .build();
    }

    private List<User> loadEnrolledStudents(Course course) {
        Long classroomId = course.getClassroom().getId();
        return classroomRepository.findById(classroomId)
                .map(c -> c.getStudents() == null ? List.<User>of() : c.getStudents())
                .orElse(List.of());
    }

    /**
     * Rank: more questions attempted first, then faster total time, then more correct answers.
     */
    private Comparator<TeacherCourseStatsResponse.StudentCourseStatEntry> leaderboardComparator() {
        return Comparator
                .comparingLong(TeacherCourseStatsResponse.StudentCourseStatEntry::getQuestionsAttempted).reversed()
                .thenComparingLong(TeacherCourseStatsResponse.StudentCourseStatEntry::getTotalTimeMs)
                .thenComparingLong(TeacherCourseStatsResponse.StudentCourseStatEntry::getCorrectCount).reversed();
    }

    private void assertTeacherOwnsCourse(String teacherEmail, Course course) {
        if (course.getClassroom() == null
                || course.getClassroom().getTeacher() == null
                || !teacherEmail.equals(course.getClassroom().getTeacher().getEmail())) {
            throw new AccessDeniedException("You do not teach this course");
        }
    }

    private static long nullSafe(Long value) {
        return value == null ? 0L : value;
    }
}
