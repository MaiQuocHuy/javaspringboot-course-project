package project.ktc.springboot_app.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.user.services.UserServiceImp;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "API for managing users")
public class UserController {

    private final UserServiceImp userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> getProfile() {
        return userService.getProfile();
    }

    // Mock endpoints for demonstration - can be removed later
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<String>> getAllUsers() {
        // Mock data for demonstration
        List<String> users = Arrays.asList("Alice Nguyen", "Bob Tran");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<String> getUserById(@PathVariable String id) {
        // Mock response
        return ResponseEntity.ok("User with ID: " + id);
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