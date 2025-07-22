package project.ktc.springboot_app.user.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.auth.dto.UserResponseDto;

public interface UserService {
    ResponseEntity<ApiResponse<UserResponseDto>> getProfile();
}
