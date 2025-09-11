package project.ktc.springboot_app.course.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.entity.CourseReviewStatus.ReviewStatus;
import project.ktc.springboot_app.course.enums.CourseLevel;

@Repository
public interface InstructorCourseRepository extends JpaRepository<Course, String> {

        // @Query("SELECT DISTINCT c FROM Course c " +
        // "LEFT JOIN FETCH c.categories cat " +
        // "WHERE c.instructor.id = :instructorId AND c.isDeleted = false " +
        // "AND (:search IS NULL OR " +
        // " LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        // " LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
        // "AND (:status IS NULL OR " +
        // " (:status = 'PUBLISHED' AND c.isPublished = true) OR " +
        // " (:status = 'UNPUBLISHED' AND c.isPublished = false))")
        // Page<Course> findByInstructorIdWithFilters(
        // @Param("instructorId") String instructorId,
        // @Param("search") String search,
        // @Param("status") String status,
        // Pageable pageable);

        /**
         * Find instructor courses with filters
         * 
         * @param instructorId The instructor ID
         * @param search       Search term for title or description
         * @param reviewStatus Filter by course review status (from
         *                     CourseReviewStatusHistory's action field)
         * @param categoryIds  List of category IDs to filter by
         * @param minPrice     Minimum price filter
         * @param maxPrice     Maximum price filter
         * @param rating       Minimum average rating filter
         * @param pageable     Pagination information
         * @return Page of courses matching the criteria
         */
        @Query("SELECT DISTINCT c FROM Course c " +
                        "LEFT JOIN FETCH c.categories cat " +
                        "LEFT JOIN CourseReviewStatus crvs ON crvs.course.id = c.id " +
                        "LEFT JOIN Review r ON r.course.id = c.id " +
                        "WHERE c.instructor.id = :instructorId " +
                        "AND c.isDeleted = false " +
                        "AND (:search IS NULL  OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))" +
                        " OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:reviewStatus IS NULL OR crvs.status = :reviewStatus) " +
                        "AND (:isPublished IS NULL OR c.isPublished = :isPublished) " +
                        "AND (:categoryIds IS NULL OR cat.id IN :categoryIds) " +
                        "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
                        "AND (:rating IS NULL OR (SELECT AVG(r.rating) FROM Review r WHERE r.course.id = c.id) >= :rating)"
                        +
                        "AND (:level IS NULL OR c.level = :level)")
        Page<Course> findByInstructorIdWithFilters(
                        @Param("instructorId") String instructorId,
                        @Param("search") String search,
                        @Param("reviewStatus") ReviewStatus reviewStatus,
                        @Param("categoryIds") List<String> categoryIds,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        @Param("rating") Integer rating,
                        @Param("level") CourseLevel level,
                        @Param("isPublished") Boolean isPublished,
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

        /**
         * Find all published courses for an instructor
         */
        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.categories " +
                        "WHERE c.instructor.id = :instructorId AND c.isPublished = :isPublished AND c.isDeleted = false "
                        +
                        "ORDER BY c.createdAt DESC")
        List<Course> findByInstructorIdAndIsPublished(@Param("instructorId") String instructorId,
                        @Param("isPublished") Boolean isPublished);
}
