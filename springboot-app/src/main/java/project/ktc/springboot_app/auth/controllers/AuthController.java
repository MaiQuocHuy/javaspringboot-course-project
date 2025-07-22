package project.ktc.springboot_app.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;
import project.ktc.springboot_app.auth.services.AuthServiceImp;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for user registration and login")
@Validated
public class AuthController {

    private final AuthServiceImp authService;

    public AuthController(AuthServiceImp authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with STUDENT role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> register(
            @Valid @RequestBody RegisterUserDto dto) {
        authService.registerUser(dto);
        return ApiResponseUtil.created("User registered successfully");
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticates user and returns JWT access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginUserDto dto) {
        Map<String, Object> tokens = authService.loginUser(dto);
        return ApiResponseUtil.success(tokens, "Login successful");
    }
}