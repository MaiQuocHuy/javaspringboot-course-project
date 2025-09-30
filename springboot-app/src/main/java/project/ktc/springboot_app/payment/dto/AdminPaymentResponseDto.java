package project.ktc.springboot_app.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.payment.entity.Payment;

/**
 * DTO for Payment response containing payment information with course details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentResponseDto {
	private String id;

	private UserInfoDto user;

	private BigDecimal amount;

	private String currency;

	private String status;

	private String paymentMethod;

	// @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
	private LocalDateTime createdAt;

	private LocalDateTime paidAt;

	private LocalDateTime paidOutAt;

	private CourseInfoDto course;

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
	}

	/** Factory method to create PaymentAdminResponseDto from Payment entity */
	public static AdminPaymentResponseDto fromEntity(Payment payment) {
		UserInfoDto userInfoDto = UserInfoDto.builder()
				.id(payment.getUser().getId())
				.name(payment.getUser().getName())
				.email(payment.getUser().getEmail())
				.thumbnailUrl(payment.getUser().getThumbnailUrl())
				.build();

		CourseInfoDto courseInfo = CourseInfoDto.builder()
				.id(payment.getCourse().getId())
				.title(payment.getCourse().getTitle())
				.thumbnailUrl(payment.getCourse().getThumbnailUrl())
				.build();

		return AdminPaymentResponseDto.builder()
				.id(payment.getId())
				.user(userInfoDto)
				.amount(payment.getAmount())
				.currency("USD") // Default currency as specified in requirements
				.status(payment.getStatus().name())
				.paymentMethod(payment.getPaymentMethod())
				.createdAt(payment.getCreatedAt())
				.paidAt(payment.getPaidAt())
				.paidOutAt(payment.getPaidOutAt())
				.course(courseInfo)
				.build();
	}
}
