package project.ktc.springboot_app.permission.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for role operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for role operations")
public class RoleResponseDto {

    @Schema(description = "Role ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Role name", example = "MANAGER")
    private String name;

    @Schema(description = "Role description", example = "Manager role with administrative privileges")
    private String description;

    @Schema(description = "Role creation timestamp", example = "2025-08-18T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Role last update timestamp", example = "2025-08-18T10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
