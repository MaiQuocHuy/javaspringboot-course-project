package project.ktc.springboot_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import project.ktc.springboot_app.entity.RoleFilterRule;

import java.util.List;
import java.util.Set;

/**
 * Repository for Role Filter Rule operations
 * Handles queries for scope-based permission filtering
 */
@Repository
public interface RoleFilterRuleRepository extends JpaRepository<RoleFilterRule, String> {

    /**
     * Find all active filter rules for a specific role and permission
     * 
     * @param roleId        The role ID
     * @param permissionKey The permission key (e.g., "course:READ")
     * @return List of active filter rules
     */
    @Query("SELECT rfr FROM RoleFilterRule rfr " +
            "WHERE rfr.roleId = :roleId " +
            "AND rfr.permissionKey = :permissionKey " +
            "AND rfr.isActive = true")
    List<RoleFilterRule> findActiveRulesByRoleAndPermission(
            @Param("roleId") String roleId,
            @Param("permissionKey") String permissionKey);

    /**
     * Find all active filter rules for multiple roles and a specific permission
     * Used when user has multiple roles
     * 
     * @param roleIds       Set of role IDs
     * @param permissionKey The permission key
     * @return List of active filter rules from all roles
     */
    @Query("SELECT rfr FROM RoleFilterRule rfr " +
            "WHERE rfr.roleId IN :roleIds " +
            "AND rfr.permissionKey = :permissionKey " +
            "AND rfr.isActive = true")
    List<RoleFilterRule> findActiveRulesByRolesAndPermission(
            @Param("roleIds") Set<String> roleIds,
            @Param("permissionKey") String permissionKey);

    /**
     * Find all active filter rules for a specific role
     * 
     * @param roleId The role ID
     * @return List of all active filter rules for the role
     */
    @Query("SELECT rfr FROM RoleFilterRule rfr " +
            "WHERE rfr.roleId = :roleId " +
            "AND rfr.isActive = true")
    List<RoleFilterRule> findActiveRulesByRole(@Param("roleId") String roleId);

    /**
     * Find all active filter rules for multiple roles
     * 
     * @param roleIds Set of role IDs
     * @return List of all active filter rules for the roles
     */
    @Query("SELECT rfr FROM RoleFilterRule rfr " +
            "WHERE rfr.roleId IN :roleIds " +
            "AND rfr.isActive = true")
    List<RoleFilterRule> findActiveRulesByRoles(@Param("roleIds") Set<String> roleIds);

    /**
     * Check if a specific filter rule exists and is active
     * 
     * @param roleId        The role ID
     * @param permissionKey The permission key
     * @param filterType    The filter type
     * @return true if the rule exists and is active
     */
    @Query("SELECT COUNT(rfr) > 0 FROM RoleFilterRule rfr " +
            "WHERE rfr.roleId = :roleId " +
            "AND rfr.permissionKey = :permissionKey " +
            "AND rfr.filterType = :filterType " +
            "AND rfr.isActive = true")
    boolean existsActiveRule(
            @Param("roleId") String roleId,
            @Param("permissionKey") String permissionKey,
            @Param("filterType") RoleFilterRule.FilterType filterType);

    /**
     * Get distinct permission keys that have filter rules for a role
     * 
     * @param roleId The role ID
     * @return Set of permission keys
     */
    @Query("SELECT DISTINCT rfr.permissionKey FROM RoleFilterRule rfr " +
            "WHERE rfr.roleId = :roleId " +
            "AND rfr.isActive = true")
    Set<String> findPermissionKeysByRole(@Param("roleId") String roleId);

    /**
     * Get distinct filter types for a specific role and permission
     * 
     * @param roleId        The role ID
     * @param permissionKey The permission key
     * @return Set of filter types
     */
    @Query("SELECT rfr.filterType FROM RoleFilterRule rfr " +
            "WHERE rfr.roleId = :roleId " +
            "AND rfr.permissionKey = :permissionKey " +
            "AND rfr.isActive = true")
    Set<RoleFilterRule.FilterType> findFilterTypesByRoleAndPermission(
            @Param("roleId") String roleId,
            @Param("permissionKey") String permissionKey);

    /**
     * Find filter rules by permission key (for admin/debug purposes)
     * 
     * @param permissionKey The permission key
     * @return List of all rules for the permission
     */
    List<RoleFilterRule> findByPermissionKeyAndIsActiveTrue(String permissionKey);

    /**
     * Find all rules for a specific role (active and inactive)
     * 
     * @param roleId The role ID
     * @return List of all rules for the role
     */
    List<RoleFilterRule> findByRoleIdOrderByPermissionKeyAscFilterTypeAsc(String roleId);
}
