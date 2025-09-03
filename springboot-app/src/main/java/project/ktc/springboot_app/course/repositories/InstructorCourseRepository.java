package project.ktc.springboot_app.course.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.course.entity.Course;

@Repository
public interface InstructorCourseRepository extends JpaRepository<Course, String> {

        @Query("SELECT DISTINCT c FROM Course c " +
                        "LEFT JOIN FETCH c.categories cat " +
                        "WHERE c.instructor.id = :instructorId AND c.isDeleted = false " +
                        "AND (:search IS NULL OR " +
                        "     LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:status IS NULL OR " +
                        "     (:status = 'PUBLISHED' AND c.isPublished = true) OR " +
                        "     (:status = 'UNPUBLISHED' AND c.isPublished = false))")
        Page<Course> findByInstructorIdWithFilters(
                        @Param("instructorId") String instructorId,
                        @Param("search") String search,
                        @Param("status") String status,
                        Pageable pageable);

        // Get total instructor's courses
        @Query("SELECT COUNT(c) FROM Course c " +
                        "WHERE c.instructor.id = :instructorId AND c.isDeleted = false")
        Long countTotalCoursesByInstructorId(String instructorId);

        // Get total active courses
        @Query("SELECT COUNT(c) FROM Course c WHERE c.instructor.id = :instructorId AND c.isDeleted = false AND c.isApproved = true")
        Long countTotalActiveCoursesByInstructorId(String instructorId);

        /**
         * Get enrollment count for a course
         */
        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
        Long countEnrollmentsByCourseId(@Param("courseId") String courseId);

        /**
         * Get total revenue for a course
         */
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.course.id = :courseId AND p.status = 'COMPLETED'")
        BigDecimal getTotalRevenueByCourseId(@Param("courseId") String courseId);

        /**
         * Get last content update timestamp for a course
         */
        @Query("SELECT MAX(GREATEST(COALESCE(s.updatedAt, s.createdAt), " +
                        "               COALESCE(l.updatedAt, l.createdAt))) " +
                        "FROM Section s LEFT JOIN s.lessons l " +
                        "WHERE s.course.id = :courseId")
        Optional<LocalDateTime> getLastContentUpdateByCourseId(@Param("courseId") String courseId);

        /**
         * Find course by ID and instructor ID with instructor details
         */
        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.instructor " +
                        "WHERE c.id = :courseId AND c.instructor.id = :instructorId AND c.isDeleted = false")
        Optional<Course> findByIdAndInstructorId(@Param("courseId") String courseId,
                        @Param("instructorId") String instructorId);

}
