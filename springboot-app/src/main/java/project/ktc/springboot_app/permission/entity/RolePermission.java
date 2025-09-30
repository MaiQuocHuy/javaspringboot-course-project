package project.ktc.springboot_app.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.entity.UserRole;

/** Role Permission Entity for Role-Based Access Control Maps permissions to user roles */
@Entity
@Table(
    name = "role_permissions",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "unique_role_permission",
          columnNames = {"role_id", "permission_id"})
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RolePermission extends BaseEntity {

  /** Reference to the user role */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "role_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_role_permissions_role"))
  private UserRole role;

  /** Reference to the permission */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "permission_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_role_permissions_permission"))
  private Permission permission;

  /** Reference to the filter type (new schema support) */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "filter_type_id",
      nullable = false,
      foreignKey = @ForeignKey(name = "fk_role_permissions_filter_type"))
  private FilterType filterType;

  /** User who granted this permission (audit trail) */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "granted_by", foreignKey = @ForeignKey(name = "fk_role_permissions_granter"))
  private User grantedBy;

  /** Flag to enable/disable role-permission */
  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  /** Convenience method to check if role-permission is active */
  public boolean isActive() {
    return Boolean.TRUE.equals(isActive) && (permission != null && permission.isActive());
  }

  /** Convenience method to check if this role-permission matches the given role and permission */
  public boolean matches(String roleType, String permissionKey) {
    return role != null
        && permission != null
        && role.getRole().equals(roleType)
        && permission.getPermissionKey().equals(permissionKey)
        && isActive();
  }

  /** Convenience method to get role type */
  public String getRoleType() {
    return role != null ? role.getRole() : null;
  }

  /** Convenience method to get permission key */
  public String getPermissionKey() {
    return permission != null ? permission.getPermissionKey() : null;
  }

  /** Convenience method to get granter name */
  public String getGranterName() {
    return grantedBy != null ? grantedBy.getName() : null;
  }

  /** Check if this role permission has ALL access (filter-type-001) */
  public boolean hasAllAccess() {
    return filterType != null && filterType.isAllAccess();
  }

  /** Check if this role permission has OWN access (filter-type-002) */
  public boolean hasOwnAccess() {
    return filterType != null && filterType.isOwnAccess();
  }

  /** Get effective filter type from the associated filter type */
  public FilterType.EffectiveFilterType getEffectiveFilterType() {
    return filterType != null
        ? filterType.toEffectiveFilterType()
        : FilterType.EffectiveFilterType.DENIED;
  }

  /** Override toString to prevent recursive calls */
  @Override
  public String toString() {
    return "RolePermission{"
        + "id='"
        + getId()
        + '\''
        + ", roleId='"
        + (role != null ? role.getId() : null)
        + '\''
        + ", permissionId='"
        + (permission != null ? permission.getId() : null)
        + '\''
        + ", filterTypeId='"
        + (filterType != null ? filterType.getId() : null)
        + '\''
        + ", grantedById='"
        + (grantedBy != null ? grantedBy.getId() : null)
        + '\''
        + ", isActive="
        + isActive
        + ", createdAt="
        + getCreatedAt()
        + '}';
  }
}
