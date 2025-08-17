package project.ktc.springboot_app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.entity.*;
import project.ktc.springboot_app.auth.entitiy.User;

import java.util.List;

/**
 * Permission Service for Role-Based Access Control
 * Provides business logic for checking permissions and managing role-permission
 * mappings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    /**
     * Check if a user has a specific permission
     * 
     * @param user          The user to check
     * @param permissionKey The permission key (format: resource:action)
     * @return true if user has the permission, false otherwise
     */
    public boolean hasPermission(User user, String permissionKey) {
        if (user == null || user.getRole() == null || permissionKey == null) {
            return false;
        }

        try {
            // In a real implementation, this would query the database
            // For now, we just validate the structure and log the check
            log.debug("Checking permission '{}' for user '{}' with role '{}'",
                    permissionKey, user.getEmail(), user.getRole().getRole());

            // Validate permission key format
            if (!permissionKey.contains(":")) {
                log.warn("Invalid permission key format: {}", permissionKey);
                return false;
            }

            String[] parts = permissionKey.split(":");
            if (parts.length != 2) {
                log.warn("Invalid permission key format: {}", permissionKey);
                return false;
            }

            String resource = parts[0];
            String action = parts[1];

            log.debug("Permission check - Resource: '{}', Action: '{}', Role: '{}'",
                    resource, action, user.getRole().getRole());

            // Basic role-based logic for demonstration
            UserRole.RoleType roleType = user.getRole().getRole();
            return checkRolePermission(roleType, resource, action);

        } catch (Exception e) {
            log.error("Error checking permission '{}' for user '{}'", permissionKey, user.getEmail(), e);
            return false;
        }
    }

    /**
     * Basic role-based permission checking logic
     * In a real implementation, this would query the role_permissions table
     */
    private boolean checkRolePermission(UserRole.RoleType roleType, String resource, String action) {
        switch (roleType) {
            case ADMIN:
                // Admin has access to everything
                return true;

            case INSTRUCTOR:
                // Instructors can manage courses and lessons
                if ("course".equals(resource)) {
                    return List.of("CREATE", "READ", "UPDATE", "PUBLISH").contains(action);
                }
                if ("lesson".equals(resource)) {
                    return List.of("CREATE", "READ", "UPDATE").contains(action);
                }
                if ("enrollment".equals(resource)) {
                    return "READ".equals(action);
                }
                return false;

            case STUDENT:
                // Students have limited read access and can enroll
                if ("course".equals(resource) || "lesson".equals(resource)) {
                    return "READ".equals(action);
                }
                if ("enrollment".equals(resource)) {
                    return List.of("ENROLL", "READ").contains(action);
                }
                return false;

            default:
                return false;
        }
    }

    /**
     * Validate that all entities work together correctly
     */
    public boolean validateEntities() {
        try {
            // Test Resource entity
            Resource resource = Resource.builder()
                    .name("test-resource")
                    .description("Test resource for validation")
                    .resourcePath("/test")
                    .isActive(true)
                    .build();

            log.info("Resource validation: {}", resource.toString());

            // Test Action entity
            Action action = Action.builder()
                    .name("TEST")
                    .description("Test action for validation")
                    .actionType(Action.ActionType.CUSTOM)
                    .isActive(true)
                    .build();

            log.info("Action validation: {}", action.toString());

            // Test Permission entity
            Permission permission = Permission.builder()
                    .resource(resource)
                    .action(action)
                    .description("Test permission for validation")
                    .isActive(true)
                    .build();

            // Manually generate permission key for test
            permission.setPermissionKey(resource.getName() + ":" + action.getName());

            log.info("Permission validation: {}", permission.toString());

            // Test UserRole (should already exist)
            UserRole userRole = UserRole.builder()
                    .role(UserRole.RoleType.STUDENT)
                    .build();

            log.info("UserRole validation: {}", userRole.toString());

            // Test RolePermission entity
            RolePermission rolePermission = RolePermission.builder()
                    .role(userRole)
                    .permission(permission)
                    .isActive(true)
                    .build();

            log.info("RolePermission validation: {}", rolePermission.toString());

            // Test convenience methods
            log.info("Permission matches test: {}", permission.matches("test-resource", "TEST"));
            log.info("Action is CRUD: {}", action.isCrudAction());
            log.info("Action is Business: {}", action.isBusinessAction());

            return true;

        } catch (Exception e) {
            log.error("Entity validation failed", e);
            return false;
        }
    }
}
