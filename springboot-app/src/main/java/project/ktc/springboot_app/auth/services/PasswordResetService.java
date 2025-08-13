package project.ktc.springboot_app.auth.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.auth.dto.ForgotPasswordRequestDto;
import project.ktc.springboot_app.auth.dto.ForgotPasswordResponseDto;
import project.ktc.springboot_app.auth.dto.ResetPasswordConfirmDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.common.exception.BusinessLogicException;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.exception.ValidationException;
import project.ktc.springboot_app.email.interfaces.EmailService;
import project.ktc.springboot_app.entity.PasswordResetToken;
import project.ktc.springboot_app.repository.PasswordResetTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for handling password reset functionality.
 * 
 * This service manages the complete password reset flow including:
 * - OTP generation and email sending
 * - OTP verification and validation
 * - Password update with security measures
 * - Rate limiting and abuse prevention
 * 
 * Security features:
 * - Rate limiting to prevent spam
 * - Secure OTP generation
 * - Token expiration and attempt limits
 * - Previous token invalidation
 * - Input validation and sanitization
 * 
 * @author KTC Team
 * @version 1.0
 * @since 2025-01-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // Configuration properties with default values
    @Value("${app.password-reset.otp-length:6}")
    private int otpLength;

    @Value("${app.password-reset.otp-expiry-minutes:15}")
    private int otpExpiryMinutes;

    @Value("${app.password-reset.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.password-reset.rate-limit-minutes:5}")
    private int rateLimitMinutes;

    @Value("${app.password-reset.max-requests-per-hour:3}")
    private int maxRequestsPerHour;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Initiates the password reset process by generating and sending an OTP.
     * 
     * @param request the forgot password request containing user email
     * @return response with masked email and expiry information
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessLogicException    if rate limits exceeded
     */
    @Transactional
    public ForgotPasswordResponseDto initiateForgotPassword(ForgotPasswordRequestDto request) {
        log.info("Initiating password reset for email: {}", maskEmailForLog(request.getEmail()));

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new BusinessLogicException("Account is disabled. Please contact support.");
        }

        // Check rate limiting
        checkRateLimit(user.getId());

        // Invalidate any existing active tokens
        LocalDateTime now = LocalDateTime.now();
        int invalidatedTokens = passwordResetTokenRepository.invalidateActiveTokensByUserId(user.getId(), now);
        boolean previousTokensInvalidated = invalidatedTokens > 0;

        if (previousTokensInvalidated) {
            log.info("Invalidated {} active tokens for user: {}", invalidatedTokens, user.getId());
        }

        // Generate unique OTP
        String otpCode = generateUniqueOtp();

        // Create new password reset token
        LocalDateTime expiresAt = now.plusMinutes(otpExpiryMinutes);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(user)
                .otpCode(otpCode)
                .expiresAt(expiresAt)
                .isUsed(false)
                .attempts(0)
                .maxAttempts(maxAttempts)
                .build();

        passwordResetTokenRepository.save(token);

        // Send OTP email
        sendPasswordResetEmail(user, otpCode, expiresAt);

        log.info("Password reset OTP generated and sent for user: {}", user.getId());

        return ForgotPasswordResponseDto.success(
                request.getEmail(),
                expiresAt,
                maxAttempts,
                previousTokensInvalidated);
    }

    /**
     * Confirms the password reset using OTP and updates the user's password.
     * 
     * @param request the reset password confirmation request
     * @throws ResourceNotFoundException if user or token not found
     * @throws ValidationException       if OTP is invalid or passwords don't match
     * @throws BusinessLogicException    if token is expired or max attempts reached
     */
    @Transactional
    public void confirmPasswordReset(ResetPasswordConfirmDto request) {
        log.info("Confirming password reset for email: {}", maskEmailForLog(request.getEmail()));

        // Validate password match
        if (!request.isPasswordMatching()) {
            throw new ValidationException("New password and confirmation password do not match");
        }

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Find valid token
        LocalDateTime now = LocalDateTime.now();
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository
                .findValidTokenByUserIdAndOtpCode(user.getId(), request.getOtpCode(), now);

        if (tokenOpt.isEmpty()) {
            // Check if there's a token with this OTP but it's invalid
            Optional<PasswordResetToken> invalidTokenOpt = passwordResetTokenRepository
                    .findActiveTokenByOtpCode(request.getOtpCode(), now);

            if (invalidTokenOpt.isPresent()) {
                PasswordResetToken invalidToken = invalidTokenOpt.get();

                // Increment attempts if token belongs to this user
                if (invalidToken.getUser().getId().equals(user.getId())) {
                    invalidToken.incrementAttempts();
                    passwordResetTokenRepository.save(invalidToken);

                    int remainingAttempts = invalidToken.getRemainingAttempts();
                    if (remainingAttempts > 0) {
                        throw new ValidationException(
                                String.format("Invalid OTP code. %d attempt(s) remaining.", remainingAttempts));
                    } else {
                        throw new BusinessLogicException(
                                "Maximum verification attempts exceeded. Please request a new OTP.");
                    }
                }
            }

            throw new ValidationException("Invalid or expired OTP code");
        }

        PasswordResetToken token = tokenOpt.get();

        // Double-check token validity (extra security)
        if (!token.isValid()) {
            throw new BusinessLogicException("Token is no longer valid");
        }

        // Update user password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Mark token as used
        token.markAsUsed();
        passwordResetTokenRepository.save(token);

        log.info("Password successfully reset for user: {}", user.getId());
    }

    /**
     * Checks if the user has exceeded the rate limit for password reset requests.
     * 
     * @param userId the user ID to check
     * @throws BusinessLogicException if rate limit exceeded
     */
    private void checkRateLimit(String userId) {
        // Check recent requests in the last hour
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        long recentRequests = passwordResetTokenRepository.countTokensCreatedByUserSince(userId, hourAgo);

        if (recentRequests >= maxRequestsPerHour) {
            log.warn("Rate limit exceeded for user: {} (requests: {})", userId, recentRequests);
            throw new BusinessLogicException(
                    String.format(
                            "Too many password reset requests. Please wait before trying again. Limit: %d per hour.",
                            maxRequestsPerHour));
        }

        // Check if there's a recent request within the rate limit window
        LocalDateTime rateLimitWindow = LocalDateTime.now().minusMinutes(rateLimitMinutes);
        Optional<PasswordResetToken> recentToken = passwordResetTokenRepository.findLatestTokenByUserId(userId);

        if (recentToken.isPresent() && recentToken.get().getCreatedAt().isAfter(rateLimitWindow)) {
            long waitMinutes = rateLimitMinutes -
                    java.time.Duration.between(recentToken.get().getCreatedAt(), LocalDateTime.now()).toMinutes();
            throw new BusinessLogicException(
                    String.format("Please wait %d minute(s) before requesting another OTP.", Math.max(1, waitMinutes)));
        }
    }

    /**
     * Generates a unique OTP code that doesn't exist in the system.
     * 
     * @return unique OTP string
     */
    private String generateUniqueOtp() {
        String otpCode;
        int maxRetries = 10;
        int retries = 0;

        do {
            otpCode = generateOtp();
            retries++;

            if (retries > maxRetries) {
                log.error("Failed to generate unique OTP after {} retries", maxRetries);
                throw new BusinessLogicException("Unable to generate OTP. Please try again.");
            }
        } while (isOtpCodeExists(otpCode));

        return otpCode;
    }

    /**
     * Generates a random OTP code of specified length.
     * 
     * @return OTP string
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(SECURE_RANDOM.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Checks if an OTP code already exists in active tokens.
     * 
     * @param otpCode the OTP code to check
     * @return true if exists, false otherwise
     */
    private boolean isOtpCodeExists(String otpCode) {
        return passwordResetTokenRepository
                .findActiveTokenByOtpCode(otpCode, LocalDateTime.now())
                .isPresent();
    }

    /**
     * Sends password reset email with OTP to the user.
     * 
     * @param user      the user to send email to
     * @param otpCode   the OTP code
     * @param expiresAt when the OTP expires
     */
    private void sendPasswordResetEmail(User user, String otpCode, LocalDateTime expiresAt) {
        try {
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("userName", user.getName());
            templateVariables.put("otpCode", otpCode);
            templateVariables.put("expiryTime", expiresAt.toString());
            templateVariables.put("expiryMinutes", otpExpiryMinutes);

            emailService.sendTemplateEmail(
                    user.getEmail(),
                    "Password Reset - Your OTP Code",
                    "password-reset-template",
                    templateVariables);

            log.info("Password reset email sent to: {}", maskEmailForLog(user.getEmail()));
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", maskEmailForLog(user.getEmail()), e);
            throw new BusinessLogicException("Failed to send password reset email. Please try again.");
        }
    }

    /**
     * Masks email address for logging purposes to protect privacy.
     * 
     * @param email the email to mask
     * @return masked email string
     */
    private String maskEmailForLog(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        String maskedLocal = localPart.length() > 2 ? localPart.substring(0, 2) + "***" : "***";

        return maskedLocal + "@" + domain;
    }

    /**
     * Cleanup method to remove expired tokens (for scheduled execution).
     * Should be called periodically to maintain database performance.
     * 
     * @return number of tokens cleaned up
     */
    @Transactional
    public int cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = passwordResetTokenRepository.deleteExpiredTokens(now);

        // Also cleanup old used tokens (older than 30 days)
        LocalDateTime cutoff = now.minusDays(30);
        int oldUsedCount = passwordResetTokenRepository.deleteOldUsedTokens(cutoff);

        if (deletedCount > 0 || oldUsedCount > 0) {
            log.info("Cleaned up {} expired tokens and {} old used tokens", deletedCount, oldUsedCount);
        }

        return deletedCount + oldUsedCount;
    }
}
