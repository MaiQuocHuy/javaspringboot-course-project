package project.ktc.springboot_app.permission.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.permission.entity.FilterType;
import project.ktc.springboot_app.permission.entity.RolePermission;
import project.ktc.springboot_app.permission.repositories.RolePermissionRepository;
import project.ktc.springboot_app.permission.dto.UserPermissionsDto;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authorization Service using the new database schema
 * Handles permission evaluation with filter_types table
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthorizationService {

    private final RolePermissionRepository rolePermissionRepository;

    /**
     * Evaluate permission for a user and return authorization result
     * 
     * @param user          the user
     * @param permissionKey the permission key (e.g., "course:READ")
     * @return authorization result with effective filter
     */
    public AuthorizationResult evaluatePermission(User user, String permissionKey) {
        log.debug("Evaluating permission: {} for user: {}", permissionKey, user.getEmail());

        try {
            // Get user's role permissions for this permission
            List<RolePermission> rolePermissions = rolePermissionRepository
                    .findActiveByRoleAndPermission(user.getRole().getId(), permissionKey);

            if (rolePermissions.isEmpty()) {
                log.debug("No active permissions found for user: {} and permission: {}",
                        user.getEmail(), permissionKey);
                return AuthorizationResult.denied("No permission found");
            }

            // Determine the most permissive filter type
            FilterType.EffectiveFilterType effectiveFilter = determineEffectiveFilter(rolePermissions);

            log.debug("Effective filter determined: {} for user: {} and permission: {}",
                    effectiveFilter, user.getEmail(), permissionKey);

            return AuthorizationResult.allowed(effectiveFilter, user);

        } catch (Exception e) {
            log.error("Error evaluating permission: {} for user: {}", permissionKey, user.getEmail(), e);
            return AuthorizationResult.denied("Permission evaluation failed: " + e.getMessage());
        }
    }

    /**
     * Check if user has a specific permission
     * 
     * @param user          the user
     * @param permissionKey the permission key
     * @return true if user has the permission
     */
    public boolean hasPermission(User user, String permissionKey) {
        AuthorizationResult result = evaluatePermission(user, permissionKey);
        return result.isAllowed();
    }

    /**
     * Get effective filter for a user and permission
     * 
     * @param user          the user
     * @param permissionKey the permission key
     * @return effective filter type
     */
    public FilterType.EffectiveFilterType getEffectiveFilter(User user, String permissionKey) {
        AuthorizationResult result = evaluatePermission(user, permissionKey);
        return result.getEffectiveFilter();
    }

    /**
     * Get all permissions for a user with their effective filters
     * 
     * @param user the user
     * @return set of permission keys the user has access to
     */
    public Set<String> getUserPermissions(User user) {
        log.debug("Getting all permissions for user: {}", user.getEmail());

        List<RolePermission> rolePermissions = rolePermissionRepository
                .findActiveByRoleId(user.getRole().getId());

        return rolePermissions.stream()
                .map(RolePermission::getPermissionKey)
                .collect(Collectors.toSet());
    }

    /**
     * Check if user has specific filter type for a permission
     * 
     * @param user          the user
     * @param permissionKey the permission key
     * @param filterType    the filter type to check
     * @return true if user has this filter type
     */
    public boolean hasFilterType(User user, String permissionKey, FilterType.EffectiveFilterType filterType) {
        List<RolePermission> rolePermissions = rolePermissionRepository
                .findActiveByRoleAndPermission(user.getRole().getId(), permissionKey);

        return rolePermissions.stream()
                .anyMatch(rp -> rp.getEffectiveFilterType() == filterType);
    }

    /**
     * Check if user has a specific permission with ALL access (filter-type-001)
     * This is used for admin notifications - only users with ALL access should
     * receive them
     * 
     * @param user          the user
     * @param permissionKey the permission key
     * @return true if user has the permission with ALL access
     */
    public boolean hasPermissionWithAllAccess(User user, String permissionKey) {
        log.debug("Checking if user: {} has permission: {} with ALL access", user.getEmail(), permissionKey);

        AuthorizationResult result = evaluatePermission(user, permissionKey);
        boolean hasAllAccess = result.isAllowed() && result.getEffectiveFilter() == FilterType.EffectiveFilterType.ALL;

        log.debug("User: {} has permission: {} with ALL access: {}", user.getEmail(), permissionKey, hasAllAccess);
        return hasAllAccess;
    }

    /**
     * Determine the most permissive filter type from multiple role permissions
     * Priority: ALL > OWN > DENIED
     * 
     * @param rolePermissions list of role permissions
     * @return most permissive effective filter type
     */
    private FilterType.EffectiveFilterType determineEffectiveFilter(List<RolePermission> rolePermissions) {
        // Check if any permission has ALL access (highest priority)
        boolean hasAll = rolePermissions.stream().anyMatch(RolePermission::hasAllAccess);
        if (hasAll) {
            return FilterType.EffectiveFilterType.ALL;
        }

        // Check if any permission has OWN access
        boolean hasOwn = rolePermissions.stream().anyMatch(RolePermission::hasOwnAccess);
        if (hasOwn) {
            return FilterType.EffectiveFilterType.OWN;
        }

        // Default to DENIED if no valid filter types found
        return FilterType.EffectiveFilterType.DENIED;
    }

    /**
     * Authorization result wrapper
     */
    public static class AuthorizationResult {
        private final boolean allowed;
        private final FilterType.EffectiveFilterType effectiveFilter;
        private final User user;
        private final String reason;

        private AuthorizationResult(boolean allowed, FilterType.EffectiveFilterType effectiveFilter,
                User user, String reason) {
            this.allowed = allowed;
            this.effectiveFilter = effectiveFilter;
            this.user = user;
            this.reason = reason;
        }

        public static AuthorizationResult allowed(FilterType.EffectiveFilterType effectiveFilter, User user) {
            return new AuthorizationResult(true, effectiveFilter, user, "Permission granted");
        }

        public static AuthorizationResult denied(String reason) {
            return new AuthorizationResult(false, FilterType.EffectiveFilterType.DENIED, null, reason);
        }

        // Getters
        public boolean isAllowed() {
            return allowed;
        }

        public FilterType.EffectiveFilterType getEffectiveFilter() {
            return effectiveFilter;
        }

        public User getUser() {
            return user;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "AuthorizationResult{" +
                    "allowed=" + allowed +
                    ", effectiveFilter=" + effectiveFilter +
                    ", user=" + (user != null ? user.getEmail() : null) +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }

    /**
     * Get comprehensive user permissions information
     * 
     * @param user the user
     * @return user permissions DTO with detailed information
     */
    public UserPermissionsDto getUserPermissionsDto(User user) {
        log.debug("Getting comprehensive permissions for user: {}", user.getEmail());

        try {
            // Get all role permissions for the user
            List<RolePermission> rolePermissions = rolePermissionRepository
                    .findActiveByRoleId(user.getRole().getId());

            // Extract permission keys
            Set<String> permissions = rolePermissions.stream()
                    .map(RolePermission::getPermissionKey)
                    .collect(Collectors.toSet());

            // Create detailed permission information
            List<UserPermissionsDto.PermissionDetailDto> detailedPermissions = rolePermissions.stream()
                    .map(rp -> UserPermissionsDto.PermissionDetailDto.builder()
                            .permissionKey(rp.getPermissionKey())
                            .description(rp.getPermission().getDescription())
                            .resource(rp.getPermission().getResourceName())
                            .action(rp.getPermission().getActionName())
                            .filterType(rp.getFilterType().getName())
                            .canAccessAll(rp.hasAllAccess())
                            .canAccessOwn(rp.hasOwnAccess())
                            .build())
                    .collect(Collectors.toList());

            // Create role info
            UserPermissionsDto.RoleInfoDto roleInfo = UserPermissionsDto.RoleInfoDto.builder()
                    .id(user.getRole().getId())
                    .name(user.getRole().getRole())
                    .build();

            // Build final DTO
            return UserPermissionsDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(roleInfo)
                    .permissions(permissions)
                    .detailedPermissions(detailedPermissions)
                    .build();

        } catch (Exception e) {
            log.error("Error getting user permissions for user: {}", user.getEmail(), e);

            // Return minimal permissions on error
            UserPermissionsDto.RoleInfoDto roleInfo = UserPermissionsDto.RoleInfoDto.builder()
                    .id(user.getRole().getId())
                    .name(user.getRole().getRole())
                    .build();

            return UserPermissionsDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .role(roleInfo)
                    .permissions(Set.of())
                    .detailedPermissions(List.of())
                    .build();
        }
    }
}
