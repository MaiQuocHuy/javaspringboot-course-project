package project.ktc.springboot_app.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.auth.enums.UserRoleEnum;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.service.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.service.FileValidationService;
import project.ktc.springboot_app.user.dto.UpdateUserDto;
import project.ktc.springboot_app.user.dto.UpdateUserRoleDto;
import project.ktc.springboot_app.user.dto.UpdateUserStatusDto;
import project.ktc.springboot_app.user.interfaces.UserService;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import java.util.Optional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryServiceImp cloudinaryService;
    private final FileValidationService fileValidationService;
    private final UserRoleRepository userRoleRepository;

    private final ObjectMapper objectMapper;

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

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(UpdateUserDto userDto,
            MultipartFile thumbnailFile) {
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

            // Find the user by email
            User user = userRepository.findByEmail(userEmail).orElse(null);

            if (user == null) {
                log.warn("User not found in database: {}", userEmail);
                return ApiResponseUtil.notFound("User not found");
            }

            // Validate userDto fields
            if (userDto.getName() == null || userDto.getName().trim().isEmpty()) {
                log.warn("Invalid name provided for user: {}", userEmail);
                return ApiResponseUtil.badRequest("Name cannot be null or empty");
            }

            // Validate bio length if provided
            if (userDto.getBio() != null && userDto.getBio().length() > 500) {
                log.warn("Bio exceeds maximum length for user: {}", userEmail);
                return ApiResponseUtil.badRequest("Bio cannot exceed 500 characters");
            }

            // Update user details
            user.setName(userDto.getName().trim());

            // Update bio if provided
            if (userDto.getBio() != null) {
                user.setBio(userDto.getBio().trim());
            }

            // Handle thumbnail upload if file is provided
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                try {
                    // Validate the uploaded file
                    fileValidationService.validateImageFile(thumbnailFile);

                    // Delete old image if it exists
                    if (user.getThumbnailId() != null && !user.getThumbnailId().isEmpty()) {
                        boolean deleted = cloudinaryService.deleteImage(user.getThumbnailId());
                        log.info("Old thumbnail deletion result for user {}: {}", userEmail, deleted);
                    }

                    // Upload new image to Cloudinary
                    ImageUploadResponseDto uploadResult = cloudinaryService.uploadImage(thumbnailFile);

                    // Update user thumbnail information
                    user.setThumbnailUrl(uploadResult.getUrl());
                    user.setThumbnailId(uploadResult.getPublicId());

                    log.info("New thumbnail uploaded for user {}: {}", userEmail, uploadResult.getPublicId());

                } catch (Exception e) {
                    log.error("Error uploading thumbnail for user {}: {}", userEmail, e.getMessage(), e);
                    return ApiResponseUtil.badRequest("Failed to upload thumbnail: " + e.getMessage());
                }
            }

            // Save updated user
            User updatedUser = userRepository.save(user);

            // Create response DTO
            UserResponseDto updatedUserResponseDto = new UserResponseDto(updatedUser);

            log.info("Profile updated successfully for user: {} with thumbnail: {}", userEmail,
                    thumbnailFile != null ? "uploaded" : "not provided");
            return ApiResponseUtil.success(updatedUserResponseDto, "Profile updated successfully");

        } catch (Exception e) {
            log.error("Error updating profile with thumbnail for user: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update profile. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getUsers() {
        try {
            // Get the current authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Retrieve all users from the repository
            List<User> users = userRepository.findAll();
            users.forEach(user -> log.debug("User ID: {}, Email: {}, Roles: {}", user.getId(), user.getEmail(),
                    user.getRoles()));

            if (users.isEmpty()) {
                log.info("No users found in the database");
                return ApiResponseUtil.notFound("No users found");
            }

            // remove own id from the list
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId == null) {
                log.warn("Current user ID is null, cannot filter out own user");
                return ApiResponseUtil.internalServerError("Failed to retrieve users. Please try again later.");
            }
            users.removeIf(user -> user.getId().equals(currentUserId));

            // Convert users to UserResponseDto
            List<UserResponseDto> userResponseDtos = users.stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());

            return ApiResponseUtil.success(userResponseDtos, "Users retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving users: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve users. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(String id) {
        try {
            // Get the current authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Find the user by ID
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", id);
                return ApiResponseUtil.notFound("User not found");
            }

            // Create response DTO
            UserResponseDto userResponseDto = new UserResponseDto(user);

            log.info("User found with ID: {}", id);
            return ApiResponseUtil.success(userResponseDto, "User retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving user by ID: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve user. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(String id, UpdateUserRoleDto role) {
        try {
            // Get the current authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Find the user by ID
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", id);
                return ApiResponseUtil.notFound("User not found");
            }

            UserRoleEnum roleEnum;
            try {
                roleEnum = UserRoleEnum.valueOf(role.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ApiResponseUtil.badRequest("Invalid role");
            }

            Optional<UserRole> existingUserRoleOpt = userRoleRepository.findByUserId(user.getId());
            if (existingUserRoleOpt.isEmpty()) {
                return ApiResponseUtil.notFound("User role not found");
            }

            UserRole existingUserRole = existingUserRoleOpt.get();
            existingUserRole.setRole(roleEnum.name());
            userRoleRepository.save(existingUserRole); // Lưu lại

            // Cập nhật response
            user.setRoles(List.of(existingUserRole)); // để DTO có roles
            UserResponseDto userResponseDto = new UserResponseDto(user);

            return ApiResponseUtil.success(userResponseDto, "User role updated successfully");
        } catch (Exception e) {
            log.error("Error updating user role: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update user role. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserStatus(String id, UpdateUserStatusDto status) {
        try {
            // Get the current authenticated user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authenticated user found in security context");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Find the user by ID
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", id);
                return ApiResponseUtil.notFound("User not found");
            }

            // Update user status
            user.setIsActive(status.getStatus().toLowerCase().equals("active"));
            User updatedUser = userRepository.save(user);

            // Create response DTO
            UserResponseDto userResponseDto = new UserResponseDto(updatedUser);

            log.info("User status updated successfully for ID: {}", id);
            return ApiResponseUtil.success(userResponseDto, "User status updated successfully");

        } catch (Exception e) {
            log.error("Error updating user status: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update user status. Please try again later.");
        }
    }

}
