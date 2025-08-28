package project.ktc.springboot_app.permission.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.CreateRoleRequest;
import project.ktc.springboot_app.permission.dto.RoleWithPermissionsDto;
import project.ktc.springboot_app.permission.entity.RolePermission;
import project.ktc.springboot_app.permission.interfaces.RoleService;
import project.ktc.springboot_app.permission.repositories.RolePermissionRepository;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;

/**
 * Implementation of RoleService for role management operations
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImp implements RoleService {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public UserRole createRole(CreateRoleRequest request) {
        log.info("Creating new role with name: {}", request.getName());

        // Validate role name uniqueness
        String roleName = request.getName().toUpperCase().trim();
        if (existsByRoleName(roleName)) {
            log.warn("Role creation failed - role name already exists: {}", roleName);
            throw new IllegalArgumentException("Role name '" + roleName + "' already exists");
        }

        try {
            // Convert string to RoleType enum
            String roleType = roleName;

            // Create new role entity
            UserRole newRole = UserRole.builder()
                    .role(roleType)
                    .build();

            // Save role to database
            UserRole savedRole = userRoleRepository.save(newRole);
            log.info("Role created successfully with ID: {} and name: {}", savedRole.getId(), savedRole.getRole());

            return savedRole;

        } catch (IllegalArgumentException e) {
            log.error("Invalid role name provided: {}. Valid roles are: STUDENT, INSTRUCTOR, ADMIN", roleName);
            throw new IllegalArgumentException(
                    "Invalid role name '" + roleName + "'. Valid roles are: STUDENT, INSTRUCTOR, ADMIN");
        } catch (DataIntegrityViolationException e) {
            log.error("Database constraint violation while creating role: {}", roleName, e);
            throw new IllegalArgumentException("Role name '" + roleName + "' already exists");
        } catch (Exception e) {
            log.error("Unexpected error while creating role: {}", roleName, e);
            throw new RuntimeException("Failed to create role due to internal server error");
        }
    }

    @Override
    public UserRole findById(String roleId) {
        log.debug("Finding role by ID: {}", roleId);
        return userRoleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Role not found with ID: {}", roleId);
                    return new RuntimeException("Role not found with ID: " + roleId);
                });
    }

    @Override
    public UserRole findByRoleType(String roleType) {
        log.debug("Finding role by type: {}", roleType);
        return userRoleRepository.findByRole(roleType).orElse(null);
    }

    @Override
    public boolean existsByRoleName(String roleName) {
        try {
            String roleType = roleName.toUpperCase().trim();
            boolean exists = userRoleRepository.findByRole(roleType).isPresent();
            log.debug("Role existence check for '{}': {}", roleName, exists);
            return exists;
        } catch (IllegalArgumentException e) {
            log.debug("Invalid role name for existence check: {}", roleName);
            return false;
        }
    }

    @Override
    public List<UserRole> getAllRoles() {
        log.debug("Retrieving all roles");
        List<UserRole> roles = userRoleRepository.findAll();
        log.debug("Found {} roles", roles.size());
        return roles;
    }

    @Override
    public Page<RoleWithPermissionsDto> getAllRolesWithPermissions(int page, int size) {
        log.info("Retrieving all roles with permissions - page: {}, size: {}", page, size);

        // Validate pagination parameters
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }

        // Create pageable
        Pageable pageable = PageRequest.of(page, size);

        try {
            // Get paginated roles
            Page<UserRole> rolesPage = userRoleRepository.findAll(pageable);
            log.debug("Found {} roles in page {} of {}", rolesPage.getContent().size(), page,
                    rolesPage.getTotalPages());

            // Convert to DTOs with permissions
            List<RoleWithPermissionsDto> roleWithPermissionsDtos = rolesPage.getContent().stream()
                    .map(this::convertToRoleWithPermissionsDto)
                    .collect(Collectors.toList());

            log.debug("Converted {} roles to DTOs with permissions", roleWithPermissionsDtos.size());

            // Return paginated result
            return new PageImpl<>(roleWithPermissionsDtos, pageable, rolesPage.getTotalElements());

        } catch (Exception e) {
            log.error("Error retrieving roles with permissions for page: {}, size: {}", page, size, e);
            throw new RuntimeException("Failed to retrieve roles with permissions", e);
        }
    }

    @Override
    @Transactional
    public UserRole updateRole(String roleId, project.ktc.springboot_app.permission.dto.UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        UserRole role = findById(roleId);

        // Validate new role name uniqueness if changed
        String newRoleName = request.getName().toUpperCase().trim();
        if (!role.getRole().equals(newRoleName) && existsByRoleName(newRoleName)) {
            log.warn("Role update failed - role name already exists: {}", newRoleName);
            throw new IllegalArgumentException("Role name '" + newRoleName + "' already exists");
        }

        role.setRole(newRoleName);
        return userRoleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        log.info("Deleting role with ID: {}", roleId);

        UserRole role = findById(roleId);

        // Check if role is being used (optional validation)
        // You might want to add checks here to prevent deletion of roles in use

        userRoleRepository.delete(role);
    }

    @Override
    @Transactional
    public void assignPermissions(String roleId,
            project.ktc.springboot_app.permission.dto.AssignPermissionsRequest request) {
        log.info("Assigning {} permissions to role ID: {}", request.getPermissionIds().size(), roleId);

        // Validate role exists
        findById(roleId);

        // Implementation depends on your permission assignment logic
        // This is a placeholder - you'll need to implement based on your permission
        // system
        throw new UnsupportedOperationException("Permission assignment not implemented yet");
    }

    @Override
    @Transactional
    public void removePermissions(String roleId,
            project.ktc.springboot_app.permission.dto.RemovePermissionsRequest request) {
        log.info("Removing {} permissions from role ID: {}", request.getPermissionIds().size(), roleId);

        // Validate role exists
        findById(roleId);

        // Implementation depends on your permission removal logic
        // This is a placeholder - you'll need to implement based on your permission
        // system
        throw new UnsupportedOperationException("Permission removal not implemented yet");
    }

    @Override
    public List<project.ktc.springboot_app.permission.dto.RolePermissionDetailDto> getRolePermissions(String roleId) {
        log.info("Getting permissions for role ID: {}", roleId);

        // Validate role exists
        findById(roleId);
        List<RolePermission> rolePermissions = rolePermissionRepository.findActiveByRoleId(roleId);

        return rolePermissions.stream()
                .map(rp -> project.ktc.springboot_app.permission.dto.RolePermissionDetailDto.builder()
                        .permissionKey(rp.getPermission().getPermissionKey())
                        .description(rp.getPermission().getDescription())
                        .resource(rp.getPermission().getResourceName())
                        .action(rp.getPermission().getActionName())
                        .filterType(rp.getFilterType().getName())
                        .isAssigned(true)
                        .canAssignToRole(true)
                        .isRestricted(false)
                        .allowedRoles(List.of())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public RoleWithPermissionsDto convertToRoleWithPermissionsDto(UserRole role) {
        return convertToRoleWithPermissionsDtoInternal(role);
    }

    /**
     * Convert UserRole entity to RoleWithPermissionsDto
     */
    private RoleWithPermissionsDto convertToRoleWithPermissionsDtoInternal(UserRole role) {
        log.debug("Converting role {} to DTO with permissions", role.getId());

        try {
            // Get all permissions for this role
            List<RolePermission> rolePermissions = rolePermissionRepository.findActiveByRoleId(role.getId());
            log.debug("Found {} active permissions for role {}", rolePermissions.size(), role.getId());

            // Convert permissions to summary DTOs
            List<RoleWithPermissionsDto.PermissionSummaryDto> permissionSummaries = rolePermissions.stream()
                    .map(rp -> RoleWithPermissionsDto.PermissionSummaryDto.builder()
                            .id(rp.getPermission().getId())
                            .name(rp.getPermission().getPermissionKey())
                            .filterType(rp.getFilterType().getName())
                            .build())
                    .collect(Collectors.toList());

            // Build role with permissions DTO
            return RoleWithPermissionsDto.builder()
                    .id(role.getId())
                    .name(role.getRole())
                    .totalPermission(permissionSummaries.size())
                    .permissions(permissionSummaries)
                    .build();

        } catch (Exception e) {
            log.error("Error converting role {} to DTO with permissions", role.getId(), e);

            // Return role with empty permissions in case of error
            return RoleWithPermissionsDto.builder()
                    .id(role.getId())
                    .name(role.getRole())
                    .totalPermission(0)
                    .permissions(List.of())
                    .build();
        }
    }
}
