package project.ktc.springboot_app.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RefreshTokenDto;
import project.ktc.springboot_app.auth.dto.RegisterApplicationDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;
import project.ktc.springboot_app.auth.dto.ResetPasswordDto;
import project.ktc.springboot_app.auth.services.AuthServiceImp;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication API", description = "Endpoints for user registration and login")
@Validated
@RequiredArgsConstructor
public class AuthController {

        private final AuthServiceImp authService;

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
        @Operation(summary = "Login a user", description = "Authenticates user and returns JWT access and refresh tokens. "
                        +
                        "Test accounts: Admin (alice@example.com/alice123), " +
                        "Student (bob@example.com/bob123), " +
                        "Instructor (charlie@example.com/charlie123)")
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

        @PostMapping("/reset-password")
        @Operation(summary = "Reset password", description = "Resets the user's password using the provided token and new password")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "401", description = "Invalid or expired token"),
                        @ApiResponse(responseCode = "500", description = "Password reset failed due to server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Map<String, String>>> resetPassword(
                        @Valid @RequestBody ResetPasswordDto dto) {
                return authService.resetPassword(dto.getOldPassword(), dto.getNewPassword());
        }

        @PostMapping("/logout")
        @Operation(summary = "Logout user", description = "Logs out the user by revoking the provided refresh token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logout successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid or missing refresh token"),
                        @ApiResponse(responseCode = "500", description = "Logout failed due to server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> logout(
                        @Valid @RequestBody RefreshTokenDto refreshTokenRequest) {
                String refreshToken = refreshTokenRequest.getRefreshToken();
                if (refreshToken == null || refreshToken.isEmpty()) {
                        return ApiResponseUtil.badRequest("Refresh token is required");
                }

                return authService.logout(refreshToken);
        }

        @PostMapping(value = "/register-application", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Register with instructor application", description = """
                        Registers a new user account. If the role is INSTRUCTOR, an instructor application is also submitted along with the required documents.

                        **Business Rules:**
                        - If role = INSTRUCTOR:
                          - Instructor application is automatically created along with user registration
                          - User must provide certificate, cv, and valid portfolio URL
                          - Only users with no prior submission or rejected (once) within 3 days can apply
                        - If role = STUDENT:
                          - Instructor application data is ignored
                        - If invalid data is provided or documents are missing (for INSTRUCTOR), registration will fail

                        **Required Fields for INSTRUCTOR:**
                        - certificate: Professional certification file (PDF/DOCX/IMG - max 15MB)
                        - cv: Resume/CV file (PDF/DOCX/IMG - max 15MB)
                        - portfolio: GitHub/LinkedIn/portfolio URL

                        **Optional Fields:**
                        - other: Additional supporting documents (max 15MB)
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Registration successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or missing required fields for instructor"),
                        @ApiResponse(responseCode = "409", description = "Email already exists"),
                        @ApiResponse(responseCode = "500", description = "Registration failed due to server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> registerApplication(
                        @Parameter(description = "Full name of the user", required = true) @RequestParam("name") String name,
                        @Parameter(description = "Valid and unique email", required = true) @RequestParam("email") String email,
                        @Parameter(description = "Password (minimum 6 characters)", required = true) @RequestParam("password") String password,
                        @Parameter(description = "Role of the user (STUDENT or INSTRUCTOR)", required = true) @RequestParam("role") String role,
                        @Parameter(description = "GitHub/LinkedIn/portfolio URL (required for INSTRUCTOR)", required = false) @RequestParam(value = "portfolio", required = false) String portfolio,
                        @Parameter(description = "Professional certification file (required for INSTRUCTOR)", required = false) @RequestParam(value = "certificate", required = false) MultipartFile certificate,
                        @Parameter(description = "Resume/CV file (required for INSTRUCTOR)", required = false) @RequestParam(value = "cv", required = false) MultipartFile cv,
                        @Parameter(description = "Optional supporting documents", required = false) @RequestParam(value = "other", required = false) MultipartFile other) {

                // Create DTO from form parameters
                RegisterApplicationDto dto = new RegisterApplicationDto();
                dto.setName(name);
                dto.setEmail(email);
                dto.setPassword(password);
                dto.setPortfolio(portfolio);

                // Parse role enum
                try {
                        dto.setRole(project.ktc.springboot_app.auth.enums.UserRoleEnum.valueOf(role.toUpperCase()));
                } catch (IllegalArgumentException e) {
                        return ApiResponseUtil.badRequest("Invalid role. Must be STUDENT or INSTRUCTOR");
                }

                return authService.registerApplication(dto, certificate, cv, other);
        }

}