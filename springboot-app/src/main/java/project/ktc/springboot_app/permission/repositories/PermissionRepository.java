package project.ktc.springboot_app.permission.repositories;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import project.ktc.springboot_app.permission.entity.Permission;

/**
 * Repository interface for Permission entity
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

        /**
         * Find permission by resource name and action name
         */
        @Query("SELECT p FROM Permission p " +
                        "JOIN p.resource r " +
                        "JOIN p.action a " +
                        "WHERE r.name = :resourceName " +
                        "AND a.name = :actionName " +
                        "AND p.isActive = true " +
                        "AND r.isActive = true " +
                        "AND a.isActive = true")
        Optional<Permission> findByResourceNameAndActionName(@Param("resourceName") String resourceName,
                        @Param("actionName") String actionName);

        /**
         * Find permission by permission key
         */
        @Query("SELECT p FROM Permission p " +
                        "WHERE p.permissionKey = :permissionKey " +
                        "AND p.isActive = true")
        Optional<Permission> findByPermissionKey(@Param("permissionKey") String permissionKey);

        /**
         * Find all active permissions
         */
        @Query("SELECT p FROM Permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "WHERE p.isActive = true " +
                        "AND r.isActive = true " +
                        "AND a.isActive = true")
        List<Permission> findAllActivePermissions();

        /**
         * Find all permissions for a specific role
         */
        @Query("SELECT DISTINCT p FROM Permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "JOIN RolePermission rp ON rp.permission = p " +
                        "JOIN rp.role role " +
                        "WHERE role.role = :roleType " +
                        "AND p.isActive = true " +
                        "AND r.isActive = true " +
                        "AND a.isActive = true " +
                        "AND rp.isActive = true")
        List<Permission> findPermissionsByRoleType(
                        @Param("roleType") String roleType);

        /**
         * Find permission keys for a specific role
         */
        @Query("SELECT p.permissionKey FROM Permission p " +
                        "JOIN RolePermission rp ON rp.permission = p " +
                        "JOIN rp.role role " +
                        "WHERE role.role = :roleType " +
                        "AND p.isActive = true " +
                        "AND rp.isActive = true")
        Set<String> findPermissionKeysByRoleType(
                        @Param("roleType") String roleType);

        /**
         * Check if a permission exists and is active
         */
        @Query("SELECT COUNT(p) > 0 FROM Permission p " +
                        "JOIN p.resource r " +
                        "JOIN p.action a " +
                        "WHERE p.permissionKey = :permissionKey " +
                        "AND p.isActive = true " +
                        "AND r.isActive = true " +
                        "AND a.isActive = true")
        boolean existsByPermissionKeyAndActive(@Param("permissionKey") String permissionKey);

        /**
         * Find permissions by resource name
         */
        @Query("SELECT p FROM Permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "WHERE r.name = :resourceName " +
                        "AND p.isActive = true " +
                        "AND r.isActive = true " +
                        "AND a.isActive = true")
        List<Permission> findByResourceName(@Param("resourceName") String resourceName);

        /**
         * Find all permissions with their resource and action details (for admin
         * purposes)
         * This includes both active and inactive permissions
         */
        @Query("SELECT p FROM Permission p " +
                        "JOIN FETCH p.resource r " +
                        "JOIN FETCH p.action a " +
                        "ORDER BY r.name, a.name")
        List<Permission> findAllPermissionsWithDetails();
}
