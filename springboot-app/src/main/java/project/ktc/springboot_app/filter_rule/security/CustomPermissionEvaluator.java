package project.ktc.springboot_app.filter_rule.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.filter_rule.enums.EffectiveFilter;
import project.ktc.springboot_app.filter_rule.services.AuthorizationService;

import java.io.Serializable;

/**
 * Enhanced Permission Evaluator for Spring Security method-level security
 * Integrates RBAC with ABAC-style filter rules
 */
@Component("rbacPermissionEvaluator")
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final AuthorizationService authorizationService;

    /**
     * Evaluate permission for a target object
     * 
     * @param authentication     the current authentication
     * @param targetDomainObject the target object (can be entity ID)
     * @param permission         the permission string (e.g., "course:READ")
     * @return true if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            log.debug("Authentication or permission is null");
            return false;
        }

        User user = extractUser(authentication);
        if (user == null) {
            log.debug("Could not extract user from authentication");
            return false;
        }

        String permissionKey = permission.toString();
        log.debug("Evaluating permission: {} for user: {} on target: {}",
                permissionKey, user.getEmail(), targetDomainObject);

        // Get authorization result
        AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user, permissionKey);

        if (!result.isHasPermission()) {
            log.debug("User: {} denied access to permission: {}", user.getEmail(), permissionKey);
            return false;
        }

        // Store effective filter in thread-local for use in repository queries
        EffectiveFilterContext.setCurrentFilter(result.getEffectiveFilter());
        EffectiveFilterContext.setCurrentUser(user);
        EffectiveFilterContext.setTargetObject(targetDomainObject);

        log.debug("User: {} granted access to permission: {} with filter: {}",
                user.getEmail(), permissionKey, result.getEffectiveFilter());

        return true;
    }

    /**
     * Evaluate permission for a target type and identifier
     * 
     * @param authentication the current authentication
     * @param targetId       the target identifier
     * @param targetType     the target type (e.g., "Course")
     * @param permission     the permission string
     * @return true if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        log.debug("Evaluating permission: {} for user on targetType: {} with ID: {}",
                permission, targetType, targetId);

        // Delegate to the main method
        return hasPermission(authentication, targetId, permission);
    }

    /**
     * Extract User from Spring Security Authentication
     * 
     * @param authentication the authentication object
     * @return User entity or null if not found
     */
    private User extractUser(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }

        log.warn("Authentication principal is not a User instance: {}",
                authentication.getPrincipal().getClass());
        return null;
    }
}
