package project.ktc.springboot_app.auth.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RegisterApplicationDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;
import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.auth.enums.UserRoleEnum;
import project.ktc.springboot_app.entity.RefreshToken;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.instructor_application.entity.InstructorApplication;
import project.ktc.springboot_app.instructor_application.repositories.InstructorApplicationRepository;
import project.ktc.springboot_app.refresh_token.repositories.RefreshTokenRepository;
import project.ktc.springboot_app.upload.service.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.service.FileValidationService;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;
import project.ktc.springboot_app.utils.JwtTokenProvider;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import project.ktc.springboot_app.auth.interfaces.AuthService;

@Service
@Slf4j
@RequiredArgsConstructor

public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final InstructorApplicationRepository instructorApplicationRepository;
    private final CloudinaryServiceImp cloudinaryService;
    private final FileValidationService fileValidationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> registerUser(RegisterUserDto registerUserDto) {

        // Validation checks
        if (registerUserDto == null) {
            return ApiResponseUtil.badRequest("Registration data cannot be null");
        }
        if (registerUserDto.getEmail() == null || registerUserDto.getEmail().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Email cannot be null or empty");
        }
        if (registerUserDto.getPassword() == null || registerUserDto.getPassword().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Password cannot be null or empty");
        }
        if (registerUserDto.getName() == null || registerUserDto.getName().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Name cannot be null or empty");
        }

        // Check if email already exists
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            return ApiResponseUtil.conflict("Email already exists");
        }

        try {
            // First, find or create the role
            UserRole.RoleType roleType = UserRole.RoleType.valueOf(registerUserDto.getRole().name());
            Optional<UserRole> existingRoleOpt = userRoleRepository.findByRole(roleType);
            UserRole userRole;

            if (existingRoleOpt.isPresent()) {
                userRole = existingRoleOpt.get();
            } else {
                userRole = UserRole.builder()
                        .role(roleType)
                        .build();
                userRole = userRoleRepository.save(userRole);
            }

            // Create user with role reference
            User user = User.builder()
                    .name(registerUserDto.getName())
                    .email(registerUserDto.getEmail())
                    .password(passwordEncoder.encode(registerUserDto.getPassword()))
                    .role(userRole)
                    .build();

            // Save user
            User savedUser = userRepository.save(user);

            log.info("User registered successfully: {}", savedUser.getEmail());
            return ApiResponseUtil.created("User registered successfully");

        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            // return ApiResponseUtil.internalServerError("Registration failed. Please try
            // again later.");
            throw new RuntimeException("Registration failed. Root cause: " + e.getMessage(), e);

        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> registerApplication(
            RegisterApplicationDto registerApplicationDto,
            MultipartFile certificate,
            MultipartFile cv,
            MultipartFile other) {

        log.info("Starting registration with application for role: {}", registerApplicationDto.getRole());

        // Validation checks
        if (registerApplicationDto.getEmail() == null || registerApplicationDto.getEmail().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Email cannot be null or empty");
        }
        if (registerApplicationDto.getPassword() == null || registerApplicationDto.getPassword().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Password cannot be null or empty");
        }
        if (registerApplicationDto.getName() == null || registerApplicationDto.getName().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Name cannot be null or empty");
        }
        if (registerApplicationDto.getRole() == null) {
            return ApiResponseUtil.badRequest("Role cannot be null");
        }

        // Check if email already exists
        if (userRepository.findByEmail(registerApplicationDto.getEmail()).isPresent()) {
            return ApiResponseUtil.conflict("Email already exists");
        }

        // Validate instructor application requirements
        if (registerApplicationDto.getRole() == UserRoleEnum.INSTRUCTOR) {
            // Validate required documents for instructor
            if (certificate == null || certificate.isEmpty()) {
                return ApiResponseUtil.badRequest("Certificate file is required for instructor application");
            }
            if (cv == null || cv.isEmpty()) {
                return ApiResponseUtil.badRequest("CV file is required for instructor application");
            }
            if (registerApplicationDto.getPortfolio() == null
                    || registerApplicationDto.getPortfolio().trim().isEmpty()) {
                return ApiResponseUtil.badRequest("Portfolio URL is required for instructor application");
            }

            // Validate portfolio URL format
            try {
                validatePortfolioUrl(registerApplicationDto.getPortfolio());
            } catch (Exception e) {
                return ApiResponseUtil.badRequest(e.getMessage());
            }
        }

        try {
            // First, find or create the role
            UserRole.RoleType roleType = UserRole.RoleType.valueOf(registerApplicationDto.getRole().name());
            Optional<UserRole> existingRoleOpt = userRoleRepository.findByRole(roleType);
            UserRole userRole;

            if (existingRoleOpt.isPresent()) {
                userRole = existingRoleOpt.get();
            } else {
                userRole = UserRole.builder()
                        .role(roleType)
                        .build();
                userRole = userRoleRepository.save(userRole);
            }

            // Create user with role reference
            User user = User.builder()
                    .name(registerApplicationDto.getName())
                    .email(registerApplicationDto.getEmail())
                    .password(passwordEncoder.encode(registerApplicationDto.getPassword()))
                    .role(userRole)
                    .build();

            // Save user
            User savedUser = userRepository.save(user);

            // If instructor role, create instructor application
            if (registerApplicationDto.getRole() == UserRoleEnum.INSTRUCTOR) {
                try {
                    createInstructorApplication(savedUser, certificate, cv, other,
                            registerApplicationDto.getPortfolio());
                } catch (Exception e) {
                    log.error("Failed to create instructor application for user: {}, error: {}", savedUser.getId(),
                            e.getMessage());
                    // We still consider registration successful even if application fails
                    // as the user account was created successfully
                }
            }

            log.info("User registered successfully with role {}: {}", registerApplicationDto.getRole().name(),
                    savedUser.getEmail());
            return ApiResponseUtil.created("Registration successful");

        } catch (Exception e) {
            log.error("Error during user registration: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Registration failed. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> loginUser(LoginUserDto dto) {
        // Validation checks
        if (dto == null) {
            return ApiResponseUtil.badRequest("Login data cannot be null");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Email cannot be null or empty");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Password cannot be null or empty");
        }

        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));

            Optional<User> user = userRepository.findByEmail(dto.getEmail());
            if (user.isEmpty()) {
                return ApiResponseUtil.unauthorized("Invalid email or password");
            }
            log.info("User found: {}", user.get().getEmail());
            User foundUser = user.get();
            if (!foundUser.getIsActive()) {
                return ApiResponseUtil.unauthorized("User account is inactive");
            }
            String accessToken = jwtTokenProvider.generateAccessToken(foundUser);

            // Generate and save refresh token
            String refreshTokenStr = UUID.randomUUID().toString();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(foundUser)
                    .token(refreshTokenStr)
                    .expiresAt(LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault()))
                    .createdAt(LocalDateTime.now())
                    .build();
            refreshTokenRepository.save(refreshToken);

            UserResponseDto userResponseDto = new UserResponseDto(foundUser);
            userResponseDto.setRole(null);
            Map<String, Object> loginResponse = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshTokenStr,
                    "user", userResponseDto);

            log.info("User logged in successfully: {}", foundUser.getEmail());
            return ApiResponseUtil.success(loginResponse, "Login successful");

        } catch (DisabledException e) {
            log.warn("Account is blocked: {}", dto.getEmail());
            return ApiResponseUtil.unauthorized("Your account has been blocked. Please contact the administrator.");
        } catch (BadCredentialsException e) {
            log.warn("Invalid email or password: {}", dto.getEmail());
            return ApiResponseUtil.unauthorized("Email or password is incorrect.");
        } catch (AuthenticationException e) {
            log.warn("Unknown authentication error: {}", dto.getEmail(), e);
            return ApiResponseUtil.unauthorized("Authentication failed.");
        } catch (Exception e) {
            log.error("System error occurred while logging in user: {}", dto.getEmail(), e);
            return ApiResponseUtil.internalServerError("An error occurred. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshAccessToken(String refreshToken) {
        // Validation check
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ApiResponseUtil.badRequest("Refresh token cannot be null or empty");
        }

        try {
            RefreshToken existingRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                    .orElse(null);

            if (existingRefreshToken == null) {
                return ApiResponseUtil.unauthorized("Invalid refresh token");
            }

            if (existingRefreshToken.getIsRevoked()) {
                return ApiResponseUtil.unauthorized("Refresh token has been revoked");
            }

            // Check if token is expired
            if (existingRefreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
                return ApiResponseUtil.unauthorized("Refresh token has expired");
            }

            User user = existingRefreshToken.getUser();
            String newAccessToken = jwtTokenProvider.generateAccessToken(user);

            Map<String, Object> refreshResponse = Map.of(
                    "accessToken", newAccessToken,
                    "refreshToken", existingRefreshToken.getToken());

            log.info("Access token refreshed successfully for user: {}", user.getEmail());
            return ApiResponseUtil.success(refreshResponse, "Token refreshed successfully");

        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Token refresh failed. Please login again.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(String oldPassword, String newPassword) {
        // Validation checks
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            return ApiResponseUtil.badRequest("Email cannot be null or empty");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ApiResponseUtil.badRequest("New password cannot be null or empty");
        }

        if (newPassword.equals(oldPassword)) {
            return ApiResponseUtil.badRequest("New password cannot be the same as the old password");
        }

        try {

            String email = SecurityUtil.getCurrentUserEmail();
            if (email == null) {
                return ApiResponseUtil.unauthorized("User is not authenticated");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ApiResponseUtil.notFound("User with this email does not exist");
            }

            User user = userOpt.get();
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ApiResponseUtil.unauthorized("Old password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            log.info("Password reset successfully for user: {}", email);
            return ApiResponseUtil.success(Map.of("message", "Password reset successfully"),
                    "Password reset successful");

        } catch (Exception e) {
            log.error("Error during password reset: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Password reset failed. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> logout(String refreshToken) {
        log.info("Processing logout request");

        try {
            // Validate refresh token is provided
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                log.warn("Logout attempt with empty refresh token");
                return ApiResponseUtil.badRequest("Refresh token is required");
            }

            // Find the refresh token in database
            Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken.trim());

            if (tokenOpt.isEmpty()) {
                log.warn("Logout attempt with invalid refresh token: {}", refreshToken);
                return ApiResponseUtil.badRequest("Invalid refresh token");
            }

            RefreshToken token = tokenOpt.get();

            // Check if token is already revoked
            if (Boolean.TRUE.equals(token.getIsRevoked())) {
                log.warn("Logout attempt with already revoked token: {}", refreshToken);
                return ApiResponseUtil.badRequest("Invalid refresh token");
            }

            // Check if token is expired
            if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.warn("Logout attempt with expired token: {}", refreshToken);
                return ApiResponseUtil.badRequest("Invalid refresh token");
            }

            // Revoke the refresh token
            token.setIsRevoked(true);
            refreshTokenRepository.save(token);

            log.info("Successfully logged out user with token: {}",
                    refreshToken.substring(0, Math.min(20, refreshToken.length())) + "...");
            return ApiResponseUtil.success("Logout successful");

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Logout failed. Please try again later.");
        }
    }

    /**
     * Validate portfolio URL format
     */
    private void validatePortfolioUrl(String portfolio) {
        if (portfolio == null || portfolio.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio URL is required");
        }

        // Basic URL validation - allows various portfolio platforms
        String trimmedUrl = portfolio.trim().toLowerCase();
        if (!trimmedUrl.startsWith("http://") && !trimmedUrl.startsWith("https://") && !trimmedUrl.startsWith("www.")) {
            throw new IllegalArgumentException(
                    "Portfolio must be a valid URL starting with http://, https://, or www.");
        }
    }

    /**
     * Create instructor application with uploaded documents
     */
    private void createInstructorApplication(User user, MultipartFile certificate, MultipartFile cv,
            MultipartFile other, String portfolio) {
        try {
            log.info("Creating instructor application for user: {}", user.getId());

            // Check if user already has an application (shouldn't happen in normal flow)
            Optional<InstructorApplication> existingApplication = instructorApplicationRepository
                    .findByUserId(user.getId());
            if (existingApplication.isPresent()) {
                log.warn("User {} already has an instructor application", user.getId());
                return;
            }

            // Validate and upload documents
            Map<String, String> documentUrls = uploadDocumentsToCloudinary(certificate, cv, other);

            // Add portfolio URL to documents
            documentUrls.put("portfolio", portfolio);

            // Convert documents map to JSON string
            String documentsJson = objectMapper.writeValueAsString(documentUrls);

            // Create new instructor application
            InstructorApplication application = new InstructorApplication();
            application.setUser(user);
            application.setDocuments(documentsJson);
            application.setStatus(InstructorApplication.ApplicationStatus.PENDING);
            application.setSubmittedAt(LocalDateTime.now());

            instructorApplicationRepository.save(application);
            log.info("Instructor application created successfully for user: {}", user.getId());

        } catch (Exception e) {
            log.error("Failed to create instructor application for user: {}, error: {}", user.getId(), e.getMessage(),
                    e);
            throw new RuntimeException("Failed to create instructor application", e);
        }
    }

    /**
     * Upload documents to Cloudinary and return URLs
     */
    private Map<String, String> uploadDocumentsToCloudinary(
            MultipartFile certificate, MultipartFile cv, MultipartFile other) {

        Map<String, String> documentUrls = new HashMap<>();

        try {
            // Upload certificate
            fileValidationService.validateDocumentFile(certificate);
            project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto certificateResult = cloudinaryService
                    .uploadDocument(certificate);
            documentUrls.put("certificate", certificateResult.getUrl());
            log.info("Certificate uploaded successfully: {}", certificateResult.getUrl());

            // Upload CV
            fileValidationService.validateDocumentFile(cv);
            project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto cvResult = cloudinaryService
                    .uploadDocument(cv);
            documentUrls.put("cv", cvResult.getUrl());
            log.info("CV uploaded successfully: {}", cvResult.getUrl());

            // Upload optional other document
            if (other != null && !other.isEmpty()) {
                fileValidationService.validateDocumentFile(other);
                project.ktc.springboot_app.upload.dto.DocumentUploadResponseDto otherResult = cloudinaryService
                        .uploadDocument(other);
                documentUrls.put("other", otherResult.getUrl());
                log.info("Additional document uploaded successfully: {}", otherResult.getUrl());
            }

            return documentUrls;

        } catch (Exception e) {
            log.error("Failed to upload documents to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload documents to cloud storage", e);
        }
    }
}