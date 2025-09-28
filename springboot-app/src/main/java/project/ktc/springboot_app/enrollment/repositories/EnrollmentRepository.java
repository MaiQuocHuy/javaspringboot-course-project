package project.ktc.springboot_app.enrollment.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.enrollment.entity.Enrollment;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

        @Query("SELECT e FROM Enrollment e WHERE e.user.id = :userId AND e.course.id = :courseId")
        Optional<Enrollment> findByUserIdAndCourseId(@Param("userId") String userId,
                        @Param("courseId") String courseId);

        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
        Long countByCourseId(@Param("courseId") String courseId);

        boolean existsByUserIdAndCourseId(String userId, String courseId);

        @Query("SELECT e FROM Enrollment e " +
                        "JOIN FETCH e.course c " +
                        "JOIN FETCH c.instructor i " +
                        "WHERE e.user.id = :userId " +
                        "AND (:status IS NULL OR e.completionStatus = :status)")
        Page<Enrollment> findByUserIdWithCourseAndInstructor(
                        @Param("userId") String userId,
                        @Param("status") Enrollment.CompletionStatus status,
                        Pageable pageable);

        @Query("SELECT e FROM Enrollment e " +
                        "JOIN FETCH e.course c " +
                        "JOIN FETCH c.instructor i " +
                        "WHERE e.user.id = :userId " +
                        "AND (:status IS NULL OR e.completionStatus = :status)")
        List<Enrollment> findByUserIdWithCourseAndInstructor(
                        @Param("userId") String userId,
                        @Param("status") Enrollment.CompletionStatus status);

        @Query("SELECT COUNT(lc) FROM LessonCompletion lc " +
                        "JOIN lc.lesson l " +
                        "WHERE lc.user.id = :userId AND l.section.course.id = :courseId")
        Long countCompletedLessonsByUserAndCourse(@Param("userId") String userId, @Param("courseId") String courseId);

        @Query("SELECT COUNT(l) FROM Lesson l " +
                        "WHERE l.section.course.id = :courseId")
        Long countTotalLessonsByCourse(@Param("courseId") String courseId);

        boolean existsByIdAndUserId(String enrollmentId, String userId);

        @Query("SELECT e FROM Enrollment e " +
                        "JOIN FETCH e.course c " +
                        "JOIN FETCH c.instructor i " +
                        "WHERE e.user.id = :userId " +
                        "ORDER BY e.enrolledAt DESC")
        List<Enrollment> findTop3RecentEnrollmentsByUserId(@Param("userId") String userId);

        // Dashboard Statistics Methods
        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :userId")
        Long countTotalEnrollmentsByUserId(@Param("userId") String userId);

        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.user.id = :userId AND e.completionStatus = :status")
        Long countEnrollmentsByUserIdAndStatus(@Param("userId") String userId,
                        @Param("status") Enrollment.CompletionStatus status);

        @Query("SELECT COUNT(lc) FROM LessonCompletion lc " +
                        "JOIN lc.lesson l " +
                        "JOIN l.section s " +
                        "JOIN s.course c " +
                        "JOIN c.enrollments e " +
                        "WHERE lc.user.id = :userId AND e.user.id = :userId")
        Long countTotalCompletedLessonsByUserId(@Param("userId") String userId);

        @Query("SELECT COUNT(l) FROM Lesson l " +
                        "JOIN l.section s " +
                        "JOIN s.course c " +
                        "JOIN c.enrollments e " +
                        "WHERE e.user.id = :userId")
        Long countTotalLessonsInEnrolledCoursesByUserId(@Param("userId") String userId);

        // Recent Activities Methods
        @Query("SELECT e FROM Enrollment e " +
                        "JOIN FETCH e.course c " +
                        "JOIN FETCH c.instructor i " +
                        "WHERE e.user.id = :userId " +
                        "ORDER BY e.enrolledAt DESC")
        List<Enrollment> findRecentEnrollmentsByUserId(@Param("userId") String userId,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT e FROM Enrollment e " +
                        "JOIN FETCH e.course c " +
                        "JOIN FETCH c.instructor i " +
                        "WHERE e.user.id = :userId " +
                        "AND (:status IS NULL OR e.completionStatus = :status) " +
                        "AND (:search IS NULL OR :search = '' OR " +
                        "     LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<Enrollment> findByUserIdWithSearchAndFilter(
                        @Param("userId") String userId,
                        @Param("search") String search,
                        @Param("status") Enrollment.CompletionStatus status,
                        Pageable pageable);
}
