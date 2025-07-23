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

import java.math.BigDecimal;

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
}
