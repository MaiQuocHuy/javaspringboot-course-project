package project.ktc.springboot_app.auth.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.auth.dto.ForgotPasswordRequestDto;
import project.ktc.springboot_app.auth.dto.ForgotPasswordResponseDto;
import project.ktc.springboot_app.auth.dto.ResetPasswordConfirmDto;
import project.ktc.springboot_app.auth.services.PasswordResetService;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;

/**
 * REST Controller for handling password reset operations.
 *
 * <p>
 * This controller provides endpoints for: - Initiating password reset with
 * email/OTP -
 * Confirming password reset with OTP and new password
 *
 * <p>
 * Features: - Rate limiting and abuse prevention - Secure OTP generation and
 * validation -
 * Comprehensive error handling - Input validation and sanitization - Audit
 * logging for security
 *
 * @author KTC Team
 * @version 1.0
 * @since 2025-01-26
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Password Reset", description = "Password reset management operations")
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	/**
	 * Initiate password reset process by sending OTP to user's email.
	 *
	 * <p>
	 * This endpoint: - Validates the user's email address - Checks rate limiting to
	 * prevent abuse
	 * - Generates a secure OTP code - Sends email with password reset instructions
	 * - Returns masked
	 * email and expiry information
	 *
	 * <p>
	 * Rate Limiting: - Maximum 3 requests per hour per email - Minimum 5 minutes
	 * between requests
	 *
	 * <p>
	 * Security Features: - Email address validation - Rate limiting per user -
	 * Secure OTP
	 * generation - Token expiration (15 minutes default) - Previous token
	 * invalidation
	 *
	 * @param request
	 *            the forgot password request containing user email
	 * @return response with masked email and expiry information
	 */
	@PostMapping("/forgot-password")
	@Operation(summary = "Initiate password reset", description = "Send OTP to user's email for password reset process. Subject to rate limiting.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OTP sent successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request - validation errors"),
			@ApiResponse(responseCode = "404", description = "User not found with provided email"),
			@ApiResponse(responseCode = "429", description = "Rate limit exceeded - too many requests"),
			@ApiResponse(responseCode = "500", description = "Internal server error - email sending failed")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ForgotPasswordResponseDto>> initiateForgotPassword(
			@Parameter(description = "Forgot password request with user email", required = true) @Valid @RequestBody ForgotPasswordRequestDto request) {

		log.info(
				"Password reset initiation requested for email pattern: {}***",
				request.getEmail().substring(0, Math.min(3, request.getEmail().length())));

		try {
			ForgotPasswordResponseDto response = passwordResetService.initiateForgotPassword(request);

			log.info(
					"Password reset OTP sent successfully. Expiry: {}, Previous tokens invalidated: {}",
					response.getExpiresAt(),
					response.getPreviousTokensInvalidated());

			return ApiResponseUtil.success(
					response, "Password reset OTP sent to your email address. Please check your inbox.");

		} catch (Exception e) {
			log.error("Failed to initiate password reset: {}", e.getMessage(), e);
			throw e; // Let global exception handler manage the response
		}
	}

	/**
	 * Confirm password reset using OTP and set new password.
	 *
	 * <p>
	 * This endpoint: - Validates the OTP code and user email - Checks password
	 * strength
	 * requirements - Verifies password confirmation match - Updates user's password
	 * with encryption -
	 * Marks the OTP token as used - Logs the successful password change
	 *
	 * <p>
	 * Security Features: - OTP verification with attempt limiting - Password
	 * strength validation -
	 * Password confirmation matching - Token invalidation after use - Secure
	 * password hashing -
	 * Account activity logging
	 *
	 * <p>
	 * OTP Validation: - Maximum 3 verification attempts per token - Token expires
	 * after 15 minutes
	 * (default) - One-time use only - User-specific validation
	 *
	 * @param request
	 *            the reset password confirmation request with OTP and new
	 *            password
	 * @return success response indicating password was reset
	 */
	@PostMapping("/forgot-password/confirm")
	@Operation(summary = "Confirm password reset", description = "Verify OTP and set new password. OTP has limited attempts and expiration time.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Password reset successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request - validation errors or password mismatch"),
			@ApiResponse(responseCode = "401", description = "Invalid or expired OTP code"),
			@ApiResponse(responseCode = "404", description = "User not found with provided email"),
			@ApiResponse(responseCode = "410", description = "OTP expired or maximum attempts exceeded"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> confirmPasswordReset(
			@Parameter(description = "Password reset confirmation with OTP and new password", required = true) @Valid @RequestBody ResetPasswordConfirmDto request) {

		log.info(
				"Password reset confirmation requested for email pattern: {}***",
				request.getEmail().substring(0, Math.min(3, request.getEmail().length())));

		try {
			passwordResetService.confirmPasswordReset(request);

			log.info(
					"Password reset completed successfully for user with email pattern: {}***",
					request.getEmail().substring(0, Math.min(3, request.getEmail().length())));

			return ApiResponseUtil.success(
					"Password has been reset successfully. You can now log in with your new password.");

		} catch (Exception e) {
			log.error("Failed to confirm password reset: {}", e.getMessage(), e);
			throw e; // Let global exception handler manage the response
		}
	}

	/**
	 * Health check endpoint for password reset service. Used for monitoring and
	 * ensuring the service
	 * is operational.
	 *
	 * @return simple health status
	 */
	@GetMapping("/forgot-password/health")
	@Operation(summary = "Password reset service health check", description = "Check if password reset service is operational")
	@ApiResponse(responseCode = "200", description = "Service is healthy")
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<String>> healthCheck() {
		return ApiResponseUtil.success("healthy", "Password reset service is operational");
	}
}
