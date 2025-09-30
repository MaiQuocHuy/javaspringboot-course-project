package project.ktc.springboot_app.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for assigning permissions to a role */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for assigning permissions to a role")
public class AssignPermissionsRequest {

	@NotEmpty(message = "Permission IDs list cannot be empty")
	@Schema(description = "List of permission IDs to assign", example = "[\"perm-123\", \"perm-456\"]", required = true)
	private List<String> permissionIds;

	@Builder.Default
	@Schema(description = "Filter type for permissions", example = "ALL", allowableValues = { "ALL", "OWN" })
	private String filterType = "ALL";
}
