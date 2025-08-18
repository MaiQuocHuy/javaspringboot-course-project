package project.ktc.springboot_app.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import project.ktc.springboot_app.entity.BaseEntity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Resource Entity for Permission System
 * Represents system resources that can be secured (courses, lessons, reviews,
 * etc.)
 */
@Entity
@Table(name = "resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Resource extends BaseEntity {

    /**
     * Resource name (e.g., "course", "lesson", "review")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Human-readable description of the resource
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Resource path or identifier (e.g., "/courses", "/lessons")
     */
    @Column(name = "resource_path", length = 255)
    private String resourcePath;

    /**
     * Parent resource for hierarchical organization
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_resource_id", foreignKey = @ForeignKey(name = "fk_resources_parent"))
    private Resource parentResource;

    /**
     * Flag to enable/disable resource
     */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Resource creation timestamp
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
     * Convenience method to check if resource is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    /**
     * Override toString to prevent recursive calls
     */
    @Override
    public String toString() {
        return "Resource{" +
                "id='" + getId() + '\'' +
                ", name='" + name + '\'' +
                ", resourcePath='" + resourcePath + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
