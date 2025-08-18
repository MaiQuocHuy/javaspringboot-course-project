package project.ktc.springboot_app.permission.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.PermissionUpdateRequest;
import project.ktc.springboot_app.permission.dto.ResourceDto;
import project.ktc.springboot_app.permission.entity.Permission;
import project.ktc.springboot_app.permission.entity.Resource;
import project.ktc.springboot_app.permission.entity.RolePermission;
import project.ktc.springboot_app.permission.interfaces.PermissionService;
import project.ktc.springboot_app.permission.repositories.PermissionRepository;
import project.ktc.springboot_app.permission.repositories.ResourceRepository;
import project.ktc.springboot_app.permission.repositories.RolePermissionRepository;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;

/**
 * Implementation of PermissionService without caching
 */
@Service
@Transactional(readOnly = true)
public class PermissionServiceImp implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImp.class);

    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final ResourceRepository resourceRepository;

    public PermissionServiceImp(
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            UserRoleRepository userRoleRepository,
            ResourceRepository resourceRepository) {
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Set<String> loadUserPermissions(User user) {
        logger.debug("Loading permissions for user: {}", user.getEmail());

        String userRole = user.getRole().getRole();
        Set<String> permissions = rolePermissionRepository
                .findPermissionKeysByRoleType(userRole);

        logger.debug("Loaded {} permissions for user: {}", permissions.size(), user.getEmail());
        return permissions;
    }

    @Override
    public Set<String> loadUserPermissions(String userId, String roleType) {
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
        if (user == null || resource == null || action == null) {
            return false;
        }

        String permissionKey = resource + ":" + action;
        return hasPermission(user, permissionKey);
    }

    @Override
    public Set<String> getPermissionsForRole(String roleType) {
        logger.debug("Getting permissions for role: {}", roleType);

        Set<String> permissions = rolePermissionRepository.findPermissionKeysByRoleType(roleType);

        logger.debug("Found {} permissions for role: {}", permissions.size(), roleType);
        return permissions;
    }

    @Override
    public boolean isValidPermission(String resource, String action) {
        if (resource == null || action == null) {
            return false;
        }

        String permissionKey = resource + ":" + action;
        Optional<Permission> permission = permissionRepository.findByPermissionKey(permissionKey);

        boolean isValid = permission.isPresent() && permission.get().isActive();
        logger.debug("Permission validation - Key: {}, Valid: {}", permissionKey, isValid);

        return isValid;
    }

    @Override
    public List<Permission> getAllActivePermissions() {
        logger.debug("Fetching all active permissions");

        List<Permission> permissions = permissionRepository.findAllActivePermissions();

        logger.debug("Found {} active permissions", permissions.size());
        return permissions;
    }

    @Override
    public Permission findByPermissionKey(String permissionKey) {
        logger.debug("Finding permission by key: {}", permissionKey);

        Optional<Permission> permission = permissionRepository.findByPermissionKey(permissionKey);

        if (permission.isPresent()) {
            logger.debug("Found permission: {}", permission.get().getPermissionKey());
            return permission.get();
        } else {
            logger.debug("Permission not found with key: {}", permissionKey);
            return null;
        }
    }

    @Override
    public boolean permissionExists(String permissionKey) {
        return findByPermissionKey(permissionKey) != null;
    }

    @Override
    public List<Permission> getPermissionsByResource(String resourceName) {
        logger.debug("Getting permissions for resource: {}", resourceName);

        List<Permission> permissions = permissionRepository.findByResourceName(resourceName);

        logger.debug("Found {} permissions for resource: {}", permissions.size(), resourceName);
        return permissions;
    }

    @Override
    public Set<String> getRolePermissions(String roleType) {
        return getPermissionsForRole(roleType);
    }

    @Override
    public boolean hasRolePermission(String roleType, String permissionKey) {
        logger.debug("Checking if role {} has permission: {}", roleType, permissionKey);

        boolean hasPermission = rolePermissionRepository.hasPermission(roleType, permissionKey);

        logger.debug("Role permission check - Role: {}, Permission: {}, Result: {}",
                roleType, permissionKey, hasPermission);

        return hasPermission;
    }

    @Override
    public List<Permission> getAllPermissionsWithDetails() {
        logger.debug("Fetching all permissions with details for admin purposes");

        List<Permission> permissions = permissionRepository.findAllPermissionsWithDetails();

        logger.debug("Found {} permissions with details", permissions.size());
        return permissions;
    }

    @Override
    public List<RolePermission> getPermissionsByRole(String roleId) {
        logger.debug("Fetching permissions for role ID: {}", roleId);

        // Validate role exists
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRole(role);
        logger.debug("Found {} permissions for role ID: {}", rolePermissions.size(), roleId);

        return rolePermissions;
    }

    @Override
    @Transactional
    public List<RolePermission> updatePermissionsForRole(String roleId, PermissionUpdateRequest request) {
        logger.debug("Updating permissions for role ID: {} with {} changes",
                roleId, request.getPermissions().size());

        // Validate role exists
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Process each permission update
        for (PermissionUpdateRequest.PermissionUpdateItemDto permissionUpdate : request.getPermissions()) {
            String permissionKey = permissionUpdate.getKey();
            Boolean isActive = permissionUpdate.getIsActive();

            logger.debug("Processing permission update: {} -> {}", permissionKey, isActive);

            // Validate permission exists and is active at system level
            Optional<Permission> permissionOpt = permissionRepository.findByPermissionKey(permissionKey);
            if (!permissionOpt.isPresent()) {
                throw new RuntimeException("Permission not found with key: " + permissionKey);
            }

            Permission permission = permissionOpt.get();
            if (!permission.isActive()) {
                throw new RuntimeException("Permission " + permissionKey + " is disabled at system level");
            }

            // Check if role-permission combination already exists
            java.util.Optional<RolePermission> existingRolePermission = rolePermissionRepository
                    .findByRoleAndPermissionKey(role, permissionKey);

            if (existingRolePermission.isPresent()) {
                // Update existing role-permission
                RolePermission rolePermission = existingRolePermission.get();
                rolePermission.setIsActive(isActive);
                rolePermissionRepository.save(rolePermission);
                logger.debug("Updated existing role-permission: {} -> {}", permissionKey, isActive);
            } else {
                // Create new role-permission if it doesn't exist and is being activated
                if (isActive) {
                    RolePermission newRolePermission = RolePermission.builder()
                            .role(role)
                            .permission(permission)
                            .isActive(true)
                            .build();
                    rolePermissionRepository.save(newRolePermission);
                    logger.debug("Created new role-permission: {} -> {}", permissionKey, isActive);
                } else {
                    logger.debug("Skipping creation of inactive role-permission: {}", permissionKey);
                }
            }
        }

        // Return updated list of all permissions for the role
        List<RolePermission> updatedPermissions = rolePermissionRepository.findAllByRole(role);
        logger.debug("Permission update completed for role: {}. Total permissions: {}",
                role.getRole(), updatedPermissions.size());

        return updatedPermissions;
    }

    @Override
    public List<ResourceDto> getResourceTreeForRole(String roleId) {
        logger.debug("Building resource tree for role ID: {}", roleId);

        // 1. Validate role exists
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // 2. Fetch all active resources with their parent relationships
        List<Resource> allResources = resourceRepository.findAllWithParents();
        logger.debug("Found {} active resources", allResources.size());

        // 3. Get all permissions assigned to this role
        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRole(role);
        Set<String> assignedResourceNames = rolePermissions.stream()
                .filter(rp -> rp.isActive() && rp.getPermission().isActive())
                .map(rp -> rp.getPermission().getResource().getName())
                .collect(Collectors.toSet());
        logger.debug("Found {} directly assigned resource permissions for role", assignedResourceNames.size());

        // 4. Build resource hierarchy map
        Map<String, List<Resource>> parentChildMap = new HashMap<>();
        List<Resource> rootResources = new ArrayList<>();

        for (Resource resource : allResources) {
            if (resource.getParentResource() == null) {
                rootResources.add(resource);
            } else {
                String parentId = resource.getParentResource().getId();
                parentChildMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(resource);
            }
        }

        // 5. Build ResourceDto tree with permission calculations
        List<ResourceDto> resourceTree = rootResources.stream()
                .map(resource -> buildResourceDto(resource, parentChildMap, assignedResourceNames, new HashSet<>()))
                .collect(Collectors.toList());

        logger.debug("Built resource tree with {} root resources for role ID: {}", resourceTree.size(), roleId);
        return resourceTree;
    }

    /**
     * Recursively build ResourceDto with permission inheritance calculation
     */
    private ResourceDto buildResourceDto(Resource resource, Map<String, List<Resource>> parentChildMap,
            Set<String> assignedResourceNames, Set<String> inheritedResourceNames) {

        // Check if this resource has directly assigned permissions
        boolean assigned = assignedResourceNames.contains(resource.getName());

        // Check if this resource inherits permissions from parent
        boolean inherited = inheritedResourceNames.contains(resource.getName());

        // If this resource has assigned permissions, its children will inherit
        Set<String> childInheritedNames = new HashSet<>(inheritedResourceNames);
        if (assigned) {
            childInheritedNames.add(resource.getName());
        }

        // Build child resources recursively
        List<ResourceDto> children = parentChildMap.getOrDefault(resource.getId(), List.of())
                .stream()
                .map(child -> buildResourceDto(child, parentChildMap, assignedResourceNames, childInheritedNames))
                .collect(Collectors.toList());

        // Create parent resource info if exists
        ResourceDto.ParentResourceDto parent = null;
        if (resource.getParentResource() != null) {
            parent = ResourceDto.ParentResourceDto.builder()
                    .id(resource.getParentResource().getId())
                    .key(resource.getParentResource().getName())
                    .name(resource.getParentResource().getName())
                    .build();
        }

        return ResourceDto.builder()
                .id(resource.getId())
                .key(resource.getName())
                .name(resource.getName())
                .description(resource.getDescription())
                .resourcePath(resource.getResourcePath())
                .assigned(assigned)
                .inherited(inherited)
                .active(resource.isActive())
                .children(children)
                .parent(parent)
                .build();
    }
}
