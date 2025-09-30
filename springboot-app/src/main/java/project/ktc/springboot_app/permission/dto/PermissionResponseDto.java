package project.ktc.springboot_app.permission.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response DTO for Permission with resource and action details */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponseDto {

  /** Permission key (format: resource:action) */
  private String key;

  /** Human-readable description of the permission */
  private String description;

  /** Resource information */
  private ResourceDto resource;

  /** Action information */
  private ActionDto action;

  /** Whether the permission is active at system level */
  private Boolean isActive;

  /**
   * Whether the permission is active for this specific role (from role_permission.is_active) Only
   * populated when retrieving permissions for a specific role
   */
  private Boolean roleActive;

  /**
   * Whether the permission is active at permission level (from permission.is_active) Only populated
   * when retrieving permissions for a specific role
   */
  private Boolean permissionActive;

  /** Permission creation timestamp */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime createdAt;

  /** Last modification timestamp */
  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime updatedAt;

  /** Nested DTO for Resource information */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ResourceDto {
    /** Resource key/name */
    private String key;

    /** Human-readable resource name */
    private String name;

    /** Whether the resource is active */
    private Boolean isActive;
  }

  /** Nested DTO for Action information */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ActionDto {
    /** Action key/name */
    private String key;

    /** Human-readable action name */
    private String name;

    /** Whether the action is active */
    private Boolean isActive;
  }
}
