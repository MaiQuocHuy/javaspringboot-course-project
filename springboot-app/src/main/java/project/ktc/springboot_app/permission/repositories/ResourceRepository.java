package project.ktc.springboot_app.permission.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.permission.entity.Resource;

/** Repository interface for Resource entity */
@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {

  /** Find all active resources */
  @Query("SELECT r FROM Resource r WHERE r.isActive = true ORDER BY r.name")
  List<Resource> findAllActive();

  /** Find all root resources (resources without parent) */
  @Query(
      "SELECT r FROM Resource r WHERE r.parentResource IS NULL AND r.isActive = true ORDER BY r.name")
  List<Resource> findAllRootResources();

  /** Find all child resources of a parent resource */
  @Query(
      "SELECT r FROM Resource r WHERE r.parentResource = :parent AND r.isActive = true ORDER BY r.name")
  List<Resource> findByParentResource(@Param("parent") Resource parent);

  /** Find resource by name (case insensitive) */
  @Query("SELECT r FROM Resource r WHERE LOWER(r.name) = LOWER(:name) AND r.isActive = true")
  Optional<Resource> findByNameIgnoreCase(@Param("name") String name);

  /** Find resource hierarchy (parent and all children) for a specific resource */
  @Query(
      "SELECT r FROM Resource r WHERE r.parentResource = :parentResource OR r = :parentResource AND r.isActive = true ORDER BY r.name")
  List<Resource> findResourceHierarchy(@Param("parentResource") Resource parentResource);

  /** Check if a resource has any children */
  @Query(
      "SELECT COUNT(r) > 0 FROM Resource r WHERE r.parentResource = :parent AND r.isActive = true")
  boolean hasChildren(@Param("parent") Resource parent);

  /** Find all resources with their parent-child relationships for building trees */
  @Query(
      "SELECT r FROM Resource r LEFT JOIN FETCH r.parentResource WHERE r.isActive = true ORDER BY r.name")
  List<Resource> findAllWithParents();

  /** Count resources by parent */
  @Query("SELECT COUNT(r) FROM Resource r WHERE r.parentResource = :parent AND r.isActive = true")
  long countByParentResource(@Param("parent") Resource parent);
}
