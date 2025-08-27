package project.ktc.springboot_app.permission.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for role with permissions operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for role with permissions operations")
public class RoleWithPermissionsDto {

    @Schema(description = "Role ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Role name", example = "ADMIN")
    private String name;

    @Schema(description = "Role description", example = "Administrator role with full system access")
    private String description;

    @Schema(description = "Total number of permissions assigned to this role", example = "12")
    private Integer totalPermission;

    @Schema(description = "List of permissions assigned to this role")
    private List<PermissionSummaryDto> permissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Permission summary for role response")
    public static class PermissionSummaryDto {

        @Schema(description = "Permission ID", example = "perm-123e4567-e89b-12d3-a456-426614174000")
        private String id;

        @Schema(description = "Permission name/key", example = "course:create")
        private String name;

        @Schema(description = "Filter type", example = "ALL")
        private String filterType;
    }
}
