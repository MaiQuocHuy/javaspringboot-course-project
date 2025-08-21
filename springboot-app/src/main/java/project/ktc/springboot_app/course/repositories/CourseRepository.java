package project.ktc.springboot_app.course.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.review.entity.Review;
import project.ktc.springboot_app.section.entity.Section;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String>, JpaSpecificationExecutor<Course> {

        @Query("SELECT DISTINCT c FROM Course c " +
                        "LEFT JOIN FETCH c.instructor i " +
                        "LEFT JOIN c.categories cat " +
                        "WHERE c.isPublished = true AND c.isApproved = true AND c.isDeleted = false " +
                        "AND (:search IS NULL OR " +
                        "     LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
                        "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
                        "AND (:level IS NULL OR c.level = :level)")
        Page<Course> findPublishedCoursesWithFilters(
                        @Param("search") String search,
                        @Param("categoryId") String categoryId,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("level") CourseLevel level,
                        Pageable pageable);

        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.instructor i " +
                        "WHERE c.id = :courseId AND c.isPublished = true AND c.isDeleted = false")
        Optional<Course> findPublishedCourseByIdWithDetails(@Param("courseId") String courseId);

        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.instructor i " +
                        "WHERE c.slug = :slug AND c.isPublished = true AND c.isApproved = true AND c.isDeleted = false")
        Optional<Course> findPublishedCourseBySlugWithDetails(@Param("slug") String slug);

        @Query("SELECT c FROM Course c " +
                        "LEFT JOIN FETCH c.categories cat " +
                        "WHERE c.id = :courseId")
        Optional<Course> findCourseWithCategories(@Param("courseId") String courseId);

        @Query("SELECT s FROM Section s " +
                        "LEFT JOIN FETCH s.lessons l " +
                        "LEFT JOIN FETCH l.content vc " +
                        "WHERE s.course.id = :courseId " +
                        "ORDER BY s.orderIndex ASC, l.orderIndex ASC")
        List<Section> findSectionsWithLessonsByCourseId(@Param("courseId") String courseId);

        @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
        Optional<Double> findAverageRatingByCourseId(@Param("courseId") String courseId);

        @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId")
        Long countReviewsByCourseId(@Param("courseId") String courseId);

        @Query("SELECT COUNT(l) FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId")
        Long countLessonsByCourseId(@Param("courseId") String courseId);

        @Query("SELECT COUNT(l) FROM Lesson l " +
                        "JOIN l.section s " +
                        "JOIN l.lessonType lt " +
                        "WHERE s.course.id = :courseId AND lt.name = 'QUIZ'")
        Long countQuizLessonsByCourseId(@Param("courseId") String courseId);

        @Query("SELECT COUNT(qq) FROM QuizQuestion qq " +
                        "JOIN qq.lesson l " +
                        "JOIN l.section s " +
                        "WHERE s.course.id = :courseId")
        Long countQuizQuestionsByCourseId(@Param("courseId") String courseId);

        @Query("SELECT COUNT(qq) FROM QuizQuestion qq " +
                        "JOIN qq.lesson l " +
                        "WHERE l.section.id = :sectionId")
        Long countQuizQuestionsBySectionId(@Param("sectionId") String sectionId);

        @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
                        "FROM Enrollment e WHERE e.course.id = :courseId AND e.user.id = :userId")
        Boolean isUserEnrolledInCourse(@Param("courseId") String courseId, @Param("userId") String userId);

        @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
        Long countUserEnrolledInCourse(@Param("courseId") String courseId);

        @Query("SELECT c.id, COUNT(e) FROM Course c " +
                        "LEFT JOIN c.enrollments e " +
                        "WHERE c.id IN :courseIds " +
                        "GROUP BY c.id")
        List<Object[]> findEnrollmentCountsByCourseIds(@Param("courseIds") List<String> courseIds);

        // Admin specific queries
        @Query("SELECT DISTINCT c FROM Course c " +
                        "LEFT JOIN FETCH c.instructor i " +
                        "LEFT JOIN c.categories cat " +
                        "WHERE c.isDeleted = false " +
                        "AND (:isApproved IS NULL OR c.isApproved = :isApproved) " +
                        "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
                        "AND (:search IS NULL OR " +
                        "     LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
                        "AND (:level IS NULL OR c.level = :level)")
        Page<Course> findCoursesForAdmin(
                        @Param("isApproved") Boolean isApproved,
                        @Param("categoryId") String categoryId,
                        @Param("search") String search,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("level") CourseLevel level,
                        Pageable pageable);

        @Query("SELECT r FROM Review r " +
                        "LEFT JOIN FETCH r.user u " +
                        "WHERE r.course.id = :courseId " +
                        "ORDER BY r.reviewedAt DESC")
        List<Review> findReviewsByCourseId(@Param("courseId") String courseId);

        // Debug query to check actual course count
        @Query("SELECT COUNT(DISTINCT c.id) FROM Course c " +
                        "LEFT JOIN c.categories cat " +
                        "WHERE c.isPublished = true AND c.isApproved = true AND c.isDeleted = false " +
                        "AND (:search IS NULL OR " +
                        "     LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "     LOWER(c.instructor.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                        "AND (:categoryId IS NULL OR cat.id = :categoryId) " +
                        "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
                        "AND (:maxPrice IS NULL OR c.price <= :maxPrice) " +
                        "AND (:level IS NULL OR c.level = :level)")
        Long countPublishedCoursesWithFilters(
                        @Param("search") String search,
                        @Param("categoryId") String categoryId,
                        @Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice,
                        @Param("level") CourseLevel level);

        boolean existsBySlug(String courseSlug);

        /**
         * Get average rating for instructor's published and approved courses only
         * This ensures consistency with course count query by only including
         * courses that are currently available to students
         */
        @Query("SELECT AVG(r.rating) FROM Review r " +
                        "WHERE r.course.instructor.id = :instructorId " +
                        "AND r.course.isPublished = true AND r.course.isApproved = true AND r.course.isDeleted = false")
        Optional<Double> findAverageRatingByInstructorId(@Param("instructorId") String instructorId);

        /**
         * Get total published and approved course count by instructor
         * Only counts courses that are currently published, approved and not deleted
         */
        @Query("SELECT COUNT(c) FROM Course c " +
                        "WHERE c.instructor.id = :instructorId " +
                        "AND c.isPublished = true AND c.isApproved = true AND c.isDeleted = false")
        Long countCoursesByInstructorId(@Param("instructorId") String instructorId);

        /**
         * Check if a slug exists (case-insensitive)
         * Used to prevent duplicate slugs with normalization
         */
        @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c " +
                        "WHERE LOWER(c.slug) = LOWER(:slug)")
        boolean existsBySlugIgnoreCase(@Param("slug") String slug);

        boolean existsByIdAndInstructorId(String courseId, String instructorId);

}
