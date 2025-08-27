package project.ktc.springboot_app.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating a new role")
public class CreateRoleRequest {

    @Schema(description = "Role name", example = "MANAGER", required = true)
    @NotBlank(message = "Role name cannot be blank")
    @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z_]+$", message = "Role name must contain only uppercase letters and underscores")
    private String name;

    @Schema(description = "Role description", example = "Manager role with administrative privileges")
    @Size(max = 500, message = "Role description cannot exceed 500 characters")
    private String description;
}
