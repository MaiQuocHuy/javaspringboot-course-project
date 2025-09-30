package project.ktc.springboot_app.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for updating a role */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating a role")
public class UpdateRoleRequest {

	@NotBlank(message = "Role name is required")
	@Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
	@Schema(description = "Role name", example = "MANAGER", required = true)
	private String name;

	@Schema(description = "Role description", example = "Manager role with team management permissions")
	private String description;
}
