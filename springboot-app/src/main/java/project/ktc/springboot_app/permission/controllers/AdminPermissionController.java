package project.ktc.springboot_app.permission.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.Permission;
import project.ktc.springboot_app.permission.dto.PermissionResponseDto;
import project.ktc.springboot_app.permission.mapper.PermissionMapper;
import project.ktc.springboot_app.permission.services.PermissionServiceImp;

/**
 * Admin Permission Controller for managing system permissions
 * Only users with ADMIN role can access these endpoints
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Permission API", description = "API for managing system permissions (Admin only)")
public class AdminPermissionController {

    private final PermissionServiceImp permissionService;
    private final PermissionMapper permissionMapper;

    /**
     * Retrieve all permissions in the system
     * 
     * @return list of all permissions with resource and action metadata
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all permissions", description = "Retrieves a list of all permissions in the system, including related resource and action metadata. Only ADMIN role is allowed to call this API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<PermissionResponseDto>>> getAllPermissions() {
        log.info("Admin requesting all permissions");

        try {
            // Fetch all permissions with resource and action details
            List<Permission> permissions = permissionService.getAllPermissionsWithDetails();
            log.debug("Found {} permissions in the system", permissions.size());

            // Convert to DTOs
            List<PermissionResponseDto> permissionDtos = permissionMapper.toResponseDtoList(permissions);
            log.debug("Converted {} permissions to DTOs", permissionDtos.size());

            return ApiResponseUtil.success(permissionDtos, "Get all permissions successful");

        } catch (Exception e) {
            log.error("Error retrieving permissions for admin", e);
            return ApiResponseUtil.internalServerError("Internal server error while retrieving permissions");
        }
    }
}
