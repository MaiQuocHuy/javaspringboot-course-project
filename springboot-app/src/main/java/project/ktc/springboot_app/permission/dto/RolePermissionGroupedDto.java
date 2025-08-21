package project.ktc.springboot_app.permission.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for grouped permissions by resource for a specific role
 * Used in API 7.7 GET /api/permissions/{role_id}
 */
public class RolePermissionGroupedDto {

    @JsonProperty("resources")
    private Map<String, List<RolePermissionDetailDto>> resources;

    public RolePermissionGroupedDto() {
    }

    public RolePermissionGroupedDto(Map<String, List<RolePermissionDetailDto>> resources) {
        this.resources = resources;
    }

    public Map<String, List<RolePermissionDetailDto>> getResources() {
        return resources;
    }

    public void setResources(Map<String, List<RolePermissionDetailDto>> resources) {
        this.resources = resources;
    }
}
