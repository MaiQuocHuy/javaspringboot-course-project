package project.ktc.springboot_app.user.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.cj.x.protobuf.MysqlxCrud.Update;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.auth.dto.UserResponseDto;
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
        @Operation(summary = "Get all users", description = "Retrieve a list of all users")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<UserResponseDto>>> getAllUsers() {
                return userService.getUsers();
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User found"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> getUserById(
                        @PathVariable String id) {
                return userService.getUserById(id);
        }

        @PutMapping("/{id}/role")
        @Operation(summary = "Update user role", description = "Update the role of a user by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User role updated successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid role or user ID")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> updateUserRole(
                        @PathVariable String id, @Valid @RequestBody UpdateUserRoleDto roleDto) {
                return userService.updateUserRole(id, roleDto);
        }

        @PutMapping("/{id}/status")
        @Operation(summary = "Update user status", description = "Update the status of a user by their ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User status updated successfully"),
                        @ApiResponse(responseCode = "404", description = "User not found"),
                        @ApiResponse(responseCode = "400", description = "Invalid status or user ID")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> updateUserStatus(
                        @PathVariable String id, @Valid @RequestBody UpdateUserStatusDto status) {
                // Assuming the service method exists to handle status updates
                return userService.updateUserStatus(id, status);
        }

        @PostMapping
        @Operation(summary = "Create a new user", description = "Create a new user in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User created"),
                        @ApiResponse(responseCode = "400", description = "Invalid input")
        })
        public ResponseEntity<String> createUser(@RequestBody String user) {
                // Mock response
                return ResponseEntity.status(201).body("User created: " + user);
        }
}
