package project.ktc.springboot_app.permission.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.permission.dto.CreateRoleRequest;
import project.ktc.springboot_app.permission.dto.PermissionResponseDto;
import project.ktc.springboot_app.permission.dto.PermissionUpdateRequest;
import project.ktc.springboot_app.permission.dto.PermissionUpdateResponse;
import project.ktc.springboot_app.permission.dto.ResourceDto;
import project.ktc.springboot_app.permission.dto.ResourceTreeResponse;
import project.ktc.springboot_app.permission.dto.RoleResponseDto;
import project.ktc.springboot_app.permission.entity.RolePermission;
import project.ktc.springboot_app.permission.mapper.PermissionMapper;
import project.ktc.springboot_app.permission.mapper.RoleMapper;
import project.ktc.springboot_app.permission.services.PermissionServiceImp;
import project.ktc.springboot_app.permission.services.RoleServiceImp;

/**
 * Admin Role Permission Controller for managing role-specific permissions
 * Only users with ADMIN role can access these endpoints
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Role Permission API", description = "API for managing role-specific permissions (Admin only)")
public class AdminRolePermissionController {

    private final PermissionServiceImp permissionService;
    private final PermissionMapper permissionMapper;
    private final RoleServiceImp roleService;
    private final RoleMapper roleMapper;

    /**
     * Retrieve all permissions assigned to a specific role
     * 
     * @param roleId the role ID
     * @return list of permissions assigned to the role with role-specific metadata
     */
    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get permissions by role", description = "Retrieves all permissions assigned to a specific role, including role-specific and permission-level status flags. Only ADMIN role is allowed to call this API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<PermissionResponseDto>>> getPermissionsByRole(
            @PathVariable String roleId) {
        log.info("Admin requesting permissions for role ID: {}", roleId);

        try {
            // Fetch all permissions assigned to the role
            List<RolePermission> rolePermissions = permissionService.getPermissionsByRole(roleId);
            log.debug("Found {} permissions assigned to role ID: {}", rolePermissions.size(), roleId);

            // Convert to DTOs with role-specific information
            List<PermissionResponseDto> permissionDtos = permissionMapper
                    .toRolePermissionResponseDtoList(rolePermissions);
            log.debug("Converted {} role permissions to DTOs", permissionDtos.size());

            return ApiResponseUtil.success(permissionDtos, "Permissions retrieved successfully");

        } catch (RuntimeException e) {
            log.error("Role not found with ID: {}", roleId, e);
            return ApiResponseUtil.notFound("Role not found with ID: " + roleId);
        } catch (Exception e) {
            log.error("Error retrieving permissions for role ID: {}", roleId, e);
            return ApiResponseUtil.internalServerError("Internal server error while retrieving permissions");
        }
    }

    /**
     * Update permissions for a specific role
     * 
     * @param roleId  the role ID
     * @param request the permission update request
     * @return updated permissions for the role
     */
    @PatchMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update role permissions", description = "Updates the permissions assigned to a specific role. This allows enabling or disabling permissions at the role_permission level. Only ADMIN role is allowed to call this API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request - Permission key format or validation error"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PermissionUpdateResponse>> updateRolePermissions(
            @PathVariable String roleId,
            @Valid @RequestBody PermissionUpdateRequest request) {
        log.info("Admin updating permissions for role ID: {} with {} permission changes",
                roleId, request.getPermissions().size());

        try {
            // Update permissions for the role
            List<RolePermission> updatedRolePermissions = permissionService.updatePermissionsForRole(roleId, request);
            log.debug("Successfully updated permissions for role ID: {}", roleId);

            // Convert to response DTO
            PermissionUpdateResponse response = permissionMapper.toPermissionUpdateResponse(roleId,
                    updatedRolePermissions);
            log.debug("Converted {} updated permissions to response DTO", updatedRolePermissions.size());

            return ApiResponseUtil.success(response, "Permissions updated successfully");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Role not found")) {
                log.error("Role not found with ID: {}", roleId, e);
                return ApiResponseUtil.notFound("Role not found with ID: " + roleId);
            } else if (e.getMessage().contains("Permission not found")) {
                log.error("Invalid permission key in request for role ID: {}", roleId, e);
                return ApiResponseUtil.badRequest("Invalid permission key: " + e.getMessage());
            } else if (e.getMessage().contains("disabled at system level")) {
                log.error("Attempt to assign system-disabled permission for role ID: {}", roleId, e);
                return ApiResponseUtil.badRequest("Permission assignment error: " + e.getMessage());
            } else {
                log.error("Validation error while updating permissions for role ID: {}", roleId, e);
                return ApiResponseUtil.badRequest("Validation error: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error updating permissions for role ID: {}", roleId, e);
            return ApiResponseUtil.internalServerError("Internal server error while updating permissions");
        }
    }

    /**
     * Get hierarchical resource tree with permission assignments for a specific
     * role
     * 
     * @param roleId the role ID
     * @return hierarchical resource tree with assigned and inherited permissions
     */
    @GetMapping("/{roleId}/resources")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get resource tree for role", description = "Retrieves a hierarchical tree of resources with permission assignments for a specific role. Each node indicates whether the role has the permission directly assigned or inherits it from a parent resource. Only ADMIN role is allowed to call this API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource tree retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ResourceTreeResponse>> getResourceTreeForRole(
            @PathVariable String roleId) {
        log.info("Admin requesting resource tree for role ID: {}", roleId);

        try {
            // Fetch resource tree for the role
            List<ResourceDto> resourceTree = permissionService.getResourceTreeForRole(roleId);
            log.debug("Built resource tree with {} root resources for role ID: {}", resourceTree.size(), roleId);

            // Create response with statistics
            // Note: Role name would be fetched from UserRoleService if needed
            ResourceTreeResponse response = permissionMapper.toResourceTreeResponse(
                    roleId,
                    null, // Role name - could be enhanced later
                    resourceTree);
            log.debug("Created resource tree response with {} total resources", response.getTotalResources());

            return ApiResponseUtil.success(response, "Resource tree retrieved successfully");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Role not found")) {
                log.error("Role not found with ID: {}", roleId, e);
                return ApiResponseUtil.notFound("Role not found with ID: " + roleId);
            } else {
                log.error("Error building resource tree for role ID: {}", roleId, e);
                return ApiResponseUtil.badRequest("Error building resource tree: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error retrieving resource tree for role ID: {}", roleId, e);
            return ApiResponseUtil.internalServerError("Internal server error while retrieving resource tree");
        }
    }

    /**
     * Create a new role
     * 
     * @param request the role creation request
     * @return the created role details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new role", description = "Creates a new role in the system. Only ADMIN users can call this API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input - validation error or role name format error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "409", description = "Conflict - Role name already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<RoleResponseDto>> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        log.info("Admin requesting to create new role: {}", request.getName());

        try {
            // Create the role
            UserRole createdRole = roleService.createRole(request);
            log.info("Role created successfully with ID: {} and name: {}", createdRole.getId(), createdRole.getRole());

            // Convert to DTO
            RoleResponseDto responseDto = roleMapper.toResponseDto(createdRole);

            return ApiResponseUtil.created(responseDto, "Role created successfully");

        } catch (IllegalArgumentException e) {
            log.warn("Role creation failed due to validation error: {}", e.getMessage());
            if (e.getMessage().contains("already exists")) {
                return ApiResponseUtil.conflict(e.getMessage());
            } else {
                return ApiResponseUtil.badRequest(e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error creating role: {}", request.getName(), e);
            return ApiResponseUtil.internalServerError("Internal server error while creating role");
        }
    }
}
