package project.ktc.springboot_app.permission.interfaces;

import java.util.List;
import java.util.Set;
import project.ktc.springboot_app.permission.entity.PermissionRoleAssignRule;

/**
 * Service interface for managing Permission Role Assignment Rules Handles business logic for
 * permission-role assignment constraints
 */
public interface PermissionRoleAssignRuleService {

  /**
   * Check if a permission can be assigned to a role
   *
   * @param roleId the role ID
   * @param permissionId the permission ID
   * @return true if permission can be assigned to role
   */
  boolean canAssignPermissionToRole(String roleId, String permissionId);

  /**
   * Get all assignable permission IDs for a role
   *
   * @param roleId the role ID
   * @return set of assignable permission IDs
   */
  Set<String> getAssignablePermissionIds(String roleId);

  /**
   * Get all assignable permission keys for a role
   *
   * @param roleId the role ID
   * @return set of assignable permission keys
   */
  Set<String> getAssignablePermissionKeys(String roleId);

  /**
   * Get all assignment rules for a role
   *
   * @param roleId the role ID
   * @return list of assignment rules
   */
  List<PermissionRoleAssignRule> getAssignmentRulesByRole(String roleId);

  /**
   * Get all active assignment rules for a role
   *
   * @param roleId the role ID
   * @return list of active assignment rules
   */
  List<PermissionRoleAssignRule> getActiveAssignmentRulesByRole(String roleId);

  /**
   * Get all roles that can be assigned a specific permission
   *
   * @param permissionId the permission ID
   * @return list of assignment rules for the permission
   */
  List<PermissionRoleAssignRule> getRolesByPermission(String permissionId);

  /**
   * Count total assignable permissions for a role
   *
   * @param roleId the role ID
   * @return count of assignable permissions
   */
  long countAssignablePermissions(String roleId);

  /**
   * Create a new assignment rule
   *
   * @param roleId the role ID
   * @param permissionId the permission ID
   * @param isActive whether the rule is active
   * @return the created assignment rule
   */
  PermissionRoleAssignRule createAssignmentRule(
      String roleId, String permissionId, boolean isActive);

  /**
   * Update an existing assignment rule
   *
   * @param ruleId the rule ID
   * @param isActive the new active status
   * @return the updated assignment rule
   */
  PermissionRoleAssignRule updateAssignmentRule(String ruleId, boolean isActive);

  /**
   * Delete an assignment rule
   *
   * @param ruleId the rule ID
   */
  void deleteAssignmentRule(String ruleId);

  /**
   * Validate permission assignment before creating role-permission relationship Throws exception if
   * assignment is not allowed
   *
   * @param roleId the role ID
   * @param permissionId the permission ID
   * @throws IllegalArgumentException if assignment is not allowed
   */
  void validatePermissionAssignment(String roleId, String permissionId);
}
