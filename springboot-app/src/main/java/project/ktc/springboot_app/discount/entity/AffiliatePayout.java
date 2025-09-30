package project.ktc.springboot_app.discount.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.discount.enums.PayoutStatus;
import project.ktc.springboot_app.entity.BaseEntity;

/**
 * AffiliatePayout entity to track commission payouts for referral discounts
 * Records payouts to
 * users who referred others through their discount codes
 */
@Entity
@Table(name = "affiliate_payouts", indexes = {
		@Index(name = "idx_affiliate_payout_user_status", columnList = "referred_by_user_id, payout_status"),
		@Index(name = "idx_affiliate_payout_course", columnList = "course_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliatePayout extends BaseEntity {

	/** The user who gets the commission (owner of the referral code) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "referred_by_user_id", nullable = false)
	private User referredByUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	/**
	 * Link to the discount usage that triggered this payout Can be null if payout
	 * is created
	 * independently
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discount_usage_id")
	private DiscountUsage discountUsage;

	/** Commission percentage applied (snapshot at time of creation) */
	@Column(name = "commission_percent", precision = 4, scale = 2, nullable = false)
	private BigDecimal commissionPercent;

	/** Actual commission amount to be paid */
	@Column(name = "commission_amount", precision = 10, scale = 2, nullable = false)
	private BigDecimal commissionAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "payout_status", length = 20, nullable = false)
	@Builder.Default
	private PayoutStatus payoutStatus = PayoutStatus.PENDING;

	@Column(name = "paid_at")
	private LocalDateTime paidAt;

	@Column(name = "cancelled_at")
	private LocalDateTime cancelledAt;

	/** Mark payout as paid */
	public void markAsPaid() {
		this.payoutStatus = PayoutStatus.PAID;
		this.paidAt = LocalDateTime.now();
		this.cancelledAt = null;
	}

	/** Mark payout as cancelled */
	public void markAsCancelled() {
		this.payoutStatus = PayoutStatus.CANCELLED;
		this.cancelledAt = LocalDateTime.now();
		this.paidAt = null;
	}
}
