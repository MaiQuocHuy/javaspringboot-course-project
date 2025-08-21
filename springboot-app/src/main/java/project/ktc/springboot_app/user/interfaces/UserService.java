package project.ktc.springboot_app.user.interfaces;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.user.dto.AdminUserPageResponseDto;
import project.ktc.springboot_app.user.dto.CreateUserDto;
import project.ktc.springboot_app.user.dto.UpdateUserDto;
import project.ktc.springboot_app.user.dto.UpdateUserRoleDto;
import project.ktc.springboot_app.user.dto.UpdateUserStatusDto;
import project.ktc.springboot_app.user.dto.AdminCreateUserDto;
import project.ktc.springboot_app.auth.dto.UserResponseDto;

public interface UserService {
    ResponseEntity<ApiResponse<UserResponseDto>> getProfile();

    ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(UpdateUserDto userDto, MultipartFile thumbnailFile);

    ResponseEntity<ApiResponse<List<UserResponseDto>>> getUsers();

    ResponseEntity<ApiResponse<UserResponseDto>> getUserById(String id);

    ResponseEntity<ApiResponse<UserResponseDto>> updateUserRole(String id, UpdateUserRoleDto role);

    ResponseEntity<ApiResponse<UserResponseDto>> updateUserStatus(String id, UpdateUserStatusDto status);

    ResponseEntity<ApiResponse<UserResponseDto>> createUser(CreateUserDto createUserDto);

    ResponseEntity<ApiResponse<UserResponseDto>> updateUserProfile(String id, UpdateUserDto updateUserDto);

    ResponseEntity<ApiResponse<AdminUserPageResponseDto>> getUsersWithPagination(
            String search, String role, Boolean isActive,
            int page, int size, String sort);

    ResponseEntity<ApiResponse<Void>> deleteUser(String id);

    ResponseEntity<ApiResponse<UserResponseDto>> createAdminUser(AdminCreateUserDto createUserDto);
}
