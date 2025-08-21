package project.ktc.springboot_app.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request DTO for updating permissions assigned to a role
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateRequest {

    /**
     * List of permissions to update for the role
     */
    @NotNull(message = "Permissions list cannot be null")
    @NotEmpty(message = "Permissions list cannot be empty")
    @Valid
    private List<PermissionUpdateItemDto> permissions;

    /**
     * Inner DTO for individual permission update items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionUpdateItemDto {

        /**
         * Permission key in format "resource:action" (e.g., "course:CREATE")
         */
        @NotNull(message = "Permission key cannot be null")
        @NotEmpty(message = "Permission key cannot be empty")
        private String key;

        /**
         * Filter type ID for this permission (e.g., "filter-type-001" for ALL,
         * "filter-type-002" for OWN)
         * If null, defaults to ALL access
         */
        private String filterType;
    }
}
