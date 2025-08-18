package project.ktc.springboot_app.permission.interfaces;

import java.util.List;

import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.CreateRoleRequest;

/**
 * Service interface for role management operations
 */
public interface RoleService {

    /**
     * Create a new role
     * 
     * @param request the role creation request
     * @return the created role entity
     * @throws IllegalArgumentException if role name already exists
     */
    UserRole createRole(CreateRoleRequest request);

    /**
     * Find role by ID
     * 
     * @param roleId the role ID
     * @return the role entity
     * @throws RuntimeException if role not found
     */
    UserRole findById(String roleId);

    /**
     * Find role by role type
     * 
     * @param roleType the role type
     * @return the role entity or null if not found
     */
    UserRole findByRoleType(String roleType);

    /**
     * Check if role name already exists
     * 
     * @param roleName the role name to check
     * @return true if role exists, false otherwise
     */
    boolean existsByRoleName(String roleName);

    /**
     * Get all roles
     * 
     * @return list of all roles
     */
    List<UserRole> getAllRoles();
}
