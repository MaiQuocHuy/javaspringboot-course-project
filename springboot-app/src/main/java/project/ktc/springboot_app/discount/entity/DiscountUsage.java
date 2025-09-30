package project.ktc.springboot_app.discount.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;

/**
 * DiscountUsage entity to track when discounts are used Records the actual
 * usage of discount codes
 * by users for specific courses
 */
@Entity
@Table(name = "discount_usages", indexes = {
		@Index(name = "idx_discount_usage_user_discount", columnList = "user_id, discount_id"),
		@Index(name = "idx_discount_usage_course", columnList = "course_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountUsage {

	@Id
	@Column(length = 36, updatable = false, nullable = false)
	@Builder.Default
	private String id = UUID.randomUUID().toString();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discount_id", nullable = false)
	private Discount discount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	/**
	 * The user who referred this purchase (only for REFERRAL type discounts) This
	 * is the owner of the
	 * referral code used
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referred_by_user_id")
	private User referredByUser;

	@Column(name = "used_at", nullable = false, updatable = false)
	@Builder.Default
	private LocalDateTime usedAt = LocalDateTime.now();

	/** The discount percentage that was applied (snapshot at time of use) */
	@Column(name = "discount_percent", precision = 4, scale = 2, nullable = false)
	private BigDecimal discountPercent;

	/**
	 * The actual discount amount in currency (calculated from course price and
	 * discount percent)
	 */
	@Column(name = "discount_amount", precision = 10, scale = 2, nullable = false)
	private BigDecimal discountAmount;
}
