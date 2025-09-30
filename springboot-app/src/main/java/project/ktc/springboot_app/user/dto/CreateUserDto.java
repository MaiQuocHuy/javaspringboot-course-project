package project.ktc.springboot_app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.auth.enums.UserRoleEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a new user by admin")
public class CreateUserDto {

  @NotBlank(message = "Name is required")
  @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
  @Schema(description = "User's full name", example = "John Doe", required = true)
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email format is invalid")
  @Schema(description = "User's email address", example = "john.doe@example.com", required = true)
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
  @Schema(description = "User's password", example = "password123", required = true)
  private String password;

  @Schema(
      description = "User's role (defaults to STUDENT if not provided)",
      example = "STUDENT",
      allowableValues = {"STUDENT", "INSTRUCTOR", "ADMIN"})
  private UserRoleEnum role;

  @Size(max = 500, message = "Bio must not exceed 500 characters")
  @Schema(
      description = "User's bio/description",
      example = "Software developer with 5 years of experience")
  private String bio;

  @Schema(description = "User's active status (defaults to true if not provided)", example = "true")
  private Boolean isActive;
}
