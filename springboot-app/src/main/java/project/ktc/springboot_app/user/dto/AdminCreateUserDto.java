package project.ktc.springboot_app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a new user by admin with comprehensive validation")
public class AdminCreateUserDto {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N}\\s_.-]+$", message = "Username can contain letters, numbers, spaces, underscores, dots, and hyphens")
    @Schema(description = "User's unique username", example = "Phuong Ngoc", required = true)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    @Schema(description = "User's password with strength requirements", example = "SecurePass123!", required = true)
    private String password;

    @NotBlank(message = "Role is required")
    @Schema(description = "User's role in the system", example = "STUDENT", required = true)
    private String role;
}
