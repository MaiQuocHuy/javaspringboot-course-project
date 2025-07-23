package project.ktc.springboot_app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDto {

    @Schema(description = "User's old password", example = "oldPassword123", required = true)
    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @Schema(description = "User's new password", example = "newPassword123", required = true)
    @NotBlank(message = "New password is required")
    private String newPassword;

}
