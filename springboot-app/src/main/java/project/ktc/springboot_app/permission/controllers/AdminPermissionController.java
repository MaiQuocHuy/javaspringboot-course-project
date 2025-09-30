package project.ktc.springboot_app.permission.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.InvalidFilterTypeException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.permission.dto.PermissionResponseDto;
import project.ktc.springboot_app.permission.dto.PermissionUpdateRequest;
import project.ktc.springboot_app.permission.dto.RoleWithPermissionsDto;
import project.ktc.springboot_app.permission.entity.Permission;
import project.ktc.springboot_app.permission.interfaces.RoleService;
import project.ktc.springboot_app.permission.mapper.PermissionMapper;
import project.ktc.springboot_app.permission.services.PermissionServiceImp;

/**
 * Admin Permission Controller for managing system permissions Only users with
 * ADMIN role can access
 * these endpoints
 */
@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Permission API", description = "API for managing system permissions (Admin only)")
public class AdminPermissionController {

	private final PermissionServiceImp permissionService;
	private final PermissionMapper permissionMapper;
	private final RoleService roleService;

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
			return ApiResponseUtil.internalServerError(
					"Internal server error while retrieving permissions");
		}
	}

	/**
	 * Retrieve all roles with their associated permissions (paginated)
	 *
	 * @param page
	 *            page number (default: 0)
	 * @param size
	 *            page size (default: 10)
	 * @return paginated list of roles with permissions
	 */
	@GetMapping("/roles")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get all roles with permissions", description = "Retrieves a list of all roles with their associated permissions. Only ADMIN users can call this API.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request - Invalid pagination parameters"),
			@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<RoleWithPermissionsDto>>> getAllRolesWithPermissions(
			@Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

		log.info("Admin requesting all roles with permissions - page: {}, size: {}", page, size);

		try {
			// Validate pagination parameters
			if (page < 0) {
				log.warn("Invalid page parameter: {}", page);
				return ApiResponseUtil.badRequest("Page number cannot be negative");
			}
			if (size <= 0 || size > 100) {
				log.warn("Invalid size parameter: {}", size);
				return ApiResponseUtil.badRequest("Page size must be between 1 and 100");
			}

			// Fetch paginated roles with permissions
			Page<RoleWithPermissionsDto> rolesPage = roleService.getAllRolesWithPermissions(page, size);
			log.debug(
					"Found {} roles in page {} of {}",
					rolesPage.getContent().size(),
					page,
					rolesPage.getTotalPages());

			// Build paginated response using PaginatedResponse
			PaginatedResponse<RoleWithPermissionsDto> response = PaginatedResponse.<RoleWithPermissionsDto>builder()
					.content(rolesPage.getContent())
					.page(
							PaginatedResponse.PageInfo.builder()
									.number(rolesPage.getNumber())
									.size(rolesPage.getSize())
									.totalPages(rolesPage.getTotalPages())
									.totalElements(rolesPage.getTotalElements())
									.first(rolesPage.isFirst())
									.last(rolesPage.isLast())
									.build())
					.build();

			log.debug(
					"Successfully built paginated response with {} roles", response.getContent().size());

			return ApiResponseUtil.success(response, "Roles retrieved successfully");

		} catch (IllegalArgumentException e) {
			log.warn("Invalid request parameters for roles with permissions: {}", e.getMessage());
			return ApiResponseUtil.badRequest(e.getMessage());
		} catch (Exception e) {
			log.error(
					"Error retrieving roles with permissions for admin - page: {}, size: {}", page, size, e);
			return ApiResponseUtil.internalServerError(
					"Internal server error while retrieving roles with permissions");
		}
	}

	/**
	 * Retrieve all available permissions with assignment information for a specific
	 * role Based on
	 * permission_role_assign_rules table - only permissions defined in this table
	 * can be assigned to
	 * the corresponding role
	 *
	 * @param roleId
	 *            the role ID to check assignment rules against
	 * @return list of available permissions with assignment flags
	 */
	@GetMapping("/available")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get available permissions for role", description = "Retrieves all permissions in the system with assignment information for a specific role based on permission_role_assign_rules table. Only ADMIN users can call this API.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Available permissions retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request - Missing or invalid role_id"),
			@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<project.ktc.springboot_app.permission.dto.AvailablePermissionDto>>> getAvailablePermissions(
			@Parameter(description = "Role ID to check assignment rules against", example = "role-001", required = true) @RequestParam(name = "role_id") String roleId) {

		log.info("Admin requesting available permissions for role ID: {}", roleId);

		try {
			// Validate roleId is provided
			if (roleId == null || roleId.trim().isEmpty()) {
				log.warn("Missing or empty role_id parameter");
				return ApiResponseUtil.badRequest("role_id parameter is required");
			}

			// Fetch available permissions with assignment information
			List<project.ktc.springboot_app.permission.dto.AvailablePermissionDto> availablePermissions = permissionService
					.getAllAvailablePermissions(roleId.trim());
			log.debug(
					"Found {} available permissions for role ID: {}", availablePermissions.size(), roleId);

			return ApiResponseUtil.success(
					availablePermissions, "Available permissions retrieved successfully");

		} catch (RuntimeException e) {
			if (e.getMessage().contains("Role not found")) {
				log.warn("Role not found: {}", roleId);
				return ApiResponseUtil.badRequest("Role not found with ID: " + roleId);
			}
			log.error("Error retrieving available permissions for role ID: {}", roleId, e);
			return ApiResponseUtil.internalServerError(
					"Internal server error while retrieving available permissions");
		} catch (Exception e) {
			log.error("Unexpected error retrieving available permissions for role ID: {}", roleId, e);
			return ApiResponseUtil.internalServerError(
					"Internal server error while retrieving available permissions");
		}
	}

	/**
	 * Retrieve all permissions for a specific role grouped by resource
	 *
	 * @param roleId
	 *            the role ID to get permissions for
	 * @return permissions grouped by resource with assignability information
	 */
	@GetMapping("/{roleId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get permissions for role", description = "Retrieves all permissions for a specific role, including assignability rules, so that admin can update permissions. Only ADMIN users can call this API.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Bad Request - Invalid role_id"),
			@ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
			@ApiResponse(responseCode = "404", description = "Role not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<project.ktc.springboot_app.permission.dto.RolePermissionGroupedDto>> getPermissionsByRole(
			@Parameter(description = "Role ID to get permissions for", example = "role-001", required = true) @org.springframework.web.bind.annotation.PathVariable String roleId) {

		log.info("Admin requesting permissions for role ID: {}", roleId);

		try {
			// Validate roleId is provided
			if (roleId == null || roleId.trim().isEmpty()) {
				log.warn("Missing or empty role_id parameter");
				return ApiResponseUtil.badRequest("role_id parameter is required");
			}

			// Fetch permissions grouped by resource with assignability information
			project.ktc.springboot_app.permission.dto.RolePermissionGroupedDto groupedPermissions = permissionService
					.getPermissionsGroupedByResourceForRole(roleId.trim());
			log.debug("Retrieved permissions grouped by resource for role ID: {}", roleId);

			return ApiResponseUtil.success(groupedPermissions, "Permissions retrieved successfully");

		} catch (RuntimeException e) {
			if (e.getMessage().contains("Role not found")) {
				log.warn("Role not found: {}", roleId);
				return ApiResponseUtil.notFound("Role not found with ID: " + roleId);
			}
			log.error("Error retrieving permissions for role ID: {}", roleId, e);
			return ApiResponseUtil.internalServerError(
					"Internal server error while retrieving permissions");
		} catch (Exception e) {
			log.error("Unexpected error retrieving permissions for role ID: {}", roleId, e);
			return ApiResponseUtil.internalServerError(
					"Internal server error while retrieving permissions");
		}
	}

	/**
	 * API 7.8: Update permissions for a specific role with filter types
	 *
	 * @param roleId
	 *            The ID of the role to update permissions for
	 * @param request
	 *            The permission update request containing permissions and filter
	 *            types
	 * @return success response with updated permissions
	 */
	@PatchMapping("/{roleId}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update role permissions with filter types", description = "Updates permissions for a role including filter type assignments (ALL/OWN access)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Permissions updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "Role not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<?> updateRolePermissions(
			@Parameter(description = "Role ID to update permissions for", required = true) @PathVariable("roleId") String roleId,
			@Parameter(description = "Permission update request with filter types", required = true) @RequestBody PermissionUpdateRequest request) {

		try {
			log.debug(
					"Updating permissions for role ID: {} with {} permission changes",
					roleId,
					request.getPermissions().size());

			// Validate role_id parameter
			if (roleId == null || roleId.trim().isEmpty()) {
				log.warn("Missing or empty role_id parameter");
				return ApiResponseUtil.badRequest("role_id parameter is required");
			}

			// Validate request body
			if (request == null
					|| request.getPermissions() == null
					|| request.getPermissions().isEmpty()) {
				log.warn("Missing or empty permissions in request body");
				return ApiResponseUtil.badRequest("permissions array is required and cannot be empty");
			}

			// Update permissions for the role (full replacement)
			var updatedRolePermissions = permissionService.updatePermissionsForRole(roleId.trim(), request);
			log.debug(
					"Successfully updated permissions for role ID: {} ({} total records, {} active)",
					roleId,
					updatedRolePermissions.size(),
					updatedRolePermissions.stream().filter(rp -> rp.getIsActive()).count());

			// Map to update response including filter types
			var responseDto = permissionMapper.toPermissionUpdateResponse(roleId.trim(), updatedRolePermissions);

			return ApiResponseUtil.success(responseDto, "Permissions updated successfully");

		} catch (InvalidFilterTypeException e) {
			log.warn("Invalid filter type in request: {}", e.getMessage());
			return ApiResponseUtil.badRequest(e.getMessage());
		} catch (RuntimeException e) {
			if (e.getMessage().contains("Role not found")) {
				log.warn("Role not found: {}", roleId);
				return ApiResponseUtil.notFound("Role not found with ID: " + roleId);
			} else if (e.getMessage().contains("Permission not found")
					|| e.getMessage().contains("FilterType not found")) {
				log.warn("Invalid permission or filter type in request: {}", e.getMessage());
				return ApiResponseUtil.badRequest(e.getMessage());
			} else if (e.getMessage().contains("Cannot assign permission")) {
				log.warn("Permission assignment validation failed: {}", e.getMessage());
				return ApiResponseUtil.badRequest(e.getMessage());
			}
			log.error("Error updating permissions for role ID: {}", roleId, e);
			return ApiResponseUtil.internalServerError(
					"Internal server error while updating permissions");
		} catch (Exception e) {
			log.error("Unexpected error updating permissions for role ID: {}", roleId, e);
			return ApiResponseUtil.internalServerError(
					"Internal server error while updating permissions");
		}
	}
}
