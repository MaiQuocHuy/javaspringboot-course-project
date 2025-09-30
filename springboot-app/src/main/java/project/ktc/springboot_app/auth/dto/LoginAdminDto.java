package project.ktc.springboot_app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login credentials for user authentication")
public class LoginAdminDto {

	@Schema(description = "User's email address (Select role below to auto-fill)", example = "alice@example.com", allowableValues = {
			"alice@example.com" })
	@NotBlank(message = "Email is required")
	@Email(message = "Email format is invalid")
	private String email;

	@Schema(description = "User's password (Select role below to auto-fill)", example = "alice123", allowableValues = {
			"alice123" })
	@NotBlank(message = "Password is required")
	private String password;
}
