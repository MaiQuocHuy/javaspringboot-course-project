package project.ktc.springboot_app.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.auth.enums.UserRoleEnum;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.user.interfaces.UserService;
import project.ktc.springboot_app.user.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> getProfile() {
        try {
            // Get the current authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Get the username (email) from the authentication object
            String userEmail = authentication.getName();

            if (userEmail == null || userEmail.equals("anonymousUser")) {
                log.warn("Anonymous user detected in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Extract roles from authentication authorities
            List<UserRoleEnum> roles = authentication.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", "")) // Remove ROLE_ prefix if present
                    .map(UserRoleEnum::valueOf)
                    .collect(Collectors.toList());

            // Find the user by email (without roles since we get them from auth)
            User user = userRepository.findByEmail(userEmail)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found in database: {}", userEmail);
                return ApiResponseUtil.notFound("User not found");
            }

            // Create user response DTO with roles from authentication
            UserResponseDto userResponseDto = new UserResponseDto(user);
            userResponseDto.setRoles(roles);

            log.info("Profile retrieved successfully for user: {} with roles: {}", userEmail, roles);
            return ApiResponseUtil.success(userResponseDto, "Profile retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving user profile: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve profile. Please try again later.");
        }
    }
}
