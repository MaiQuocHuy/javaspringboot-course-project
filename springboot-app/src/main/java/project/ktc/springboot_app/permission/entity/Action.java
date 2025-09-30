package project.ktc.springboot_app.permission.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import project.ktc.springboot_app.entity.BaseEntity;

/**
 * Action Entity for Permission System Represents actions that can be performed on resources
 * (CREATE, READ, UPDATE, DELETE, etc.)
 */
@Entity
@Table(name = "actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Action extends BaseEntity {

  /** Action name (e.g., "CREATE", "READ", "UPDATE", "DELETE", "APPROVE") */
  @Column(name = "name", nullable = false, length = 50, unique = true)
  private String name;

  /** Human-readable description of the action */
  @Column(name = "description", length = 255)
  private String description;

  /** Action type category */
  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false)
  private ActionType actionType;

  /** Flag to enable/disable action */
  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  /** Action creation timestamp */
  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /** Last modification timestamp */
  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  /** Action type enumeration */
  public enum ActionType {
    CRUD, // Basic CRUD operations
    BUSINESS, // Business-specific operations (APPROVE, REJECT, etc.)
    CUSTOM // Custom application-specific operations
  }

  /** Convenience method to check if action is active */
  public boolean isActive() {
    return Boolean.TRUE.equals(isActive);
  }

  /** Convenience method to check if this is a CRUD action */
  public boolean isCrudAction() {
    return ActionType.CRUD.equals(actionType);
  }

  /** Convenience method to check if this is a business action */
  public boolean isBusinessAction() {
    return ActionType.BUSINESS.equals(actionType);
  }

  /** Override toString to prevent recursive calls */
  @Override
  public String toString() {
    return "Action{"
        + "id='"
        + getId()
        + '\''
        + ", name='"
        + name
        + '\''
        + ", actionType="
        + actionType
        + ", isActive="
        + isActive
        + '}';
  }
}
