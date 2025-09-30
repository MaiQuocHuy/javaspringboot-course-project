package project.ktc.springboot_app.permission.entity;

import jakarta.persistence.*;
import lombok.*;
import project.ktc.springboot_app.entity.BaseEntity;

/**
 * Filter Type Entity for the new permission system Represents different data access scopes (ALL,
 * OWN)
 *
 * <p>Note: The ID field directly contains the filter type key (e.g., "filter-type-001")
 */
@Entity
@Table(name = "filter_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FilterType extends BaseEntity {

  /** Human-readable name (e.g., "ALL", "OWN") */
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  /** Description of what this filter type does */
  @Column(name = "description", length = 255)
  private String description;

  /** Convenience method to check if this is the ALL filter type */
  public boolean isAllAccess() {
    return "filter-type-001".equals(getId()) || "ALL".equalsIgnoreCase(name);
  }

  /** Convenience method to check if this is the OWN filter type */
  public boolean isOwnAccess() {
    return "filter-type-002".equals(getId()) || "OWN".equalsIgnoreCase(name);
  }

  /** Convert to enum for compatibility with existing code */
  public EffectiveFilterType toEffectiveFilterType() {
    if (isAllAccess()) {
      return EffectiveFilterType.ALL;
    } else if (isOwnAccess()) {
      return EffectiveFilterType.OWN;
    } else {
      return EffectiveFilterType.DENIED;
    }
  }

  /** Enum representing effective filter types for authorization logic */
  public enum EffectiveFilterType {
    ALL, // No restrictions - access to all data
    OWN, // Access only to user's own data
    DENIED // No access
  }

  @Override
  public String toString() {
    return "FilterType{" + "id='" + getId() + '\'' + ", name='" + name + '\'' + '}';
  }
}
