package project.ktc.springboot_app.category.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.category.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {

    /**
     * Retrieves all categories with their course count.
     * Counts only published and non-deleted courses.
     * Categories with zero courses are still included in the result.
     */
    @Query("""
            SELECT c.id as id, c.name as name,
                   COALESCE(COUNT(CASE WHEN co.isPublished = true AND co.isDeleted = false THEN 1 END), 0) as courseCount
            FROM Category c
            LEFT JOIN c.courses co
            GROUP BY c.id, c.name
            ORDER BY c.name ASC
            """)
    List<CategoryProjection> findAllWithCourseCount();

    /**
     * Projection interface for category data with course count
     */
    interface CategoryProjection {
        String getId();

        String getName();

        Long getCourseCount();
    }
}
