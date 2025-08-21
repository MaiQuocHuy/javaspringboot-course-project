package project.ktc.springboot_app.permission.interfaces;

import java.util.List;
import java.util.Set;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.entity.Permission;
import project.ktc.springboot_app.permission.entity.RolePermission;

/**
 * Service interface for managing permissions and authorization
 */
public interface PermissionService {

    /**
     * Load all permissions for a user based on their roles
     * 
     * @param user the user
     * @return set of permission keys
     */
    Set<String> loadUserPermissions(User user);

    /**
     * Load permissions for a specific user by ID and role
     * 
     * @param userId   the user ID
     * @param roleType the user's role type
     * @return set of permission keys
     */
    Set<String> loadUserPermissions(String userId, String roleType);

    /**
     * Check if a user has a specific permission
     * 
     * @param user          the user
     * @param permissionKey the permission key (e.g., "course:create")
     * @return true if user has permission
     */
    boolean hasPermission(User user, String permissionKey);

    /**
     * Check if a user has permission by user ID
     * 
     * @param userId        the user ID
     * @param permissionKey the permission key
     * @return true if user has permission
     */
    boolean hasPermission(String userId, String permissionKey);

    /**
     * Check if a user has permission for a specific resource and action
     * 
     * @param user     the user
     * @param resource the resource name
     * @param action   the action name
     * @return true if user has permission
     */
    boolean hasPermission(User user, String resource, String action);

    /**
     * Get all permissions for a specific role
     * 
     * @param roleType the role type
     * @return set of permission keys
     */
    Set<String> getPermissionsForRole(String roleType);

    /**
     * Validate if a permission exists and is properly formatted
     * 
     * @param resource the resource name
     * @param action   the action name
     * @return true if permission is valid
     */
    boolean isValidPermission(String resource, String action);

    /**
     * Get all active permissions
     * 
     * @return list of active permissions
     */
    List<Permission> getAllActivePermissions();

    /**
     * Find permission by permission key
     * 
     * @param permissionKey the permission key
     * @return permission or null if not found
     */
    Permission findByPermissionKey(String permissionKey);

    /**
     * Check if permission exists
     * 
     * @param permissionKey the permission key
     * @return true if permission exists
     */
    boolean permissionExists(String permissionKey);

    /**
     * Get permissions by resource name
     * 
     * @param resourceName the resource name
     * @return list of permissions for the resource
     */
    List<Permission> getPermissionsByResource(String resourceName);

    /**
     * Get role permissions
     * 
     * @param roleType the role type
     * @return set of permission keys for the role
     */
    Set<String> getRolePermissions(String roleType);

    /**
     * Check if role has specific permission
     * 
     * @param roleType      the role type
     * @param permissionKey the permission key
     * @return true if role has permission
     */
    boolean hasRolePermission(String roleType, String permissionKey);

    /**
     * Get all permissions with resource and action details (for admin purposes)
     * This includes both active and inactive permissions
     * 
     * @return list of all permissions with details
     */
    List<Permission> getAllPermissionsWithDetails();

    /**
     * Get all permissions assigned to a specific role (for admin purposes)
     * This includes both active and inactive permissions assigned to the role
     * 
     * @param roleId the role ID
     * @return list of permissions with role-specific details
     */
    List<RolePermission> getPermissionsByRole(String roleId);

    /**
     * Update permissions for a specific role (for admin purposes)
     * This allows enabling/disabling permissions at the role_permission level
     * 
     * @param roleId  the role ID
     * @param request the permission update request
     * @return list of updated permissions
     */
    List<RolePermission> updatePermissionsForRole(String roleId,
            project.ktc.springboot_app.permission.dto.PermissionUpdateRequest request);

    /**
     * Get hierarchical resource tree with permission assignments for a specific
     * role
     * This builds a tree structure showing which resources the role has permissions
     * for
     * (both directly assigned and inherited from parent resources)
     * 
     * @param roleId the role ID
     * @return list of root resources with hierarchical structure
     */
    List<project.ktc.springboot_app.permission.dto.ResourceDto> getResourceTreeForRole(String roleId);

    /**
     * Get all available permissions with constraint information for a specific role
     * This shows all permissions in the system and indicates which ones are
     * constrained
     * for the given role
     * 
     * @param roleId the role ID to check constraints against
     * @return list of available permissions with constraint flags
     */
    List<project.ktc.springboot_app.permission.dto.AvailablePermissionDto> getAllAvailablePermissions(String roleId);

    /**
     * Get all permissions for a specific role grouped by resource with
     * assignability information
     * This is used for admin interface to manage permissions for a role
     * 
     * @param roleId the role ID
     * @return grouped permissions by resource with assignability details
     */
    project.ktc.springboot_app.permission.dto.RolePermissionGroupedDto getPermissionsGroupedByResourceForRole(
            String roleId);
}
