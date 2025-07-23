package project.ktc.springboot_app.user.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.user.dto.UpdateUserDto;
import project.ktc.springboot_app.auth.dto.UserResponseDto;

public interface UserService {
    ResponseEntity<ApiResponse<UserResponseDto>> getProfile();

    ResponseEntity<ApiResponse<UserResponseDto>> updateProfile(UpdateUserDto userDto, MultipartFile thumbnailFile);

    ResponseEntity<ApiResponse<UserResponseDto>> updateProfileWithJson(String userJson, MultipartFile thumbnailFile);
}
