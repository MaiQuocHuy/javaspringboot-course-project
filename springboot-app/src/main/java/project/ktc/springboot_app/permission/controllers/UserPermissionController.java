package project.ktc.springboot_app.permission.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.permission.dto.UserPermissionsDto;
import project.ktc.springboot_app.permission.services.AuthorizationService;

/**
 * Controller for user permission operations
 * Provides API endpoints for retrieving user permissions
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class UserPermissionController {

    private final AuthorizationService authorizationService;

    /**
     * Get current user's permissions
     * 
     * @param currentUser the authenticated user
     * @return user permissions with detailed information
     */
    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<UserPermissionsDto>> getCurrentUserPermissions(
            @AuthenticationPrincipal User currentUser) {
        log.info("Getting permissions for current user: {}", currentUser.getEmail());

        UserPermissionsDto permissions = authorizationService.getUserPermissionsDto(currentUser);
        return ResponseEntity.ok(ApiResponse.success(permissions, "User permissions retrieved successfully"));
    }

    /**
     * Check if current user has a specific permission
     * 
     * @param currentUser   the authenticated user
     * @param permissionKey the permission key to check
     * @return boolean indicating if user has the permission
     */
    @GetMapping("/permissions/check")
    public ResponseEntity<ApiResponse<Boolean>> checkPermission(
            @AuthenticationPrincipal User currentUser,
            @RequestParam String permissionKey) {
        log.info("Checking permission '{}' for user: {}", permissionKey, currentUser.getEmail());

        boolean hasPermission = authorizationService.getUserPermissions(currentUser)
                .contains(permissionKey);

        return ResponseEntity.ok(ApiResponse.success(hasPermission,
                String.format("Permission check for '%s' completed", permissionKey)));
    }

    /**
     * Get user's role information
     * 
     * @param currentUser the authenticated user
     * @return user's role information
     */
    @GetMapping("/role")
    public ResponseEntity<ApiResponse<UserPermissionsDto.RoleInfoDto>> getCurrentUserRole(
            @AuthenticationPrincipal User currentUser) {
        log.info("Getting role for current user: {}", currentUser.getEmail());

        UserPermissionsDto.RoleInfoDto roleInfo = UserPermissionsDto.RoleInfoDto.builder()
                .id(currentUser.getRole().getId())
                .name(currentUser.getRole().getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success(roleInfo, "User role retrieved successfully"));
    }
}
