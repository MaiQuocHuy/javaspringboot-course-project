package project.ktc.springboot_app.security;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.permission.services.PermissionServiceImp;

/**
 * Custom permission evaluator for Spring Security
 * Implements fine-grained permission checking
 * using @PreAuthorize("hasPermission(...)")
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(CustomPermissionEvaluator.class);

    private final PermissionServiceImp permissionService;

    public CustomPermissionEvaluator(PermissionServiceImp permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Evaluate permission for a target object
     * 
     * @param authentication     the authentication object
     * @param targetDomainObject the target object (can be null for global
     *                           permissions)
     * @param permission         the permission to check (e.g., "course:create",
     *                           "course:edit")
     * @return true if user has permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Permission denied - not authenticated");
            return false;
        }

        String permissionKey = permission.toString();
        logger.debug("Checking permission: {} for user: {}",
                permissionKey, authentication.getName());

        try {
            // Get user from authentication
            User user = extractUserFromAuthentication(authentication);
            if (user == null) {
                logger.warn("Could not extract user from authentication");
                return false;
            }

            // Check permission using permission service
            boolean hasPermission = permissionService.hasPermission(user, permissionKey);
            logger.debug("Permission check result - User: {}, Permission: {}, Result: {}",
                    user.getEmail(), permissionKey, hasPermission);

            return hasPermission;

        } catch (Exception e) {
            logger.error("Error checking permission: {} for user: {}",
                    permissionKey, authentication.getName(), e);
            return false;
        }
    }

    /**
     * Evaluate permission for a target by ID
     * 
     * @param authentication the authentication object
     * @param targetId       the target ID
     * @param targetType     the target type (e.g., "Course", "User")
     * @param permission     the permission to check
     * @return true if user has permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("Permission denied - not authenticated");
            return false;
        }

        String permissionKey = permission.toString();
        logger.debug("Checking permission: {} for targetType: {} with ID: {} for user: {}",
                permissionKey, targetType, targetId, authentication.getName());

        try {
            // Get user from authentication
            User user = extractUserFromAuthentication(authentication);
            if (user == null) {
                logger.warn("Could not extract user from authentication");
                return false;
            }

            // For resource-specific permissions, you can implement additional logic here
            // For now, we'll use the basic permission check
            boolean hasPermission = permissionService.hasPermission(user, permissionKey);

            // You can extend this to check ownership or specific entity permissions
            if (hasPermission && targetId != null) {
                hasPermission = checkEntitySpecificPermission(user, targetType, targetId, permissionKey);
            }

            logger.debug("Permission check result - User: {}, Permission: {}, TargetType: {}, TargetId: {}, Result: {}",
                    user.getEmail(), permissionKey, targetType, targetId, hasPermission);

            return hasPermission;

        } catch (Exception e) {
            logger.error("Error checking permission: {} for targetType: {} with ID: {} for user: {}",
                    permissionKey, targetType, targetId, authentication.getName(), e);
            return false;
        }
    }

    /**
     * Extract User entity from Spring Security Authentication
     * 
     * @param authentication the authentication object
     * @return User entity or null if not found
     */
    private User extractUserFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof UserDetails) {
            // If using Spring Security UserDetails, you might need to load the full User
            // entity
            String email = ((UserDetails) principal).getUsername();
            logger.debug("Loading user by email: {}", email);
            // You would need to inject UserService or UserRepository to load the full user
            // For now, we'll return null and log a warning
            logger.warn("Authentication principal is UserDetails but not User entity. Email: {}", email);
            return null;
        } else if (principal instanceof String) {
            String email = (String) principal;
            logger.debug("Loading user by email string: {}", email);
            // You would need to inject UserService or UserRepository to load the full user
            logger.warn("Authentication principal is String. Email: {}", email);
            return null;
        }

        logger.warn("Unknown authentication principal type: {}",
                principal != null ? principal.getClass().getName() : "null");
        return null;
    }

    /**
     * Check entity-specific permissions (e.g., ownership, team membership)
     * This method can be extended to implement more complex permission logic
     * 
     * @param user          the user
     * @param targetType    the target entity type
     * @param targetId      the target entity ID
     * @param permissionKey the permission key
     * @return true if user has permission for the specific entity
     */
    private boolean checkEntitySpecificPermission(User user, String targetType, Serializable targetId,
            String permissionKey) {
        // This is where you can implement entity-specific permission checks
        // For example:
        // - Check if user owns the resource
        // - Check if user is part of the same team/organization
        // - Check hierarchical permissions

        logger.debug("Checking entity-specific permission for user: {}, targetType: {}, targetId: {}, permission: {}",
                user.getEmail(), targetType, targetId, permissionKey);

        // For now, return true (basic permission check already passed)
        // You can extend this method based on your business requirements
        return true;
    }
}
