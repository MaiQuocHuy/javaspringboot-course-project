package project.ktc.springboot_app.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for assigning role to user */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserRoleRequest {

	@NotBlank(message = "User ID is required")
	private String userId;

	@NotBlank(message = "Role ID is required")
	private String roleId;

	private String reason; // Optional reason for role assignment
}
