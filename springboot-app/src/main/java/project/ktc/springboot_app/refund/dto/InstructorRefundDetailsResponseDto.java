package project.ktc.springboot_app.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.refund.entity.Refund;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorRefundDetailsResponseDto {
	private String id;
	private PaymentInfoDto payment;
	private String reason;
	private String rejectedReason;
	private BigDecimal amount;
	private String status;
	private LocalDateTime requestedAt;
	private LocalDateTime processedAt;

	/** Nested DTO for user information in payment response */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfoDto {
		private String id;
		private String name;
		private String email;
		private String thumbnailUrl;
	}

	/** Nested DTO for course information in payment response */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CourseInfoDto {
		private String id;
		private String title;
		private String thumbnailUrl;
		private UserInfoDto instructor; // Added instructor information
		private CourseLevel level;
		private BigDecimal price;
	}

	/** Nested DTO for payment information in payment response */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PaymentInfoDto {
		private String id;
		private UserInfoDto user;
		private BigDecimal amount;
		private String currency;
		private String status;
		private String paymentMethod;
		private LocalDateTime createdAt;
		private LocalDateTime paidAt;
		private LocalDateTime updatedAt;
		private String transactionId;
		private String stripeSessionId;
		private String receiptUrl;
		private CourseInfoDto course;
	}

	public static InstructorRefundDetailsResponseDto fromEntity(Refund refund) {
		return InstructorRefundDetailsResponseDto.builder()
				.id(refund.getId())
				.payment(
						PaymentInfoDto.builder()
								.id(refund.getPayment().getId())
								.amount(refund.getPayment().getAmount())
								.currency("USD")
								.status(refund.getPayment().getStatus().name())
								.paymentMethod(refund.getPayment().getPaymentMethod())
								.user(
										UserInfoDto.builder()
												.id(refund.getPayment().getUser().getId())
												.name(refund.getPayment().getUser().getName())
												.email(refund.getPayment().getUser().getEmail())
												.thumbnailUrl(refund.getPayment().getUser().getThumbnailUrl())
												.build())
								.createdAt(refund.getPayment().getCreatedAt())
								.paidAt(refund.getPayment().getPaidAt())
								.updatedAt(refund.getPayment().getUpdatedAt())
								.stripeSessionId(refund.getPayment().getSessionId())
								.course(
										CourseInfoDto.builder()
												.id(refund.getPayment().getCourse().getId())
												.title(refund.getPayment().getCourse().getTitle())
												.thumbnailUrl(refund.getPayment().getCourse().getThumbnailUrl())
												.level(refund.getPayment().getCourse().getLevel())
												.price(refund.getPayment().getCourse().getPrice())
												.build())
								.build())
				.reason(refund.getReason())
				.rejectedReason(refund.getRejectedReason())
				.amount(refund.getAmount())
				.status(refund.getStatus().name())
				.requestedAt(refund.getRequestedAt())
				.processedAt(refund.getProcessedAt())
				.build();
	}

	/** Factory method to create PaymentDetailAdminResponseDto with Stripe data */
	public static InstructorRefundDetailsResponseDto fromEntityWithStripeData(
			Refund refund, StripePaymentData stripeData) {
		InstructorRefundDetailsResponseDto dto = fromEntity(refund);

		if (stripeData != null) {
			dto.getPayment().setTransactionId(stripeData.getTransactionId());
			dto.getPayment().setReceiptUrl(stripeData.getReceiptUrl());
		}

		return dto;
	}

	/** Data class for Stripe payment information */
	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StripePaymentData {
		private String transactionId;
		private String receiptUrl;
	}
}
