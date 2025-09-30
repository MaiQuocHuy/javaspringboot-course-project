package project.ktc.springboot_app.discount.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.discount.enums.PayoutStatus;

/** Response DTO for affiliate payout data */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliatePayoutResponseDto {

	private String id;

	private ReferredByUserDto referredByUser;

	private CourseDto course;

	private DiscountUsageDto discountUsage;

	private BigDecimal commissionPercent;

	private BigDecimal commissionAmount;

	private PayoutStatus payoutStatus;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private LocalDateTime paidAt;

	private LocalDateTime cancelledAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ReferredByUserDto {
		private String id;
		private String name;
		private String email;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CourseDto {
		private String id;
		private String name;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DiscountUsageDto {
		private String id;
		private DiscountDto discount;

		@Data
		@Builder
		@NoArgsConstructor
		@AllArgsConstructor
		public static class DiscountDto {
			private String code;
			private String type;
		}
	}
}
