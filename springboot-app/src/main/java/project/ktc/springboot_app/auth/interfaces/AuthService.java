package project.ktc.springboot_app.auth.interfaces;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import project.ktc.springboot_app.auth.dto.GoogleLoginDto;
import project.ktc.springboot_app.auth.dto.LoginUserDto;
import project.ktc.springboot_app.auth.dto.RegisterApplicationDto;
import project.ktc.springboot_app.auth.dto.RegisterUserDto;
import project.ktc.springboot_app.common.dto.ApiResponse;

public interface AuthService {
        ResponseEntity<ApiResponse<Void>> registerUser(RegisterUserDto registerUserDto);

        ResponseEntity<ApiResponse<Void>> registerApplication(
                        RegisterApplicationDto registerApplicationDto,
                        MultipartFile certificate,
                        MultipartFile cv,
                        MultipartFile other);

        ResponseEntity<ApiResponse<Map<String, Object>>> loginUser(LoginUserDto loginUserDto);

        ResponseEntity<ApiResponse<Map<String, Object>>> refreshAccessToken(String refreshToken);

        ResponseEntity<ApiResponse<Map<String, String>>> resetPassword(String email, String newPassword);

        ResponseEntity<ApiResponse<Void>> logout(String refreshToken);

        ResponseEntity<ApiResponse<Map<String, Object>>> googleLogin(GoogleLoginDto googleLoginDto);
}
