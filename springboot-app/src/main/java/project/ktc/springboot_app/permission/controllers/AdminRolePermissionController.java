package project.ktc.springboot_app.permission.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.RolePermission;
import project.ktc.springboot_app.permission.dto.PermissionResponseDto;
import project.ktc.springboot_app.permission.mapper.PermissionMapper;
import project.ktc.springboot_app.permission.services.PermissionServiceImp;

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
}
