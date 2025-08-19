package project.ktc.springboot_app.service;

import project.ktc.springboot_app.entity.RoleFilterRule;
import project.ktc.springboot_app.auth.entitiy.User;

import java.util.List;
import java.util.Set;

/**
 * Service interface for Role Filter Rule operations
 * Handles scope-based permission filtering logic
 */
public interface RoleFilterRuleService {

    /**
     * Get all active filter types for a user and permission
     * Combines rules from all user roles using OR logic
     * 
     * @param user          The user
     * @param permissionKey The permission key (e.g., "course:READ")
     * @return Set of filter types from all user roles
     */
    Set<RoleFilterRule.FilterType> getFilterTypesForUser(User user, String permissionKey);

    /**
     * Get all active filter types for specific roles and permission
     * 
     * @param roleIds       Set of role IDs
     * @param permissionKey The permission key
     * @return Set of filter types from all roles
     */
    Set<RoleFilterRule.FilterType> getFilterTypesForRoles(Set<String> roleIds, String permissionKey);

    /**
     * Check if user has specific filter type for a permission
     * 
     * @param user          The user
     * @param permissionKey The permission key
     * @param filterType    The filter type to check
     * @return true if user has this filter type
     */
    boolean hasFilterType(User user, String permissionKey, RoleFilterRule.FilterType filterType);

    /**
     * Get all active filter rules for a user and permission
     * 
     * @param user          The user
     * @param permissionKey The permission key
     * @return List of active filter rules
     */
    List<RoleFilterRule> getFilterRulesForUser(User user, String permissionKey);

    /**
     * Check if user has ANY filter rules for a permission
     * If no rules exist, access should be denied by default
     * 
     * @param user          The user
     * @param permissionKey The permission key
     * @return true if user has any filter rules for the permission
     */
    boolean hasAnyFilterRules(User user, String permissionKey);

    /**
     * Get all permissions that have filter rules for a user
     * 
     * @param user The user
     * @return Set of permission keys that have filter rules
     */
    Set<String> getPermissionsWithFilterRules(User user);

    /**
     * Create a new filter rule
     * 
     * @param roleId        The role ID
     * @param permissionKey The permission key
     * @param filterType    The filter type
     * @return Created filter rule
     */
    RoleFilterRule createFilterRule(String roleId, String permissionKey, RoleFilterRule.FilterType filterType);

    /**
     * Update filter rule active status
     * 
     * @param ruleId   The rule ID
     * @param isActive The new active status
     * @return Updated filter rule
     */
    RoleFilterRule updateFilterRuleStatus(String ruleId, boolean isActive);

    /**
     * Delete a filter rule
     * 
     * @param ruleId The rule ID
     */
    void deleteFilterRule(String ruleId);

    /**
     * Get all filter rules for a role (for admin management)
     * 
     * @param roleId The role ID
     * @return List of all filter rules for the role
     */
    List<RoleFilterRule> getFilterRulesByRole(String roleId);

    /**
     * Get all filter rules for a permission (for admin management)
     * 
     * @param permissionKey The permission key
     * @return List of all active filter rules for the permission
     */
    List<RoleFilterRule> getFilterRulesByPermission(String permissionKey);
}
