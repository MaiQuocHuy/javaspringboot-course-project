package project.ktc.springboot_app.permission.interfaces;

import java.util.List;
import org.springframework.data.domain.Page;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.CreateRoleRequest;
import project.ktc.springboot_app.permission.dto.RoleWithPermissionsDto;

/** Service interface for role management operations */
public interface RoleService {

	/**
	 * Create a new role
	 *
	 * @param request
	 *            the role creation request
	 * @return the created role entity
	 * @throws IllegalArgumentException
	 *             if role name already exists
	 */
	UserRole createRole(CreateRoleRequest request);

	/**
	 * Find role by ID
	 *
	 * @param roleId
	 *            the role ID
	 * @return the role entity
	 * @throws RuntimeException
	 *             if role not found
	 */
	UserRole findById(String roleId);

	/**
	 * Find role by role type
	 *
	 * @param roleType
	 *            the role type
	 * @return the role entity or null if not found
	 */
	UserRole findByRoleType(String roleType);

	/**
	 * Check if role name already exists
	 *
	 * @param roleName
	 *            the role name to check
	 * @return true if role exists, false otherwise
	 */
	boolean existsByRoleName(String roleName);

	/**
	 * Get all roles
	 *
	 * @return list of all roles
	 */
	List<UserRole> getAllRoles();

	/**
	 * Get all roles with their associated permissions (paginated)
	 *
	 * @param page
	 *            page number (0-based)
	 * @param size
	 *            page size (max 100)
	 * @return paginated list of roles with permissions
	 */
	Page<RoleWithPermissionsDto> getAllRolesWithPermissions(int page, int size);

	/**
	 * Update a role
	 *
	 * @param roleId
	 *            the role ID
	 * @param request
	 *            the update request
	 * @return updated role entity
	 */
	UserRole updateRole(
			String roleId, project.ktc.springboot_app.permission.dto.UpdateRoleRequest request);

	/**
	 * Delete a role
	 *
	 * @param roleId
	 *            the role ID
	 */
	void deleteRole(String roleId);

	/**
	 * Assign permissions to a role
	 *
	 * @param roleId
	 *            the role ID
	 * @param request
	 *            the assign permissions request
	 */
	void assignPermissions(
			String roleId, project.ktc.springboot_app.permission.dto.AssignPermissionsRequest request);

	/**
	 * Remove permissions from a role
	 *
	 * @param roleId
	 *            the role ID
	 * @param request
	 *            the remove permissions request
	 */
	void removePermissions(
			String roleId, project.ktc.springboot_app.permission.dto.RemovePermissionsRequest request);

	/**
	 * Get permissions assigned to a specific role
	 *
	 * @param roleId
	 *            the role ID
	 * @return list of role permission details
	 */
	List<project.ktc.springboot_app.permission.dto.RolePermissionDetailDto> getRolePermissions(
			String roleId);

	/**
	 * Convert UserRole entity to RoleWithPermissionsDto
	 *
	 * @param role
	 *            the role entity
	 * @return role with permissions DTO
	 */
	RoleWithPermissionsDto convertToRoleWithPermissionsDto(UserRole role);
}
