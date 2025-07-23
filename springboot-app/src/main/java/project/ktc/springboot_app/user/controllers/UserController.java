package project.ktc.springboot_app.user.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.user.dto.UpdateUserDto;
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

    @PutMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update user profile with optional image upload", description = """
                Updates the profile of the currently authenticated user, including `name`, `bio`, and optional `thumbnail` upload.

                **Fields:**
                - `name` (text): Full name (required)
                - `bio` (text): Bio (optional)
                - `thumbnail` (file): Image upload (optional)

                **Supported Image Formats:** JPEG, PNG, GIF, BMP, WebP
                **Max Size:** 10MB
            """, security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or file format"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<UserResponseDto>> updateProfile(
            @Parameter(description = "Full name", required = true) @RequestParam("name") String name,
            @Parameter(description = "Bio", required = false) @RequestParam(value = "bio", required = false) String bio,
            @Parameter(description = "Optional profile image file", required = false) @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile) {
        UpdateUserDto updateUserDto = new UpdateUserDto(name, bio);
        return userService.updateProfile(updateUserDto, thumbnailFile);
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