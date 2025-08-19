package project.ktc.springboot_app.filter_rule.enums;

import lombok.Getter;

/**
 * Effective Filter Enum with defined priority for conflict resolution
 * Higher priority values override lower priority values
 */
@Getter
public enum EffectiveFilter {
    /**
     * DENIED - No access (default when no matching permission)
     * Priority: 0 (lowest)
     */
    DENIED(0),

    /**
     * PUBLISHED_ONLY - Access only to published/approved data
     * Query: WHERE is_published = true AND is_approved = true
     * Priority: 1
     */
    PUBLISHED_ONLY(1),

    /**
     * OWN - Access only to user's own data
     * Query: WHERE created_by = :userId OR instructor_id = :userId
     * Priority: 2
     */
    OWN(2),

    /**
     * ALL - No filter, access to all data (typically for ADMIN)
     * Query: no WHERE clause restrictions
     * Priority: 3 (highest)
     */
    ALL(3);

    private final int priority;

    EffectiveFilter(int priority) {
        this.priority = priority;
    }

    /**
     * Compare two filters and return the one with higher priority
     *
     * @param other another filter to compare
     * @return the filter with higher priority
     */
    public EffectiveFilter combineWith(EffectiveFilter other) {
        return this.priority >= other.priority ? this : other;
    }

    /**
     * Convert FilterType to EffectiveFilter
     *
     * @param filterType the filter type from RoleFilterRule
     * @return corresponding EffectiveFilter
     */
    public static EffectiveFilter fromFilterType(
            project.ktc.springboot_app.filter_rule.entity.RoleFilterRule.FilterType filterType) {
        if (filterType == null) {
            return DENIED;
        }

        return switch (filterType) {
            case ALL -> ALL;
            case OWN -> OWN;
            case PUBLISHED_ONLY -> PUBLISHED_ONLY;
        };
    }
}
