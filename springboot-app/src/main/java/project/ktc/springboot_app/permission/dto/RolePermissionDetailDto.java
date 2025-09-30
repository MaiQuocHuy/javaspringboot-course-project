package project.ktc.springboot_app.permission.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for individual permission details within a resource group Used in API 7.7 GET
 * /api/permissions/{role_id}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RolePermissionDetailDto {

  private String permissionKey;

  private String description;

  private String resource;

  private String action;

  private String filterType;

  private Boolean isAssigned;

  private Boolean canAssignToRole;

  private Boolean isRestricted;

  private List<String> allowedRoles;
}
