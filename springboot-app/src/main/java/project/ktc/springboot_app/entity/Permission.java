package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Permission Entity for Role-Based Access Control
 * Represents a permission as a combination of resource and action
 */
@Entity
@Table(name = "permissions", uniqueConstraints = {
        @UniqueConstraint(name = "unique_resource_action", columnNames = { "resource_id", "action_id" })
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Permission extends BaseEntity {

    /**
     * Reference to the resource
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_permissions_resource"))
    private Resource resource;

    /**
     * Reference to the action
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false, foreignKey = @ForeignKey(name = "fk_permissions_action"))
    private Action action;

    /**
     * Auto-generated permission key (format: resource:action)
     */
    @Column(name = "permission_key", nullable = false, unique = true, length = 100)
    private String permissionKey;

    /**
     * Human-readable description of the permission
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Flag to enable/disable permission
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Permission creation timestamp
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Auto-generate permission key before persisting
     */
    @PrePersist
    @PreUpdate
    private void generatePermissionKey() {
        if (resource != null && action != null) {
            this.permissionKey = resource.getName() + ":" + action.getName();
        }
    }

    /**
     * Convenience method to check if permission is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive) &&
                (resource != null && resource.isActive()) &&
                (action != null && action.isActive());
    }

    /**
     * Convenience method to check if this permission matches the given resource and
     * action
     */
    public boolean matches(String resourceName, String actionName) {
        return resource != null && action != null &&
                resource.getName().equals(resourceName) &&
                action.getName().equals(actionName) &&
                isActive();
    }

    /**
     * Convenience method to get resource name
     */
    public String getResourceName() {
        return resource != null ? resource.getName() : null;
    }

    /**
     * Convenience method to get action name
     */
    public String getActionName() {
        return action != null ? action.getName() : null;
    }

    /**
     * Override toString to prevent recursive calls
     */
    @Override
    public String toString() {
        return "Permission{" +
                "id='" + getId() + '\'' +
                ", permissionKey='" + permissionKey + '\'' +
                ", resourceId='" + (resource != null ? resource.getId() : null) + '\'' +
                ", actionId='" + (action != null ? action.getId() : null) + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
