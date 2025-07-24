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
import project.ktc.springboot_app.entity.Section;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, String>, JpaSpecificationExecutor<Course> {

    @Query("SELECT c FROM Course c " +
            "LEFT JOIN FETCH c.instructor i " +
            "LEFT JOIN FETCH c.categories cat " +
            "WHERE c.isPublished = true AND c.isDeleted = false " +
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
            "LEFT JOIN FETCH c.categories cat " +
            "WHERE c.id = :courseId")
    Optional<Course> findCourseWithCategories(@Param("courseId") String courseId);

    @Query("SELECT s FROM Section s " +
            "LEFT JOIN FETCH s.lessons l " +
            "WHERE s.course.id = :courseId " +
            "ORDER BY s.orderIndex ASC, l.orderIndex ASC")
    List<Section> findSectionsWithLessonsByCourseId(@Param("courseId") String courseId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.course.id = :courseId")
    Optional<Double> findAverageRatingByCourseId(@Param("courseId") String courseId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.course.id = :courseId")
    Long countReviewsByCourseId(@Param("courseId") String courseId);

    @Query("SELECT COUNT(l) FROM Lesson l JOIN l.section s WHERE s.course.id = :courseId")
    Long countLessonsByCourseId(@Param("courseId") String courseId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " +
            "FROM Enrollment e WHERE e.course.id = :courseId AND e.user.id = :userId")
    Boolean isUserEnrolledInCourse(@Param("courseId") String courseId, @Param("userId") String userId);
}
