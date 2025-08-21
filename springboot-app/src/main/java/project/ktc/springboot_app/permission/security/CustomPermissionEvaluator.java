package project.ktc.springboot_app.permission.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.permission.entity.FilterType;
import project.ktc.springboot_app.permission.services.AuthorizationService;

import java.io.Serializable;

/**
 * Custom Permission Evaluator for Spring Security @PreAuthorize
 * Uses the new database schema with filter_types table
 */
@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final AuthorizationService authorizationService;

    /**
     * Evaluate permission for domain object
     * 
     * @param authentication     the authentication object
     * @param targetDomainObject the target object (can be null)
     * @param permission         the permission string
     * @return true if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        User user = extractUser(authentication);
        if (user == null) {
            log.debug("User not found in authentication");
            return false;
        }

        String permissionKey = permission.toString();

        log.debug("Evaluating permission: {} for user: {} with target: {}",
                permissionKey, user.getEmail(),
                targetDomainObject != null ? targetDomainObject.getClass().getSimpleName() : "null");

        try {
            AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user,
                    permissionKey);

            if (!result.isAllowed()) {
                log.debug("Permission denied: {} for user: {}, reason: {}",
                        permissionKey, user.getEmail(), result.getReason());
                return false;
            }

            // Store the effective filter and user in thread-local context for later use
            EffectiveFilterContext.setCurrentFilter(result.getEffectiveFilter());
            EffectiveFilterContext.setCurrentUser(result.getUser());

            log.debug("Permission granted: {} for user: {} with filter: {}",
                    permissionKey, user.getEmail(), result.getEffectiveFilter());

            return true;

        } catch (Exception e) {
            log.error("Error evaluating permission: {} for user: {}", permissionKey, user.getEmail(), e);
            return false;
        }
    }

    /**
     * Evaluate permission for target by ID
     * 
     * @param authentication the authentication object
     * @param targetId       the target ID
     * @param targetType     the target type
     * @param permission     the permission string
     * @return true if access is granted
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        User user = extractUser(authentication);
        if (user == null) {
            log.debug("User not found in authentication");
            return false;
        }

        String permissionKey = permission.toString();

        log.debug("Evaluating permission: {} for user: {} with targetId: {} and targetType: {}",
                permissionKey, user.getEmail(), targetId, targetType);

        try {
            AuthorizationService.AuthorizationResult result = authorizationService.evaluatePermission(user,
                    permissionKey);

            if (!result.isAllowed()) {
                log.debug("Permission denied: {} for user: {}, reason: {}",
                        permissionKey, user.getEmail(), result.getReason());
                return false;
            }

            // Store the effective filter and user in thread-local context for later use
            EffectiveFilterContext.setCurrentFilter(result.getEffectiveFilter());
            EffectiveFilterContext.setCurrentUser(result.getUser());

            log.debug("Permission granted: {} for user: {} with filter: {} for targetId: {}",
                    permissionKey, user.getEmail(), result.getEffectiveFilter(), targetId);

            return true;

        } catch (Exception e) {
            log.error("Error evaluating permission: {} for user: {} with targetId: {}",
                    permissionKey, user.getEmail(), targetId, e);
            return false;
        }
    }

    /**
     * Extract User from Authentication object
     * 
     * @param authentication the authentication object
     * @return User object or null if not found
     */
    private User extractUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        }

        // If principal is UserDetails, try to get User from it
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // This would require additional logic to get User from UserDetails
            // For now, assume principal is always User
            log.warn("Principal is UserDetails but not User: {}", principal.getClass());
            return null;
        }

        log.warn("Unknown principal type: {}", principal.getClass());
        return null;
    }

    /**
     * Thread-local context for storing effective filter information
     */
    public static class EffectiveFilterContext {
        private static final ThreadLocal<FilterType.EffectiveFilterType> currentFilter = new ThreadLocal<>();
        private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

        public static FilterType.EffectiveFilterType getCurrentFilter() {
            return currentFilter.get();
        }

        public static void setCurrentFilter(FilterType.EffectiveFilterType filter) {
            currentFilter.set(filter);
        }

        public static User getCurrentUser() {
            return currentUser.get();
        }

        public static void setCurrentUser(User user) {
            currentUser.set(user);
        }

        public static void clear() {
            currentFilter.remove();
            currentUser.remove();
        }
    }
}
