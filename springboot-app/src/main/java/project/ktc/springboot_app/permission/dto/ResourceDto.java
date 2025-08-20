package project.ktc.springboot_app.permission.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resource DTO for hierarchical tree representation with permission assignments
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceDto {

    /**
     * Resource ID
     */
    private String id;

    /**
     * Resource key/name (e.g., "course", "section", "lesson")
     */
    private String key;

    /**
     * Human-readable resource name
     */
    private String name;

    /**
     * Resource description
     */
    private String description;

    /**
     * Resource path or identifier
     */
    private String resourcePath;

    /**
     * Whether the role has this permission directly assigned
     */
    private boolean assigned;

    /**
     * Whether the role inherits this permission from a parent resource
     */
    private boolean inherited;

    /**
     * Whether this resource is active
     */
    private boolean active;

    /**
     * List of child resources (recursive structure)
     */
    @Builder.Default
    private List<ResourceDto> children = List.of();

    /**
     * Parent resource information (optional, for reference)
     */
    private ParentResourceDto parent;

    /**
     * Nested DTO for parent resource information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ParentResourceDto {
        private String id;
        private String key;
        private String name;
    }

    /**
     * Convenience method to check if resource has any permissions (assigned or
     * inherited)
     */
    public boolean hasPermissions() {
        return assigned || inherited;
    }

    /**
     * Convenience method to check if resource has children
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Convenience method to count total children recursively
     */
    public int getTotalChildrenCount() {
        if (children == null || children.isEmpty()) {
            return 0;
        }

        int count = children.size();
        for (ResourceDto child : children) {
            count += child.getTotalChildrenCount();
        }
        return count;
    }

    /**
     * Convenience method to get resource display name
     */
    public String getDisplayName() {
        return name != null ? name : key;
    }
}
