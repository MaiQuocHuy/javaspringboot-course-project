package project.ktc.springboot_app.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for permission update operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionUpdateResponse {

    /**
     * The ID of the role that was updated
     */
    private String roleId;

    /**
     * List of permissions that were updated with their new status
     */
    private List<UpdatedPermissionDto> updatedPermissions;

    /**
     * Inner DTO for updated permission items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatedPermissionDto {

        /**
         * Permission key in format "resource:action" (e.g., "course:CREATE")
         */
        private String key;

        /**
         * The new active status for this permission in the role
         */
        private Boolean isActive;

        /**
         * The filter type applied (e.g., ALL / OWN) if any
         */
        private String filterType;
    }
}
