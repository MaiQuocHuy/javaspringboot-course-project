package project.ktc.springboot_app.permission.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.entity.RolePermission;

/**
 * Repository interface for RolePermission entity
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {

        /**
         * Find all active role permissions for a specific role
         */
        @Query("SELECT rp FROM RolePermission rp " +
                        "JOIN FETCH rp.permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "WHERE rp.role = :role " +
                        "AND rp.isActive = true " +
                        "AND p.isActive = true " +
                        "AND r.isActive = true " +
                        "AND a.isActive = true")
        List<RolePermission> findActiveByRole(@Param("role") UserRole role);

        /**
         * Find all permission keys for a specific role type
         */
        @Query("SELECT p.permissionKey FROM RolePermission rp " +
                        "JOIN rp.permission p " +
                        "JOIN rp.role role " +
                        "WHERE role.role = :roleType " +
                        "AND rp.isActive = true " +
                        "AND p.isActive = true")
        Set<String> findPermissionKeysByRoleType(@Param("roleType") String roleType);

        /**
         * Find all role permissions for multiple roles
         */
        @Query("SELECT rp FROM RolePermission rp " +
                        "JOIN FETCH rp.permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "WHERE rp.role IN :roles " +
                        "AND rp.isActive = true " +
                        "AND p.isActive = true " +
                        "AND r.isActive = true " +
                        "AND a.isActive = true")
        List<RolePermission> findActiveByRoles(@Param("roles") List<UserRole> roles);

        /**
         * Check if a role has a specific permission
         */
        @Query("SELECT COUNT(rp) > 0 FROM RolePermission rp " +
                        "JOIN rp.permission p " +
                        "JOIN rp.role role " +
                        "WHERE role.role = :roleType " +
                        "AND p.permissionKey = :permissionKey " +
                        "AND rp.isActive = true " +
                        "AND p.isActive = true")
        boolean hasPermission(@Param("roleType") String roleType,
                        @Param("permissionKey") String permissionKey);

        /**
         * Find permission keys by multiple role types
         */
        @Query("SELECT DISTINCT p.permissionKey FROM RolePermission rp " +
                        "JOIN rp.permission p " +
                        "JOIN rp.role role " +
                        "WHERE role.role IN :roleTypes " +
                        "AND rp.isActive = true " +
                        "AND p.isActive = true")
        Set<String> findPermissionKeysByRoleTypes(@Param("roleTypes") Set<String> roleTypes);

        /**
         * Count active permissions for a role
         */
        @Query("SELECT COUNT(rp) FROM RolePermission rp " +
                        "WHERE rp.role = :role " +
                        "AND rp.isActive = true")
        long countActiveByRole(@Param("role") UserRole role);

        /**
         * Find all permissions assigned to a specific role (both active and inactive)
         * Used for admin API to get all permissions for a role
         */
        @Query("SELECT rp FROM RolePermission rp " +
                        "JOIN FETCH rp.permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "WHERE rp.role = :role " +
                        "ORDER BY r.name, a.name")
        List<RolePermission> findAllByRole(@Param("role") UserRole role);

        /**
         * Find a specific role-permission combination by role and permission key
         */
        @Query("SELECT rp FROM RolePermission rp " +
                        "JOIN FETCH rp.permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "WHERE rp.role = :role " +
                        "AND p.permissionKey = :permissionKey")
        java.util.Optional<RolePermission> findByRoleAndPermissionKey(@Param("role") UserRole role,
                        @Param("permissionKey") String permissionKey);

        /**
         * Find active role permissions by role ID and permission key (for new schema)
         */
        @Query("SELECT rp FROM RolePermission rp " +
                        "JOIN FETCH rp.permission p " +
                        "JOIN FETCH rp.filterType ft " +
                        "WHERE rp.role.id = :roleId " +
                        "AND p.permissionKey = :permissionKey " +
                        "AND rp.isActive = true " +
                        "AND p.isActive = true ")
        List<RolePermission> findActiveByRoleAndPermission(@Param("roleId") String roleId,
                        @Param("permissionKey") String permissionKey);

        /**
         * Find all active role permissions by role ID (for new schema)
         */
        @Query("SELECT rp FROM RolePermission rp " +
                        "JOIN FETCH rp.permission p " +
                        "JOIN FETCH rp.filterType ft " +
                        "WHERE rp.role.id = :roleId " +
                        "AND rp.isActive = true " +
                        "AND p.isActive = true ")
        List<RolePermission> findActiveByRoleId(@Param("roleId") String roleId);
}
