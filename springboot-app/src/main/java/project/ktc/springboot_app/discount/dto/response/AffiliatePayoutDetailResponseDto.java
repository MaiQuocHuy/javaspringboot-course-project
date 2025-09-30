package project.ktc.springboot_app.discount.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.discount.enums.PayoutStatus;

/**
 * Detailed response DTO for affiliate payout information Includes comprehensive
 * information about
 * the payout, discount usage, and related entities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliatePayoutDetailResponseDto {

	private String id;
	private PayoutStatus payoutStatus;
	private BigDecimal commissionPercent;
	private BigDecimal commissionAmount;
	private LocalDateTime createdAt;
	private LocalDateTime paidAt;
	private LocalDateTime cancelledAt;

	// Referrer information (who gets the commission)
	private ReferrerInfo referrer;

	// Course information
	private CourseInfo course;

	// Discount information
	private DiscountInfo discount;

	// User who purchased the course (used the discount)
	private PurchaserInfo purchaser;

	// Discount usage details
	private UsageInfo usageInfo;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ReferrerInfo {
		private String id;
		private String name;
		private String email;
		private LocalDateTime joinedAt;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CourseInfo {
		private String id;
		private String title;
		private String description;
		private BigDecimal price;
		private String instructorName;
		private String instructorEmail;
		private LocalDateTime courseCreatedAt;
		private boolean isPublished;
		private boolean isApproved;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DiscountInfo {
		private String id;
		private String code;
		private DiscountType type;
		private BigDecimal discountPercent;
		private String description;
		private LocalDateTime startDate;
		private LocalDateTime endDate;
		private Integer usageLimit;
		private Integer perUserLimit;
		private boolean isActive;
		private LocalDateTime discountCreatedAt;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PurchaserInfo {
		private String id;
		private String name;
		private String email;
		private LocalDateTime joinedAt;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UsageInfo {
		private String id;
		private LocalDateTime usedAt;
		private BigDecimal discountPercent;
		private BigDecimal discountAmount;
		private BigDecimal originalCoursePrice;
		private BigDecimal finalPrice;
	}
}
