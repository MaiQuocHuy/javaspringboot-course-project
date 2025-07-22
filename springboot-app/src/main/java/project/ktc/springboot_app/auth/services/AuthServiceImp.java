package project.ktc.springboot_app.auth.services;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;
import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.entity.RefreshToken;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.refresh_token.repositories.RefreshTokenRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.JwtTokenProvider;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;
import project.ktc.springboot_app.auth.interfaces.AuthService;

@Service
@Slf4j
@RequiredArgsConstructor

public class AuthServiceImp implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

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
            // Create user with empty roles list
            User user = User.builder()
                    .name(registerUserDto.getName())
                    .email(registerUserDto.getEmail())
                    .password(passwordEncoder.encode(registerUserDto.getPassword()))
                    .roles(new ArrayList<>()) // Explicitly initialize the roles list
                    .build();

            String selectedRole = registerUserDto.getRole().name(); // -> "STUDENT" or "INSTRUCTOR"

            // Create role and set the relationship properly
            UserRole role = UserRole.builder()
                    .user(user) // Set the user reference
                    .role(selectedRole)
                    .build();

            // Add the role to the user's roles list (bidirectional relationship)
            user.getRoles().add(role);

            // Save user with cascade - this should save both user and role in one
            // transaction
            User savedUser = userRepository.save(user);

            log.info("User registered successfully: {}", savedUser.getEmail());
            return ApiResponseUtil.created("User registered successfully");

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

            User user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String accessToken = jwtTokenProvider.generateAccessToken(user);

            // Generate and save refresh token
            String refreshTokenStr = UUID.randomUUID().toString();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_MONTH, 30);
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(refreshTokenStr)
                    .expiresAt(LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.systemDefault()))
                    .createdAt(LocalDateTime.now())
                    .build();
            refreshTokenRepository.save(refreshToken);

            UserResponseDto userResponseDto = new UserResponseDto(user);
            Map<String, Object> loginResponse = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshTokenStr,
                    "user", userResponseDto);

            log.info("User logged in successfully: {}", user.getEmail());
            return ApiResponseUtil.success(loginResponse, "Login successful");

        } catch (org.springframework.security.core.AuthenticationException e) {
            log.warn("Authentication failed for email: {}", dto.getEmail());
            return ApiResponseUtil.unauthorized("Invalid email or password");
        } catch (Exception e) {
            log.error("Error during login for email: {}", dto.getEmail(), e);
            return ApiResponseUtil.internalServerError("Login failed. Please try again later.");
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
}