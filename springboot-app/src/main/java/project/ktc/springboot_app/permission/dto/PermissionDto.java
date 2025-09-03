package project.ktc.springboot_app.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * DTO for permission information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permission information DTO")
public class PermissionDto {

    @Schema(description = "Permission ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Permission key", example = "course:read")
    private String permissionKey;

    @Schema(description = "Resource name", example = "course")
    private String resourceName;

    @Schema(description = "Action name", example = "read")
    private String actionName;

    @Schema(description = "Permission description", example = "Read course information")
    private String description;

    @Schema(description = "Whether permission is active", example = "true")
    private Boolean isActive;

    @Schema(description = "Permission creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Permission last update timestamp")
    private LocalDateTime updatedAt;
}
