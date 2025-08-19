package project.ktc.springboot_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Role Filter Rule Entity for scope-based permission filtering
 * Defines what data scope a role can access for a specific permission
 */
@Entity
@Table(name = "role_filter_rules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_permission_filter", columnNames = { "role_id", "permission_key",
                "filter_type" })
}, indexes = {
        @Index(name = "idx_role_filter_rules_role_id", columnList = "role_id"),
        @Index(name = "idx_role_filter_rules_permission", columnList = "permission_key"),
        @Index(name = "idx_role_filter_rules_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleFilterRule {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "role_id", length = 36, nullable = false)
    private String roleId;

    @Column(name = "permission_key", length = 100, nullable = false)
    private String permissionKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type", length = 20, nullable = false)
    private FilterType filterType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Foreign key relationship to UserRole
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_role_filter_rules_role_id"), insertable = false, updatable = false)
    private UserRole userRole;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Filter Type Enum defines the scope of data access
     */
    public enum FilterType {
        /**
         * ALL - No filter, access to all data (typically for ADMIN)
         * Query: no WHERE clause restrictions
         */
        ALL,

        /**
         * OWN - Access only to user's own data
         * Query: WHERE created_by = :userId OR instructor_id = :userId
         */
        OWN,

        /**
         * PUBLISHED_ONLY - Access only to published/approved data
         * Query: WHERE is_published = true AND is_approved = true
         */
        PUBLISHED_ONLY
    }
}
