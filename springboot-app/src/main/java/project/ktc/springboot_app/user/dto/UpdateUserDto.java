package project.ktc.springboot_app.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating user profile information")
public class UpdateUserDto {

  @Schema(description = "User's full name", example = "John Doe", required = true)
  @NotBlank(message = "Name cannot be blank")
  @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
  private String name;

  @Schema(
      description = "User's biography or description",
      example = "Software developer with 5 years of experience",
      required = false)
  @Size(max = 500, message = "Bio cannot exceed 500 characters")
  private String bio;
}
