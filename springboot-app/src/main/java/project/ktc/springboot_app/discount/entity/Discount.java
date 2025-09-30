package project.ktc.springboot_app.discount.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.entity.BaseEntity;

/**
 * Discount entity representing discount codes and their properties Supports both GENERAL and
 * REFERRAL discount types
 */
@Entity
@Table(name = "discounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Discount extends BaseEntity {

  @Column(name = "code", length = 50, nullable = false, unique = true)
  private String code;

  @Column(name = "discount_percent", precision = 4, scale = 2, nullable = false)
  private BigDecimal discountPercent;

  @Column(name = "description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", length = 50, nullable = false)
  private DiscountType type;

  /**
   * For GENERAL discounts: this should be null For REFERRAL discounts: this is required and
   * represents the user who created the referral code
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_user_id")
  private User ownerUser;

  @Column(name = "start_date", nullable = true)
  private LocalDateTime startDate;

  @Column(name = "end_date", nullable = true)
  private LocalDateTime endDate;

  @Column(name = "usage_limit")
  private Integer usageLimit;

  @Column(name = "per_user_limit")
  private Integer perUserLimit;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = true;

  // Relationships
  @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<DiscountUsage> discountUsages = new java.util.ArrayList<>();

  /** Check if discount is currently valid based on dates and active status */
  public boolean isCurrentlyValid() {
    LocalDateTime now = LocalDateTime.now();
    return isActive
        && (startDate == null || !now.isBefore(startDate))
        && (endDate == null || !now.isAfter(endDate));
  }

  /** Check if discount has usage remaining */
  public boolean hasUsageRemaining() {
    if (usageLimit == null) {
      return true; // No limit
    }
    return discountUsages.size() < usageLimit;
  }

  /** Check if user can still use this discount */
  public boolean canUserUse(String userId) {
    if (perUserLimit == null) {
      return true; // No per-user limit
    }

    long userUsageCount =
        discountUsages.stream().filter(usage -> usage.getUser().getId().equals(userId)).count();

    return userUsageCount < perUserLimit;
  }
}
