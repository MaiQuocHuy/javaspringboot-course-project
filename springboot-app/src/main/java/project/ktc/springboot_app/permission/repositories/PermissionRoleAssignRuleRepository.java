package project.ktc.springboot_app.permission.repositories;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.permission.entity.PermissionRoleAssignRule;

/**
 * Repository interface for Permission Role Assign Rule entity Handles database operations for
 * permission-role assignment rules
 */
@Repository
public interface PermissionRoleAssignRuleRepository
    extends JpaRepository<PermissionRoleAssignRule, String> {

  /**
   * Find all assignable permission IDs for a specific role Returns only active rules where
   * permissions can be assigned to the role
   *
   * @param roleId the role ID to check
   * @return Set of permission IDs that can be assigned to this role
   */
  @Query(
      "SELECT DISTINCT p.permission.id FROM PermissionRoleAssignRule p "
          + "WHERE p.role.id = :roleId AND p.isActive = true")
  Set<String> findAssignablePermissionIdsByRoleId(@Param("roleId") String roleId);

  /**
   * Find all assignable permission keys for a specific role Returns only active rules where
   * permissions can be assigned to the role
   *
   * @param roleId the role ID to check
   * @return Set of permission keys that can be assigned to this role
   */
  @Query(
      "SELECT DISTINCT p.permission.permissionKey FROM PermissionRoleAssignRule p "
          + "WHERE p.role.id = :roleId AND p.isActive = true")
  Set<String> findAssignablePermissionKeysByRoleId(@Param("roleId") String roleId);

  /**
   * Check if a specific permission can be assigned to a role
   *
   * @param roleId the role ID
   * @param permissionId the permission ID
   * @return true if the permission can be assigned to the role
   */
  @Query(
      "SELECT COUNT(p) > 0 FROM PermissionRoleAssignRule p "
          + "WHERE p.role.id = :roleId AND p.permission.id = :permissionId AND p.isActive = true")
  boolean canAssignPermissionToRole(
      @Param("roleId") String roleId, @Param("permissionId") String permissionId);

  /**
   * Find all permission assignment rules for a specific role
   *
   * @param roleId the role ID
   * @return List of assignment rules for the role
   */
  @Query("SELECT p FROM PermissionRoleAssignRule p " + "WHERE p.role.id = :roleId")
  List<PermissionRoleAssignRule> findByRoleId(@Param("roleId") String roleId);

  /**
   * Find all active permission assignment rules for a specific role
   *
   * @param roleId the role ID
   * @return List of active assignment rules for the role
   */
  @Query(
      "SELECT p FROM PermissionRoleAssignRule p "
          + "WHERE p.role.id = :roleId AND p.isActive = true")
  List<PermissionRoleAssignRule> findActiveByRoleId(@Param("roleId") String roleId);

  /**
   * Find all roles that can be assigned a specific permission
   *
   * @param permissionId the permission ID
   * @return List of assignment rules for the permission
   */
  @Query(
      "SELECT p FROM PermissionRoleAssignRule p "
          + "WHERE p.permission.id = :permissionId AND p.isActive = true")
  List<PermissionRoleAssignRule> findRolesByPermissionId(
      @Param("permissionId") String permissionId);

  /**
   * Count total assignable permissions for a role
   *
   * @param roleId the role ID
   * @return count of permissions that can be assigned to the role
   */
  @Query(
      "SELECT COUNT(DISTINCT p.permission.id) FROM PermissionRoleAssignRule p "
          + "WHERE p.role.id = :roleId AND p.isActive = true")
  long countAssignablePermissionsByRoleId(@Param("roleId") String roleId);

  /**
   * Find assignment rule for specific role and permission
   *
   * @param roleId the role ID
   * @param permissionId the permission ID
   * @return the assignment rule if exists
   */
  @Query(
      "SELECT p FROM PermissionRoleAssignRule p "
          + "WHERE p.role.id = :roleId AND p.permission.id = :permissionId")
  PermissionRoleAssignRule findByRoleIdAndPermissionId(
      @Param("roleId") String roleId, @Param("permissionId") String permissionId);

  /**
   * Find all active role names that can assign a specific permission
   *
   * @param permissionId the permission ID
   * @return List of role names that can assign this permission
   */
  @Query(
      "SELECT p.role.role FROM PermissionRoleAssignRule p "
          + "WHERE p.permission.id = :permissionId AND p.isActive = true")
  List<String> findAllowedRoleNamesByPermissionId(@Param("permissionId") String permissionId);

  /**
   * Check if a permission has any assignment rules (restricted)
   *
   * @param permissionId the permission ID
   * @return true if the permission has assignment rules
   */
  @Query(
      "SELECT COUNT(p) > 0 FROM PermissionRoleAssignRule p "
          + "WHERE p.permission.id = :permissionId")
  boolean hasAssignmentRules(@Param("permissionId") String permissionId);
}
