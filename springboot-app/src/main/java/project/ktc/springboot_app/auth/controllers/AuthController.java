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
import project.ktc.springboot_app.auth.dto.RefreshTokenDto;
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
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "409", description = "Email already exists"),
            @ApiResponse(responseCode = "500", description = "Registration failed due to server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> register(
            @Valid @RequestBody RegisterUserDto dto) {
        return authService.registerUser(dto);
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticates user and returns JWT access and refresh tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Login failed due to server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginUserDto dto) {
        return authService.loginUser(dto);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refreshes the JWT access token using the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "500", description = "Token refresh failed due to server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Map<String, Object>>> refreshToken(
            @Valid @RequestBody RefreshTokenDto refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ApiResponseUtil.badRequest("Refresh token is required");
        }

        return authService.refreshAccessToken(refreshToken);
    }

}