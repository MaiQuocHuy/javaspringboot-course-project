package project.ktc.springboot_app.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login by Google account")
public class GoogleLoginDto {

	@Schema(description = "Google email")
	@NotBlank(message = "Email is required")
	@Email(message = "Email format is invalid")
	private String email;

	@Schema(description = "Google account name")
	@NotBlank(message = "Name is required")
	private String name;
}
