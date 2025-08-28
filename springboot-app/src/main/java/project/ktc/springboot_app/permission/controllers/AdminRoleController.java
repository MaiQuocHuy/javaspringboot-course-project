package project.ktc.springboot_app.permission.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.*;
import project.ktc.springboot_app.permission.interfaces.RoleService;
import project.ktc.springboot_app.permission.interfaces.PermissionService;
import project.ktc.springboot_app.common.dto.ApiResponse;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Admin Role Controller for role management operations
 * Provides API endpoints for CRUD operations on roles and role-permission
 * assignments
 */
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    /**
     * Get all roles with pagination and their associated permissions
     */
    @GetMapping
    @PreAuthorize("hasPermission('Role', 'role:READ')")
    public ResponseEntity<ApiResponse<Page<RoleWithPermissionsDto>>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all roles with pagination - page: {}, size: {}", page, size);

        Page<RoleWithPermissionsDto> roles = roleService.getAllRolesWithPermissions(page, size);
        return ResponseEntity.ok(ApiResponse.success(roles, "Roles retrieved successfully"));
    }

    /**
     * Get all roles without pagination (for dropdowns)
     */
    @GetMapping("/list")
    @PreAuthorize("hasPermission('Role', 'role:READ')")
    public ResponseEntity<ApiResponse<List<RoleResponseDto>>> getAllRolesList() {
        log.info("Fetching all roles list");

        List<UserRole> roles = roleService.getAllRoles();
        List<RoleResponseDto> roleResponses = roles.stream()
                .map(role -> RoleResponseDto.builder()
                        .id(role.getId())
                        .name(role.getRole())
                        .build())
                .toList();

        return ResponseEntity.ok(ApiResponse.success(roleResponses, "Roles list retrieved successfully"));
    }

    /**
     * Get role by ID with its permissions
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("hasPermission('Role', 'role:READ')")
    public ResponseEntity<ApiResponse<RoleWithPermissionsDto>> getRoleById(@PathVariable String roleId) {
        log.info("Fetching role by ID: {}", roleId);

        UserRole role = roleService.findById(roleId);
        RoleWithPermissionsDto roleDto = roleService.convertToRoleWithPermissionsDto(role);

        return ResponseEntity.ok(ApiResponse.success(roleDto, "Role retrieved successfully"));
    }

    /**
     * Create a new role
     */
    @PostMapping
    @PreAuthorize("hasPermission('Role', 'role:CREATE')")
    public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        log.info("Creating new role with name: {}", request.getName());

        UserRole createdRole = roleService.createRole(request);
        RoleResponseDto response = RoleResponseDto.builder()
                .id(createdRole.getId())
                .name(createdRole.getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Role created successfully"));
    }

    /**
     * Update a role
     */
    @PutMapping("/{roleId}")
    @PreAuthorize("hasPermission('Role', 'role:UPDATE')")
    public ResponseEntity<ApiResponse<RoleResponseDto>> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Updating role with ID: {}", roleId);

        UserRole updatedRole = roleService.updateRole(roleId, request);
        RoleResponseDto response = RoleResponseDto.builder()
                .id(updatedRole.getId())
                .name(updatedRole.getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Role updated successfully"));
    }

    /**
     * Delete a role
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("hasPermission('Role', 'role:DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String roleId) {
        log.info("Deleting role with ID: {}", roleId);

        roleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted successfully"));
    }

    /**
     * Assign permissions to a role
     */
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasPermission('Role', 'role:UPDATE')")
    public ResponseEntity<ApiResponse<Void>> assignPermissions(
            @PathVariable String roleId,
            @Valid @RequestBody AssignPermissionsRequest request) {
        log.info("Assigning permissions to role ID: {}", roleId);

        roleService.assignPermissions(roleId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Permissions assigned successfully"));
    }

    /**
     * Remove permissions from a role
     */
    @DeleteMapping("/{roleId}/permissions")
    @PreAuthorize("hasPermission('Role', 'role:UPDATE')")
    public ResponseEntity<ApiResponse<Void>> removePermissions(
            @PathVariable String roleId,
            @Valid @RequestBody RemovePermissionsRequest request) {
        log.info("Removing permissions from role ID: {}", roleId);

        roleService.removePermissions(roleId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Permissions removed successfully"));
    }

    /**
     * Get all available permissions (for assignment)
     */
    @GetMapping("/permissions/available")
    @PreAuthorize("hasPermission('Permission', 'permission:READ')")
    public ResponseEntity<ApiResponse<List<PermissionDto>>> getAvailablePermissions() {
        log.info("Fetching available permissions for role assignment");

        List<PermissionDto> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions, "Available permissions retrieved successfully"));
    }

    /**
     * Get permissions assigned to a specific role
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasPermission('Role', 'role:READ')")
    public ResponseEntity<ApiResponse<List<RolePermissionDetailDto>>> getRolePermissions(@PathVariable String roleId) {
        log.info("Fetching permissions for role ID: {}", roleId);

        List<RolePermissionDetailDto> permissions = roleService.getRolePermissions(roleId);
        return ResponseEntity.ok(ApiResponse.success(permissions, "Role permissions retrieved successfully"));
    }
}
