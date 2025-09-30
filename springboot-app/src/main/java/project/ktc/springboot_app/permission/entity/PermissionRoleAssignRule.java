package project.ktc.springboot_app.permission.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.entity.UserRole;

/**
 * Permission Role Assign Rule Entity Defines which permissions can be assigned
 * to which roles Only
 * permissions defined in this table can be assigned to the corresponding role
 */
@Entity
@Table(name = "permission_role_assign_rules", uniqueConstraints = {
		@UniqueConstraint(name = "unique_role_permission", columnNames = { "role_id", "permission_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PermissionRoleAssignRule extends BaseEntity {

	/** Reference to the role that can be assigned this permission */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_permission_role_assign_rule_role"))
	private UserRole role;

	/** Reference to the permission that can be assigned to the role */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "permission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_permission_role_assign_rule_permission"))
	private Permission permission;

	/** Whether this rule is active (permission can be assigned to role) */
	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	/** When this rule was created */
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/** When this rule was last updated */
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	// Convenience methods

	/** Get the role ID */
	public String getRoleId() {
		return role != null ? role.getId() : null;
	}

	/** Get the permission ID */
	public String getPermissionId() {
		return permission != null ? permission.getId() : null;
	}

	/** Check if this assignment rule is active */
	public boolean isRuleActive() {
		return Boolean.TRUE.equals(isActive);
	}

	/** Get role name for convenience */
	public String getRoleName() {
		return role != null ? role.getRole() : null;
	}

	/** Get permission key for convenience */
	public String getPermissionKey() {
		return permission != null ? permission.getPermissionKey() : null;
	}
}
