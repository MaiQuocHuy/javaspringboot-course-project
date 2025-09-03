package project.ktc.springboot_app.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.user.dto.AdminUserPageResponseDto;
import project.ktc.springboot_app.user.dto.AdminCreateUserDto;
import project.ktc.springboot_app.user.dto.CreateUserDto;
import project.ktc.springboot_app.user.dto.UpdateUserDto;
import project.ktc.springboot_app.user.dto.UpdateUserRoleDto;
import project.ktc.springboot_app.user.dto.UpdateUserStatusDto;
import project.ktc.springboot_app.user.services.UserServiceImp;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin User API", description = "API for managing users")
public class AdminUserController {

        private final UserServiceImp userService;

        @GetMapping
        @PreAuthorize("hasPermission('User', 'user:READ')")
        @Operation(summary = "Get users with pagination and search", description = "Retrieve paginated list of users with search and filter capabilities")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user:read permission required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminUserPageResponseDto>> getUsersWithPagination(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

                        @Parameter(description = "Search by name or email", example = "john") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by role", example = "STUDENT", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {
                                        "STUDENT", "INSTRUCTOR",
                                        "ADMIN" })) @RequestParam(required = false) String role,

                        @Parameter(description = "Filter by active status", example = "true") @RequestParam(required = false) Boolean isActive,

                        @Parameter(description = "Sort criteria (format: field,direction)", example = "name,asc") @RequestParam(defaultValue = "createdAt,desc") String sort) {
                return userService.getUsersWithPagination(search, role, isActive, page, size, sort);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasPermission('User', 'user:READ')")
        @Operation(summary = "Get user by ID with admin details", description = "Retrieve a user by their ID with enrolled courses, total payments, and study time")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User found"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user:read permission required"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid user ID format")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<project.ktc.springboot_app.user.dto.AdminUserDetailResponseDto>> getUserById(
                        @Parameter(description = "User ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String id) {
                return userService.getAdminUserById(id);
        }

        @PutMapping("/{id}/role")
        @PreAuthorize("hasPermission('User', 'user:UPDATE')")
        @Operation(summary = "Update user role", description = "Update the role of a user by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User role updated successfully"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user:edit permission required"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid role or user ID")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> updateUserRole(
                        @Parameter(description = "User ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String id,
                        @Valid @RequestBody UpdateUserRoleDto roleDto) {
                return userService.updateUserRole(id, roleDto);
        }

        @PutMapping("/{id}/status")
        @PreAuthorize("hasPermission('User', 'user:UPDATE')")
        @Operation(summary = "Update user status", description = "Update the active status of a user by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user:edit permission required"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid status or user ID")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> updateUserStatus(
                        @Parameter(description = "User ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String id,
                        @Valid @RequestBody UpdateUserStatusDto status) {
                return userService.updateUserStatus(id, status);
        }

        @PostMapping("/newRole")
        @PreAuthorize("hasPermission('User', 'user:CREATE')")
        @Operation(summary = "Create a new user", description = "Create a new user in the system with exactly one role and comprehensive validation")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User created successfully"),
                        @ApiResponse(responseCode = "400", description = "Bad Request - missing or invalid fields"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid JWT"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - caller is not ADMIN"),
                        @ApiResponse(responseCode = "409", description = "Conflict - email or username already exists"),
                        @ApiResponse(responseCode = "422", description = "Unprocessable Entity - password too weak, invalid email, or role not recognized"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> createUserV2(
                        @Valid @RequestBody AdminCreateUserDto createUserDto) {
                return userService.createAdminUser(createUserDto);
        }

        @PostMapping
        @PreAuthorize("hasPermission('User', 'user:CREATE')")
        @Operation(summary = "Create a new user (original)", description = "Create a new user in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user:create permission required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> createUser(
                        @Valid @RequestBody CreateUserDto createUserDto) {
                return userService.createUser(createUserDto);
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasPermission('User', 'user:UPDATE')")
        @Operation(summary = "Update user profile", description = "Update user basic information (name, email, bio)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User updated successfully"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - user:edit permission required"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> updateUser(
                        @Parameter(description = "User ID", required = true, example = "7200a420-2ff3-4f18-9933-1b86d05f1a78") @PathVariable String id,
                        @Valid @RequestBody UpdateUserDto updateUserDto) {
                return userService.updateUserProfile(id, updateUserDto);
        }
}
