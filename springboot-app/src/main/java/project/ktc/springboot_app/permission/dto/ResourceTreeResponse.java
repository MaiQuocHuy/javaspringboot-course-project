package project.ktc.springboot_app.permission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for resource tree with permissions for a specific role */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceTreeResponse {

  /** Role ID for which the resource tree is generated */
  private String roleId;

  /** Role name for reference */
  private String roleName;

  /** Hierarchical list of resources with permission assignments */
  @Builder.Default private List<ResourceDto> resources = List.of();

  /** Last updated timestamp of the role's permissions */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
  private LocalDateTime timestamp;

  /** Total number of resources in the tree */
  private int totalResources;

  /** Number of resources with assigned permissions */
  private int assignedResources;

  /** Number of resources with inherited permissions */
  private int inheritedResources;

  /** Summary statistics for the role's resource tree */
  private TreeStatistics statistics;

  /** Nested DTO for tree statistics */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class TreeStatistics {

    /** Total number of root resources */
    private int rootResourceCount;

    /** Maximum depth of the resource tree */
    private int maxDepth;

    /** Number of resources with direct permission assignments */
    private int directAssignments;

    /** Number of resources with inherited permissions */
    private int inheritedAssignments;

    /** Percentage of resources with any permissions */
    private double coveragePercentage;
  }

  /** Convenience method to check if the tree has any resources */
  public boolean hasResources() {
    return resources != null && !resources.isEmpty();
  }

  /** Convenience method to count total resources recursively */
  public int calculateTotalResources() {
    if (resources == null || resources.isEmpty()) {
      return 0;
    }

    int count = resources.size();
    for (ResourceDto resource : resources) {
      count += resource.getTotalChildrenCount();
    }
    return count;
  }

  /** Convenience method to calculate coverage percentage */
  public double calculateCoveragePercentage() {
    int total = calculateTotalResources();
    if (total == 0) {
      return 0.0;
    }

    return ((double) (assignedResources + inheritedResources) / total) * 100.0;
  }
}
