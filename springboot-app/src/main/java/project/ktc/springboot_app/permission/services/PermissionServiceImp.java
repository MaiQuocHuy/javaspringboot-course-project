package project.ktc.springboot_app.permission.services;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.entity.Permission;
import project.ktc.springboot_app.entity.RolePermission;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.interfaces.PermissionService;
import project.ktc.springboot_app.permission.repositories.PermissionRepository;
import project.ktc.springboot_app.permission.repositories.RolePermissionRepository;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;

/**
 * Implementation of PermissionService without caching
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PermissionServiceImp implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImp.class);

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Set<String> loadUserPermissions(User user) {
        logger.debug("Loading permissions for user: {}", user.getEmail());

        UserRole.RoleType userRole = user.getRole().getRole();
        Set<String> permissions = rolePermissionRepository
                .findPermissionKeysByRoleType(userRole);

        logger.debug("Loaded {} permissions for user: {}", permissions.size(), user.getEmail());
        return permissions;
    }

    @Override
    public Set<String> loadUserPermissions(String userId, UserRole.RoleType roleType) {
        logger.debug("Loading permissions for user ID: {} with role: {}", userId, roleType);

        Set<String> permissions = rolePermissionRepository
                .findPermissionKeysByRoleType(roleType);

        logger.debug("Loaded {} permissions for user ID: {}", permissions.size(), userId);
        return permissions;
    }

    @Override
    public boolean hasPermission(User user, String permissionKey) {
        if (user == null || permissionKey == null) {
            return false;
        }

        Set<String> userPermissions = loadUserPermissions(user);
        boolean hasPermission = userPermissions.contains(permissionKey);

        logger.debug("Permission check - User: {}, Permission: {}, Result: {}",
                user.getEmail(), permissionKey, hasPermission);

        return hasPermission;
    }

    @Override
    public boolean hasPermission(String userId, String permissionKey) {
        if (userId == null || permissionKey == null) {
            return false;
        }

        logger.debug("Permission check by user ID: {}, Permission: {}", userId, permissionKey);
        // Note: This method requires role information which we don't have here
        // In a real implementation, you'd need to fetch the user's role first
        logger.warn("hasPermission(userId, permissionKey) called but requires user role lookup");
        return false;
    }

    @Override
    public boolean hasPermission(User user, String resource, String action) {
        String permissionKey = resource + ":" + action;
        return hasPermission(user, permissionKey);
    }

    @Override
    public Set<String> getPermissionsForRole(UserRole.RoleType roleType) {
        return rolePermissionRepository.findPermissionKeysByRoleType(roleType);
    }

    @Override
    public boolean isValidPermission(String resource, String action) {
        if (resource == null || action == null) {
            return false;
        }

        String permissionKey = resource + ":" + action;
        return permissionRepository.existsByPermissionKeyAndActive(permissionKey);
    }

    @Override
    public Set<String> getRolePermissions(UserRole.RoleType roleType) {
        return rolePermissionRepository.findPermissionKeysByRoleType(roleType);
    }

    @Override
    public boolean hasRolePermission(UserRole.RoleType roleType, String permissionKey) {
        Set<String> rolePermissions = getRolePermissions(roleType);
        return rolePermissions.contains(permissionKey);
    }

    @Override
    public List<Permission> getAllActivePermissions() {
        return permissionRepository.findAllActivePermissions();
    }

    @Override
    public Permission findByPermissionKey(String permissionKey) {
        return permissionRepository.findByPermissionKey(permissionKey).orElse(null);
    }

    @Override
    public boolean permissionExists(String permissionKey) {
        return permissionRepository.existsByPermissionKeyAndActive(permissionKey);
    }

    @Override
    public List<Permission> getPermissionsByResource(String resourceName) {
        return permissionRepository.findByResourceName(resourceName);
    }

    @Override
    public List<Permission> getAllPermissionsWithDetails() {
        logger.debug("Fetching all permissions with resource and action details");
        List<Permission> permissions = permissionRepository.findAllPermissionsWithDetails();
        logger.debug("Found {} total permissions", permissions.size());
        return permissions;
    }

    @Override
    public List<RolePermission> getPermissionsByRole(String roleId) {
        // Find the role by ID
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Get all permissions assigned to this role
        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRole(role);
        logger.debug("Found {} permissions assigned to role: {}", rolePermissions.size(), role.getRole());

        return rolePermissions;
    }
}
