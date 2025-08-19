package project.ktc.springboot_app.filter_rule.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.filter_rule.entity.RoleFilterRule;
import project.ktc.springboot_app.filter_rule.enums.EffectiveFilter;
import project.ktc.springboot_app.filter_rule.repositories.RoleFilterRuleRepository;

import java.util.List;

/**
 * Authorization Service for RBAC with ABAC-style filter rules
 * Evaluates permissions and resolves effective filters for users
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final RoleFilterRuleRepository roleFilterRuleRepository;

    /**
     * Get the effective filter for a user and permission key
     * Combines multiple role-based filter rules using priority-based conflict
     * resolution
     *
     * @param user          the authenticated user
     * @param permissionKey the permission key (e.g., "course:READ")
     * @return the effective filter with highest priority
     */
    public EffectiveFilter getEffectiveFilter(User user, String permissionKey) {
        log.debug("Evaluating effective filter for user: {} and permission: {}",
                user.getEmail(), permissionKey);

        // Get all active filter rules for user's roles and the permission
        List<RoleFilterRule> filterRules = roleFilterRuleRepository
                .findActiveFilterRulesByUserAndPermission(user.getId(), permissionKey);

        if (filterRules.isEmpty()) {
            log.debug("No filter rules found for user: {} and permission: {}",
                    user.getEmail(), permissionKey);
            return EffectiveFilter.DENIED;
        }

        // Resolve conflicts by selecting the filter with highest priority
        EffectiveFilter effectiveFilter = filterRules.stream()
                .map(rule -> EffectiveFilter.fromFilterType(rule.getFilterType()))
                .reduce(EffectiveFilter.DENIED, EffectiveFilter::combineWith);

        log.debug("Resolved effective filter: {} for user: {} and permission: {}",
                effectiveFilter, user.getEmail(), permissionKey);

        return effectiveFilter;
    }

    /**
     * Check if user has permission (without filter resolution)
     *
     * @param user          the authenticated user
     * @param permissionKey the permission key
     * @return true if user has the permission
     */
    public boolean hasPermission(User user, String permissionKey) {
        EffectiveFilter filter = getEffectiveFilter(user, permissionKey);
        return filter != EffectiveFilter.DENIED;
    }

    /**
     * Check if user has permission and get effective filter in one call
     *
     * @param user          the authenticated user
     * @param permissionKey the permission key
     * @return authorization result with permission status and effective filter
     */
    public AuthorizationResult evaluatePermission(User user, String permissionKey) {
        EffectiveFilter effectiveFilter = getEffectiveFilter(user, permissionKey);
        boolean hasPermission = effectiveFilter != EffectiveFilter.DENIED;

        return AuthorizationResult.builder()
                .hasPermission(hasPermission)
                .effectiveFilter(effectiveFilter)
                .user(user)
                .permissionKey(permissionKey)
                .build();
    }

    /**
     * Authorization result containing permission status and effective filter
     */
    @lombok.Data
    @lombok.Builder
    public static class AuthorizationResult {
        private boolean hasPermission;
        private EffectiveFilter effectiveFilter;
        private User user;
        private String permissionKey;
    }
}
