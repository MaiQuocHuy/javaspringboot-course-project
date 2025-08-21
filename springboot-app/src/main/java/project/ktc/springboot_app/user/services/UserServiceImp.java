package project.ktc.springboot_app.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.auth.enums.UserRoleEnum;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.entity.UserRole;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.services.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.services.FileValidationService;
import project.ktc.springboot_app.user.dto.CreateUserDto;
import project.ktc.springboot_app.user.dto.UpdateUserDto;
import project.ktc.springboot_app.user.dto.UpdateUserRoleDto;
import project.ktc.springboot_app.user.dto.UpdateUserStatusDto;
import project.ktc.springboot_app.user.dto.AdminUserPageResponseDto;
import project.ktc.springboot_app.user.interfaces.UserService;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.user_role.repositories.UserRoleRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import java.util.Optional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final CloudinaryServiceImp cloudinaryService;
    private final FileValidationService fileValidationService;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

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

            // Find the user by email (with role information)
            User user = userRepository.findByEmailWithRoles(userEmail)
                    .orElse(null);

            if (user == null) {
                log.warn("User not found in database: {}", userEmail);
                return ApiResponseUtil.notFound("User not found");
            }

            // Create user response DTO (role information comes from the user entity)
            UserResponseDto userResponseDto = new UserResponseDto(user);

            log.info("Profile retrieved successfully for user: {} with role: {}", userEmail,
                    user.getRole() != null ? user.getRole().getRole() : "No role");
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
            users.forEach(user -> log.debug("User ID: {}, Email: {}, Role: {}", user.getId(), user.getEmail(),
                    user.getRole() != null ? user.getRole().getRole() : "No role"));

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

    public ResponseEntity<ApiResponse<project.ktc.springboot_app.user.dto.AdminUserDetailResponseDto>> getAdminUserById(
            String id) {
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

            // Create admin response DTO with additional info
            project.ktc.springboot_app.user.dto.AdminUserDetailResponseDto adminUserDetailDto = project.ktc.springboot_app.user.dto.AdminUserDetailResponseDto
                    .builder()
                    .build();

            // Copy basic user information from UserResponseDto
            UserResponseDto baseUser = new UserResponseDto(user);
            adminUserDetailDto.setId(baseUser.getId());
            adminUserDetailDto.setEmail(baseUser.getEmail());
            adminUserDetailDto.setName(baseUser.getName());
            adminUserDetailDto.setRole(baseUser.getRole());
            adminUserDetailDto.setThumbnailUrl(baseUser.getThumbnailUrl());
            adminUserDetailDto.setBio(baseUser.getBio());
            adminUserDetailDto.setIsActive(baseUser.getIsActive());

            // Get enrolled courses with payment info and study time
            List<Object[]> enrolledCoursesData = userRepository.findEnrolledCoursesWithPaymentByUserId(id);
            List<project.ktc.springboot_app.user.dto.AdminUserDetailResponseDto.EnrolledCourseDto> enrolledCourses = enrolledCoursesData
                    .stream()
                    .map(data -> project.ktc.springboot_app.user.dto.AdminUserDetailResponseDto.EnrolledCourseDto
                            .builder()
                            .courseId((String) data[0])
                            .courseTitle((String) data[1])
                            .instructorName((String) data[2])
                            .enrolledAt(data[3] != null ? data[3].toString() : "")
                            .completionStatus(data[4] != null ? data[4].toString() : "IN_PROGRESS")
                            .paidAmount((java.math.BigDecimal) data[5])
                            .totalTimeStudying(data[6] != null ? ((Number) data[6]).longValue() : 0L) // in seconds
                            .build())
                    .collect(Collectors.toList());

            // Get total payments
            java.math.BigDecimal totalPayments = userRepository.getTotalPaymentsByUserId(id);

            // Get total study time (in seconds)
            Long totalStudyTimeSeconds = userRepository.getTotalStudyTimeByUserId(id);

            // Set additional data
            adminUserDetailDto.setEnrolledCourses(enrolledCourses);
            adminUserDetailDto.setTotalPayments(totalPayments);
            adminUserDetailDto.setTotalStudyTimeSeconds(totalStudyTimeSeconds);

            log.info("Admin user detail retrieved for ID: {}", id);
            return ApiResponseUtil.success(adminUserDetailDto, "Admin user detail retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving admin user detail by ID: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve admin user detail. Please try again later.");
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

            // Find or create the new role
            Optional<UserRole> newRoleOpt = userRoleRepository.findByRole(role.getRole().toUpperCase().trim());
            UserRole newRole = null;

            if (newRoleOpt.isPresent()) {
                newRole = newRoleOpt.get();
            } else {
                return ApiResponseUtil.badRequest("Invalid role.");
            }
            // Update user's role reference
            UserRole oldRole = user.getRole();
            if (!oldRole.getId().equals(newRole.getId())) {
                user.setRole(newRole);
                userRepository.save(user);
                log.info("User role updated: {} -> {} for user {}", oldRole.getRole(), newRole.getRole(), user.getId());
            } else {
                log.info("User role unchanged for user {}", user.getId());
            }

            // Prepare response
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
            user.setIsActive(status.getIsActive());
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

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(CreateUserDto createUserDto) {
        try {
            // Check if email already exists
            Optional<User> existingUser = userRepository.findByEmail(createUserDto.getEmail());
            if (existingUser.isPresent()) {
                log.warn("Attempt to create user with existing email: {}", createUserDto.getEmail());
                return ApiResponseUtil.badRequest("Email already exists");
            }

            // Set default role to STUDENT if not provided
            UserRoleEnum roleEnum = createUserDto.getRole() != null ? createUserDto.getRole() : UserRoleEnum.STUDENT;

            // Find the role entity
            UserRole userRole = userRoleRepository.findByRole(roleEnum.name())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleEnum.name()));

            // Create new user
            User newUser = User.builder()
                    .name(createUserDto.getName())
                    .email(createUserDto.getEmail())
                    .password(passwordEncoder.encode(createUserDto.getPassword()))
                    .bio(createUserDto.getBio() != null ? createUserDto.getBio() : "")
                    .isActive(createUserDto.getIsActive() != null ? createUserDto.getIsActive() : true)
                    .thumbnailUrl("")
                    .thumbnailId("")
                    .role(userRole)
                    .build();

            // Save the user
            User savedUser = userRepository.save(newUser);

            // Create response DTO
            UserResponseDto userResponseDto = new UserResponseDto(savedUser);

            log.info("User created successfully with ID: {} and email: {}", savedUser.getId(), savedUser.getEmail());
            return ApiResponseUtil.created(userResponseDto, "User created successfully");

        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to create user. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUserProfile(String id, UpdateUserDto updateUserDto) {
        try {
            // Find the user by ID
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", id);
                return ApiResponseUtil.notFound("User not found");
            }

            // Validate input
            if (updateUserDto.getName() == null || updateUserDto.getName().trim().isEmpty()) {
                return ApiResponseUtil.badRequest("Name cannot be null or empty");
            }

            if (updateUserDto.getBio() != null && updateUserDto.getBio().length() > 500) {
                return ApiResponseUtil.badRequest("Bio cannot exceed 500 characters");
            }

            // Update user details
            user.setName(updateUserDto.getName().trim());
            if (updateUserDto.getBio() != null) {
                user.setBio(updateUserDto.getBio().trim());
            }

            // Save updated user
            User updatedUser = userRepository.save(user);

            // Create response DTO
            UserResponseDto userResponseDto = new UserResponseDto(updatedUser);

            log.info("User profile updated successfully for ID: {}", id);
            return ApiResponseUtil.success(userResponseDto, "User profile updated successfully");

        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update user profile. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<AdminUserPageResponseDto>> getUsersWithPagination(
            String search, String role, Boolean isActive,
            int page, int size, String sort) {
        try {
            // Create pageable object with sorting
            Sort sortObj = Sort.by(Sort.Direction.ASC, "name"); // default sort
            if (sort != null && !sort.isEmpty()) {
                String[] sortParams = sort.split(",");
                if (sortParams.length == 2) {
                    Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc")
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
                    sortObj = Sort.by(direction, sortParams[0]);
                }
            }

            Pageable pageable = PageRequest.of(page, size, sortObj);

            // Get users with filters (you'll need to implement this in repository)
            Page<User> usersPage;

            if ((search == null || search.trim().isEmpty()) &&
                    (role == null || role.trim().isEmpty()) &&
                    isActive == null) {
                // No filters - get all users
                usersPage = userRepository.findAll(pageable);
            } else {
                // Apply filters - you'll need to create this method in UserRepository
                usersPage = userRepository.findUsersWithFilters(search, role, isActive, pageable);
            }

            // Convert to DTOs
            List<UserResponseDto> userResponseDtos = usersPage.getContent().stream()
                    .map(UserResponseDto::new)
                    .collect(Collectors.toList());

            // Create page response
            AdminUserPageResponseDto pageResponse = AdminUserPageResponseDto.builder()
                    .users(userResponseDtos)
                    .totalElements(usersPage.getTotalElements())
                    .totalPages(usersPage.getTotalPages())
                    .currentPage(usersPage.getNumber())
                    .pageSize(usersPage.getSize())
                    .hasNext(usersPage.hasNext())
                    .hasPrevious(usersPage.hasPrevious())
                    .build();

            log.info("Users retrieved with pagination. Page: {}, Size: {}, Total: {}",
                    page, size, usersPage.getTotalElements());
            return ApiResponseUtil.success(pageResponse, "Users retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving users with pagination: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve users. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteUser(String id) {
        try {
            // Find the user by ID
            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                log.warn("User not found with ID: {}", id);
                return ApiResponseUtil.notFound("User not found");
            }

            // Get current user to prevent self-deletion
            String currentUserId = SecurityUtil.getCurrentUserId();
            if (currentUserId != null && currentUserId.equals(id)) {
                log.warn("User attempting to delete themselves: {}", id);
                return ApiResponseUtil.badRequest("Cannot delete your own account");
            }

            // Delete thumbnail from Cloudinary if exists
            if (user.getThumbnailId() != null && !user.getThumbnailId().isEmpty()) {
                try {
                    cloudinaryService.deleteImage(user.getThumbnailId());
                    log.info("Thumbnail deleted for user: {}", id);
                } catch (Exception e) {
                    log.warn("Failed to delete thumbnail for user {}: {}", id, e.getMessage());
                    // Continue with user deletion even if thumbnail deletion fails
                }
            }

            // Delete the user
            userRepository.delete(user);

            log.info("User deleted successfully with ID: {}", id);
            return ApiResponseUtil.success(null, "User deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to delete user. Please try again later.");
        }
    }

}
