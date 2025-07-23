package project.ktc.springboot_app.auth.interfaces;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;
import project.ktc.springboot_app.common.dto.ApiResponse;

public interface AuthService {
        ResponseEntity<ApiResponse<Void>> registerUser(RegisterUserDto registerUserDto);

        ResponseEntity<ApiResponse<Map<String, Object>>> loginUser(LoginUserDto loginUserDto);

        ResponseEntity<ApiResponse<Map<String, Object>>> refreshAccessToken(String refreshToken);

        ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(String email, String newPassword);
}
