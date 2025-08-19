package project.ktc.springboot_app.filter_rule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.entity.BaseEntity;
import project.ktc.springboot_app.permission.entity.RolePermission;

import java.time.LocalDateTime;

/**
 * Role Filter Rule Entity for scope-based permission filtering
 * Defines what data scope can be accessed for a specific role permission
 * 
 * NORMALIZED DESIGN:
 * - Links to role_permission table via role_permission_id foreign key
 * - Eliminates redundant role_id + permission_key columns
 * - Supports 1:N relationship (one permission can have multiple filter rules)
 */
@Entity
@Table(name = "role_filter_rules", indexes = {
        @Index(name = "idx_role_filter_rules_permission", columnList = "role_permission_id"),
        @Index(name = "idx_role_filter_rules_type", columnList = "filter_type"),
        @Index(name = "idx_role_filter_rules_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RoleFilterRule extends BaseEntity {

    /**
     * Foreign key to role_permission table
     * Links to the specific role-permission combination this filter applies to
     */
    @Column(name = "role_permission_id", length = 36, nullable = false)
    private String rolePermissionId;

    /**
     * Type of data filtering to apply
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type", length = 20, nullable = false)
    private FilterType filterType;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Many-to-One relationship with RolePermission
     * This provides access to role and permission details through the normalized
     * structure
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_permission_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_role_filter_rules_permission"), insertable = false, updatable = false)
    private RolePermission rolePermission;

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
