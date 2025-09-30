package project.ktc.springboot_app.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.payment.entity.Payment;

/** DTO for detailed Payment response containing payment information with external gateway data */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentDetailResponseDto {

  private String id;

  private UserInfoDto user;

  private BigDecimal amount;

  private String currency;

  private String status;

  private String paymentMethod;

  // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  private LocalDateTime createdAt;

  private LocalDateTime paidAt;

  private LocalDateTime paidoutAt;

  private LocalDateTime updatedAt;

  private String transactionId;

  private String stripeSessionId;

  private String receiptUrl;

  private CardInfoDto card;

  private CourseInfoDto course;

  /** Nested DTO for card information */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CardInfoDto {
    private String brand;
    private String last4;
    private Integer expMonth;
    private Integer expYear;
  }

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

  /** Factory method to create PaymentDetailAdminResponseDto from Payment entity */
  public static AdminPaymentDetailResponseDto fromEntity(Payment payment) {
    UserInfoDto userInfoDto =
        UserInfoDto.builder()
            .id(payment.getUser().getId())
            .name(payment.getUser().getName())
            .email(payment.getUser().getEmail())
            .thumbnailUrl(payment.getUser().getThumbnailUrl())
            .build();

    CourseInfoDto courseInfo =
        CourseInfoDto.builder()
            .id(payment.getCourse().getId())
            .title(payment.getCourse().getTitle())
            .thumbnailUrl(payment.getCourse().getThumbnailUrl())
            .instructor(
                payment.getCourse().getInstructor() != null
                    ? UserInfoDto.builder()
                        .id(payment.getCourse().getInstructor().getId())
                        .name(payment.getCourse().getInstructor().getName())
                        .email(payment.getCourse().getInstructor().getEmail())
                        .thumbnailUrl(payment.getCourse().getInstructor().getThumbnailUrl())
                        .build()
                    : null)
            .level(payment.getCourse().getLevel())
            .price(payment.getCourse().getPrice())
            .build();

    return AdminPaymentDetailResponseDto.builder()
        .id(payment.getId())
        .user(userInfoDto)
        .amount(payment.getAmount())
        .currency("USD") // Default currency as specified in requirements
        .status(payment.getStatus().name())
        .paymentMethod(payment.getPaymentMethod())
        .createdAt(payment.getCreatedAt())
        .paidAt(payment.getPaidAt())
        .paidoutAt(payment.getPaidOutAt())
        .updatedAt(payment.getUpdatedAt())
        .stripeSessionId(payment.getSessionId())
        .course(courseInfo)
        .build();
  }

  /** Factory method to create PaymentDetailAdminResponseDto with Stripe data */
  public static AdminPaymentDetailResponseDto fromEntityWithStripeData(
      Payment payment, StripePaymentData stripeData) {
    AdminPaymentDetailResponseDto dto = fromEntity(payment);

    if (stripeData != null) {
      dto.setTransactionId(stripeData.getTransactionId());
      dto.setReceiptUrl(stripeData.getReceiptUrl());

      if (stripeData.getCardBrand() != null) {
        dto.setCard(
            CardInfoDto.builder()
                .brand(stripeData.getCardBrand())
                .last4(stripeData.getCardLast4())
                .expMonth(stripeData.getCardExpMonth())
                .expYear(stripeData.getCardExpYear())
                .build());
      }
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
    private String cardBrand;
    private String cardLast4;
    private Integer cardExpMonth;
    private Integer cardExpYear;
  }
}
