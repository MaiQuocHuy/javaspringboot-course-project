package project.ktc.springboot_app.filter_rule.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.filter_rule.entity.RoleFilterRule;

import java.util.List;
import java.util.Set;

/**
 * Repository for Role Filter Rule operations with NORMALIZED DESIGN
 * Handles queries for scope-based permission filtering using role_permission_id
 * foreign key
 */
@Repository
public interface RoleFilterRuleRepository extends JpaRepository<RoleFilterRule, String> {

        /**
         * Find all active filter rules for a specific role permission
         * 
         * @param rolePermissionId The role permission ID (foreign key)
         * @return List of active filter rules
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "WHERE rfr.rolePermissionId = :rolePermissionId " +
                        "AND rfr.isActive = true")
        List<RoleFilterRule> findActiveRulesByRolePermission(@Param("rolePermissionId") String rolePermissionId);

        /**
         * Find all active filter rules for multiple role permissions
         * Used when checking permissions across multiple role-permission combinations
         * 
         * @param rolePermissionIds Set of role permission IDs
         * @return List of active filter rules from all role permissions
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "WHERE rfr.rolePermissionId IN :rolePermissionIds " +
                        "AND rfr.isActive = true")
        List<RoleFilterRule> findActiveRulesByRolePermissions(
                        @Param("rolePermissionIds") Set<String> rolePermissionIds);

        /**
         * Find all active filter rules for a specific role (through role_permission
         * join)
         * 
         * @param roleId The role ID
         * @return List of all active filter rules for the role
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "JOIN rfr.rolePermission rp " +
                        "WHERE rp.role.id = :roleId " +
                        "AND rfr.isActive = true")
        List<RoleFilterRule> findActiveRulesByRole(@Param("roleId") String roleId);

        /**
         * Find all active filter rules for multiple roles and a specific permission
         * Used when user has multiple roles and we need to check specific permission
         * 
         * @param roleIds       Set of role IDs
         * @param permissionKey The permission key
         * @return List of active filter rules from all roles
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "JOIN rfr.rolePermission rp " +
                        "WHERE rp.role.id IN :roleIds " +
                        "AND rp.permission.permissionKey = :permissionKey " +
                        "AND rfr.isActive = true")
        List<RoleFilterRule> findActiveRulesByRolesAndPermission(
                        @Param("roleIds") Set<String> roleIds,
                        @Param("permissionKey") String permissionKey);

        /**
         * Find all active filter rules for multiple roles
         * 
         * @param roleIds Set of role IDs
         * @return List of all active filter rules for the roles
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "JOIN rfr.rolePermission rp " +
                        "WHERE rp.role.id IN :roleIds " +
                        "AND rfr.isActive = true")
        List<RoleFilterRule> findActiveRulesByRoles(@Param("roleIds") Set<String> roleIds);

        /**
         * Check if a specific filter rule exists and is active
         * 
         * @param rolePermissionId The role permission ID
         * @param filterType       The filter type
         * @return true if the rule exists and is active
         */
        @Query("SELECT COUNT(rfr) > 0 FROM RoleFilterRule rfr " +
                        "WHERE rfr.rolePermissionId = :rolePermissionId " +
                        "AND rfr.filterType = :filterType " +
                        "AND rfr.isActive = true")
        boolean existsActiveRule(
                        @Param("rolePermissionId") String rolePermissionId,
                        @Param("filterType") RoleFilterRule.FilterType filterType);

        /**
         * Get distinct filter types for a specific role permission
         * 
         * @param rolePermissionId The role permission ID
         * @return Set of filter types
         */
        @Query("SELECT rfr.filterType FROM RoleFilterRule rfr " +
                        "WHERE rfr.rolePermissionId = :rolePermissionId " +
                        "AND rfr.isActive = true")
        Set<RoleFilterRule.FilterType> findFilterTypesByRolePermission(
                        @Param("rolePermissionId") String rolePermissionId);

        /**
         * Find filter rules by permission key (for admin/debug purposes)
         * 
         * @param permissionKey The permission key
         * @return List of all active rules for the permission
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "JOIN rfr.rolePermission rp " +
                        "WHERE rp.permission.permissionKey = :permissionKey " +
                        "AND rfr.isActive = true")
        List<RoleFilterRule> findByPermissionKeyAndIsActiveTrue(@Param("permissionKey") String permissionKey);

        /**
         * Find all rules for a specific role (active and inactive) - for admin
         * management
         * 
         * @param roleId The role ID
         * @return List of all rules for the role
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "JOIN rfr.rolePermission rp " +
                        "WHERE rp.role.id = :roleId " +
                        "ORDER BY rp.permission.permissionKey ASC, rfr.filterType ASC")
        List<RoleFilterRule> findByRoleIdOrderByPermissionKeyAscFilterTypeAsc(@Param("roleId") String roleId);

        /**
         * Get distinct permission keys that have filter rules for a role
         * 
         * @param roleId The role ID
         * @return Set of permission keys
         */
        @Query("SELECT DISTINCT rp.permission.permissionKey FROM RoleFilterRule rfr " +
                        "JOIN rfr.rolePermission rp " +
                        "WHERE rp.role.id = :roleId " +
                        "AND rfr.isActive = true")
        Set<String> findPermissionKeysByRole(@Param("roleId") String roleId);

        /**
         * Find all filter rules for a specific role permission (for detailed analysis)
         * 
         * @param rolePermissionId The role permission ID
         * @return List of all rules (active and inactive) for the role permission
         */
        List<RoleFilterRule> findByRolePermissionId(String rolePermissionId);

        /**
         * Count active filter rules for a role permission
         * 
         * @param rolePermissionId The role permission ID
         * @return Count of active filter rules
         */
        @Query("SELECT COUNT(rfr) FROM RoleFilterRule rfr " +
                        "WHERE rfr.rolePermissionId = :rolePermissionId " +
                        "AND rfr.isActive = true")
        long countActiveByRolePermission(@Param("rolePermissionId") String rolePermissionId);

        /**
         * Find all active filter rules for a user and specific permission key
         * This method joins through role_permission to get the user's role filter rules
         * 
         * @param userId        The user ID
         * @param permissionKey The permission key (e.g., "course:READ")
         * @return List of active filter rules for the user and permission
         */
        @Query("SELECT rfr FROM RoleFilterRule rfr " +
                        "JOIN rfr.rolePermission rp " +
                        "JOIN rp.role ur " +
                        "JOIN User u ON u.role.id = ur.id " +
                        "WHERE u.id = :userId " +
                        "AND rp.permission.permissionKey = :permissionKey " +
                        "AND rfr.isActive = true " +
                        "AND rp.isActive = true")
        List<RoleFilterRule> findActiveFilterRulesByUserAndPermission(
                        @Param("userId") String userId,
                        @Param("permissionKey") String permissionKey);
}
