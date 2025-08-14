package project.ktc.springboot_app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for confirming password reset with OTP.
 * 
 * This DTO is used when a user submits the OTP code received via email
 * along with their new password to complete the password reset process.
 * 
 * @author KTC Team
 * @version 1.0
 * @since 2025-01-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordConfirmDto {

    /**
     * The email address of the user resetting their password.
     * Must match the email used in the forgot password request.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /**
     * The 6-digit OTP code sent to the user's email.
     * Must be exactly 6 digits for security and consistency.
     */
    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP code must be exactly 6 digits")
    private String otpCode;

    /**
     * The new password to set for the user account.
     * Must meet minimum security requirements.
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String newPassword;

    /**
     * Confirmation of the new password.
     * Must match the newPassword field exactly.
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    /**
     * Validates that the new password and confirmation password match.
     * 
     * @return true if passwords match, false otherwise
     */
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
