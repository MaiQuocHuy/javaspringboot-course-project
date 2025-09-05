package project.ktc.springboot_app.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * DTO for user permissions response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPermissionsDto {

    /**
     * User basic information
     */
    private String userId;
    private String email;
    private String name;

    /**
     * Role information
     */
    private RoleInfoDto role;

    /**
     * All permission keys the user has access to
     */
    private Set<String> permissions;

    /**
     * Detailed permissions with filter types
     */
    private List<PermissionDetailDto> detailedPermissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleInfoDto {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PermissionDetailDto {
        private String permissionKey;
        private String description;
        private String resource;
        private String action;
        private String filterType;
        private boolean canAccessAll;
        private boolean canAccessOwn;
    }
}
