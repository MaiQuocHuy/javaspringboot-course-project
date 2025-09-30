package project.ktc.springboot_app.permission.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request DTO for removing permissions from a role */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for removing permissions from a role")
public class RemovePermissionsRequest {

	@NotEmpty(message = "Permission IDs list cannot be empty")
	@Schema(description = "List of permission IDs to remove", example = "[\"perm-123\", \"perm-456\"]", required = true)
	private List<String> permissionIds;
}
