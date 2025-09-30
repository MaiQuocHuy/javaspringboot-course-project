package project.ktc.springboot_app.permission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for available permissions with constraint information */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePermissionDto {

	/** Permission ID */
	private String id;

	/** Permission key (format: resource:action) */
	private String permissionKey;

	/** Human-readable description of the permission */
	private String description;

	/** Resource name */
	private String resource;

	/** Action name */
	private String action;

	/**
	 * Whether this permission can be assigned to the specified role Based on
	 * permission_role_assign_rules table
	 */
	private Boolean canAssignToRole;

	/**
	 * Whether this permission has assignment restrictions True if there are any
	 * rules in
	 * permission_role_assign_rules, false otherwise
	 */
	private Boolean isRestricted;

	/**
	 * List of role names that are allowed to assign this permission Based on
	 * permission_role_assign_rules where is_active = true
	 */
	private java.util.List<String> allowedRoles;
}
