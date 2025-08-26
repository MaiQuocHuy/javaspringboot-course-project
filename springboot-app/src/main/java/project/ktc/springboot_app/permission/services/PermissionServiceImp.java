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
import project.ktc.springboot_app.permission.dto.AvailablePermissionDto;
import project.ktc.springboot_app.permission.dto.PermissionUpdateRequest;
import project.ktc.springboot_app.permission.dto.ResourceDto;
import project.ktc.springboot_app.permission.entity.FilterType;
import project.ktc.springboot_app.permission.entity.Permission;
import project.ktc.springboot_app.permission.entity.Resource;
import project.ktc.springboot_app.permission.entity.RolePermission;
import project.ktc.springboot_app.permission.interfaces.PermissionService;
import project.ktc.springboot_app.permission.interfaces.PermissionRoleAssignRuleService;
import project.ktc.springboot_app.permission.repositories.PermissionRoleAssignRuleRepository;
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
    private final PermissionRoleAssignRuleRepository permissionRoleAssignRuleRepository;
    private final PermissionRoleAssignRuleService permissionRoleAssignRuleService;
    private final project.ktc.springboot_app.permission.repositories.FilterTypeRepository filterTypeRepository;

    public PermissionServiceImp(
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            UserRoleRepository userRoleRepository,
            ResourceRepository resourceRepository,
            PermissionRoleAssignRuleRepository permissionRoleAssignRuleRepository,
            PermissionRoleAssignRuleService permissionRoleAssignRuleService,
            project.ktc.springboot_app.permission.repositories.FilterTypeRepository filterTypeRepository) {
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.resourceRepository = resourceRepository;
        this.permissionRoleAssignRuleRepository = permissionRoleAssignRuleRepository;
        this.permissionRoleAssignRuleService = permissionRoleAssignRuleService;
        this.filterTypeRepository = filterTypeRepository;
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
        logger.debug("[updatePermissionsForRole] Incoming {} permission items for role {}",
                request.getPermissions().size(), roleId);

        // 1. Validate role exists
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // 2. Build current permissions map (permissionKey -> RolePermission)
        List<RolePermission> currentRolePermissions = rolePermissionRepository.findAllByRole(role);
        Map<String, RolePermission> currentMap = currentRolePermissions.stream()
                .collect(Collectors.toMap(rp -> rp.getPermission().getPermissionKey(), rp -> rp));

        // 3. Prepare request keys set (for deactivation logic)
        Set<String> requestedKeys = request.getPermissions().stream()
                .map(PermissionUpdateRequest.PermissionUpdateItemDto::getKey)
                .collect(Collectors.toSet());

        // 4. Process each requested permission (activate / create + set filterType)
        for (PermissionUpdateRequest.PermissionUpdateItemDto item : request.getPermissions()) {
            String permissionKey = item.getKey();
            String filterTypeId = item.getFilterType();

            logger.debug("Processing permission '{}' with filterType '{}'", permissionKey, filterTypeId);

            Permission permission = permissionRepository.findByPermissionKey(permissionKey)
                    .orElseThrow(() -> new RuntimeException("Permission not found with key: " + permissionKey));
            if (!permission.isActive()) {
                throw new RuntimeException("Permission " + permissionKey + " is disabled at system level");
            }

            // Default filterType to ALL when not provided
            FilterType filterType;
            if (filterTypeId == null || filterTypeId.trim().isEmpty()) {
                filterType = filterTypeRepository.getAllFilterType()
                        .orElseThrow(() -> new RuntimeException("FilterType not found with ID: filter-type-001"));
            } else {
                filterType = filterTypeRepository.findById(filterTypeId)
                        .orElseThrow(() -> new RuntimeException("FilterType not found with ID: " + filterTypeId));
            }

            RolePermission rolePermission = currentMap.get(permissionKey);
            if (rolePermission != null) {
                // Reactivate & update filterType
                rolePermission.setIsActive(true);
                rolePermission.setFilterType(filterType);
                rolePermissionRepository.save(rolePermission);
                logger.debug("Updated existing role-permission '{}' (active=true, filterType={})", permissionKey,
                        filterType.getName());
            } else {
                // Create new active mapping
                rolePermission = RolePermission.builder()
                        .role(role)
                        .permission(permission)
                        .filterType(filterType)
                        .isActive(true)
                        .build();
                rolePermissionRepository.save(rolePermission);
                logger.debug("Created new role-permission '{}' (filterType={})", permissionKey, filterType.getName());
            }
        }

        // 5. Deactivate any existing role-permission not present in request (full
        // replacement semantics)
        int deactivatedCount = 0;
        for (RolePermission existing : currentRolePermissions) {
            String existingKey = existing.getPermission().getPermissionKey();
            if (!requestedKeys.contains(existingKey) && Boolean.TRUE.equals(existing.getIsActive())) {
                existing.setIsActive(false);
                rolePermissionRepository.save(existing);
                deactivatedCount++;
                logger.debug("Deactivated role-permission '{}' for role {}", existingKey, role.getRole());
            }
        }

        logger.debug("Permission update completed for role {}. Activated/updated: {}, Deactivated: {}", roleId,
                requestedKeys.size(), deactivatedCount);

        return rolePermissionRepository.findAllByRole(role);
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

    @Override
    public List<AvailablePermissionDto> getAllAvailablePermissions(String roleId) {
        logger.debug("Getting all available permissions for role ID: {}", roleId);

        // Validate role exists
        userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Get all permissions from the database
        List<Permission> allPermissions = permissionRepository.findAllPermissionsWithDetails();
        logger.debug("Found {} permissions in the system", allPermissions.size());

        // Convert to AvailablePermissionDto list with assignment logic
        List<AvailablePermissionDto> availablePermissions = allPermissions.stream()
                .map(permission -> {
                    // Check if this permission has any assignment rules (restricted)
                    boolean isRestricted = permissionRoleAssignRuleRepository.hasAssignmentRules(permission.getId());

                    // Get allowed roles for this permission
                    List<String> allowedRoles = permissionRoleAssignRuleRepository
                            .findAllowedRoleNamesByPermissionId(permission.getId());

                    // Determine if this role can assign this permission
                    boolean canAssignToRole;
                    if (!isRestricted) {
                        // No rules → assignable by all roles
                        canAssignToRole = true;
                    } else {
                        // Has rules → only listed roles with is_active = true can assign
                        canAssignToRole = allowedRoles.contains(
                                userRoleRepository.findById(roleId).get().getRole());
                    }

                    return AvailablePermissionDto.builder()
                            .id(permission.getId())
                            .permissionKey(permission.getPermissionKey())
                            .description(permission.getDescription())
                            .resource(permission.getResourceName())
                            .action(permission.getActionName())
                            .canAssignToRole(canAssignToRole)
                            .isRestricted(isRestricted)
                            .allowedRoles(allowedRoles)
                            .build();
                })
                .collect(Collectors.toList());

        logger.debug(
                "Converted {} permissions to AvailablePermissionDto with assignment flags, restrictions, and allowed roles",
                availablePermissions.size());

        return availablePermissions;
    }

    @Override
    public project.ktc.springboot_app.permission.dto.RolePermissionGroupedDto getPermissionsGroupedByResourceForRole(
            String roleId) {
        logger.debug("Getting permissions grouped by resource for role ID: {}", roleId);

        // Validate role exists
        UserRole role = userRoleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Define restricted resources that new roles cannot have permissions for
        Set<String> restrictedResources = Set.of("enrollment", "refresh_token", "permission", "system_log",
                "video_content", "lesson_completion");

        // Get all permissions from the database
        List<Permission> allPermissions = permissionRepository.findAllPermissionsWithDetails();
        logger.debug("Found {} permissions in the system", allPermissions.size());

        // Get current role permissions to check what's assigned and get filter types
        List<RolePermission> currentRolePermissions = rolePermissionRepository.findAllByRole(role);
        Set<String> assignedPermissionKeys = currentRolePermissions.stream()
                .filter(rp -> rp.getIsActive())
                .map(rp -> rp.getPermission().getPermissionKey())
                .collect(Collectors.toSet());

        // Create map of permission key to filter type for assigned permissions
        Map<String, String> permissionFilterTypeMap = currentRolePermissions.stream()
                .filter(rp -> rp.getIsActive() && rp.getFilterType() != null)
                .collect(Collectors.toMap(
                        rp -> rp.getPermission().getPermissionKey(),
                        rp -> rp.getFilterType().getName(),
                        (existing, replacement) -> existing // handle duplicate keys by keeping existing
                ));

        logger.debug("Role {} has {} assigned permissions", roleId, assignedPermissionKeys.size());

        // Convert to RolePermissionDetailDto and group by resource
        Map<String, List<project.ktc.springboot_app.permission.dto.RolePermissionDetailDto>> resourceGroups = allPermissions
                .stream()
                .filter(permission -> !restrictedResources.contains(permission.getResourceName())) // Filter out
                                                                                                   // restricted
                                                                                                   // resources
                .map(permission -> {
                    // Check if this permission has any assignment rules (restricted)
                    boolean isRestricted = permissionRoleAssignRuleRepository.hasAssignmentRules(permission.getId());

                    // Get allowed roles for this permission
                    List<String> allowedRoles = permissionRoleAssignRuleRepository
                            .findAllowedRoleNamesByPermissionId(permission.getId());

                    // Determine if this role can assign this permission
                    boolean canAssignToRole;
                    if (!isRestricted) {
                        // No rules → assignable by all roles
                        canAssignToRole = true;
                    } else {
                        // Has rules → only listed roles with is_active = true can assign
                        canAssignToRole = allowedRoles.contains(role.getRole());
                    }

                    // Check if currently assigned
                    boolean isAssigned = assignedPermissionKeys.contains(permission.getPermissionKey());

                    // Get current filter type for this permission
                    String currentFilterType = permissionFilterTypeMap.get(permission.getPermissionKey());

                    return new project.ktc.springboot_app.permission.dto.RolePermissionDetailDto(
                            permission.getPermissionKey(),
                            permission.getDescription(),
                            permission.getResourceName(),
                            permission.getActionName(),
                            currentFilterType,
                            isAssigned,
                            canAssignToRole,
                            isRestricted,
                            allowedRoles);
                })
                .collect(Collectors
                        .groupingBy(project.ktc.springboot_app.permission.dto.RolePermissionDetailDto::getResource));

        logger.debug("Grouped permissions into {} resource categories (filtered out restricted resources)",
                resourceGroups.size());

        return new project.ktc.springboot_app.permission.dto.RolePermissionGroupedDto(resourceGroups);
    }
}
