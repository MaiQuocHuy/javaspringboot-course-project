package project.ktc.springboot_app.category.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
  // Find by ID
  @SuppressWarnings("null")
  Optional<Category> findById(String id);

  // Find by name (case insensitive)
  Optional<Category> findByNameIgnoreCase(String name);

  // Check if name exists (excluding current id for updates)
  @Query(
      "SELECT COUNT(c) > 0 FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.id != :excludeId")
  boolean existsByNameIgnoreCaseAndIdNot(
      @Param("name") String name, @Param("excludeId") String excludeId);

  // Check if name exists
  boolean existsByNameIgnoreCase(String name);

  // Find categories with search
  @Query(
      "SELECT c FROM Category c WHERE "
          + "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR "
          + "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<Category> findCategoriesWithSearch(@Param("search") String search, Pageable pageable);

  /**
   * Retrieves all categories with their course count. Counts only published, approved and
   * non-deleted courses. Categories with zero courses are still included in the result.
   */
  @Query(
      value =
          """
            SELECT c.id as id, c.name as name, c.description as description,
                   COALESCE(COUNT(CASE WHEN co.is_published = 1 AND co.is_approved = 1 AND (co.is_deleted = 0 OR co.is_deleted IS NULL) THEN 1 END), 0) as courseCount
            FROM categories c
            LEFT JOIN course_categories cc ON c.id = cc.category_id
            LEFT JOIN courses co ON cc.course_id = co.id
            GROUP BY c.id, c.name, c.description
            ORDER BY c.name ASC
            """,
      nativeQuery = true)
  List<CategoryProjection> findAllWithCourseCount();

  /** Projection interface for category data with course count */
  interface CategoryProjection {
    String getId();

    String getName();

    String getDescription();

    Long getCourseCount();
  }
}
