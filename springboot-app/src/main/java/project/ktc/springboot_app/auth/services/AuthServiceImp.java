package project.ktc.springboot_app.auth.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.entity.RefreshToken;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.refresh_token.repositories.RefreshTokenRepository;
import project.ktc.springboot_app.utils.JwtTokenProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;
import project.ktc.springboot_app.auth.interfaces.AuthService;
import project.ktc.springboot_app.auth.repositories.UserRepository;

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
    public void registerUser(RegisterUserDto registerUserDto) {
        log.debug("Starting registration process for: {}", registerUserDto != null ? registerUserDto.getEmail() : "null");

        if (registerUserDto == null) {
            throw new IllegalArgumentException("Registration data cannot be null");
        }
        if (registerUserDto.getEmail() == null || registerUserDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (registerUserDto.getPassword() == null || registerUserDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (registerUserDto.getName() == null || registerUserDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        log.debug("Checking if email already exists: {}", registerUserDto.getEmail());
        if (userRepository.findByEmail(registerUserDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        log.debug("Creating user with email: {}", registerUserDto.getEmail());
        // Create user with empty roles list
        User user = User.builder()
                .name(registerUserDto.getName())
                .email(registerUserDto.getEmail())
                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                .roles(new ArrayList<>())  // Explicitly initialize the roles list
                .build();

        log.debug("Creating role for user");
        // Create role and set the relationship properly
        UserRole role = UserRole.builder()
                .user(user)  // Set the user reference
                .role("STUDENT")
                .build();
        
        log.debug("Adding role to user's roles list");
        // Add the role to the user's roles list (bidirectional relationship)
        user.getRoles().add(role);

        log.debug("Saving user with role using cascade");
        // Save user with cascade - this should save both user and role in one transaction
        User savedUser = userRepository.save(user);
        
        log.debug("Registration completed successfully for: {} with ID: {}", registerUserDto.getEmail(), savedUser.getId());
    }

    @Override
    @Transactional
    public Map<String, String> loginUser(LoginUserDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Login data cannot be null");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

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

        return Map.of("accessToken", accessToken, "refreshToken", refreshTokenStr);
    }
}