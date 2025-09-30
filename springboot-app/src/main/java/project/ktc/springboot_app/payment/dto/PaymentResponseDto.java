package project.ktc.springboot_app.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.payment.entity.Payment;

/** DTO for Payment response containing payment information with course details */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {

  private String id;

  private BigDecimal amount;

  private String currency;

  private String status;

  private String paymentMethod;

  // @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
  private LocalDateTime createdAt;

  private CourseInfoDto course;

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

  /** Factory method to create PaymentResponseDto from Payment entity */
  public static PaymentResponseDto fromEntity(Payment payment) {
    CourseInfoDto courseInfo =
        CourseInfoDto.builder()
            .id(payment.getCourse().getId())
            .title(payment.getCourse().getTitle())
            .thumbnailUrl(payment.getCourse().getThumbnailUrl())
            .build();

    return PaymentResponseDto.builder()
        .id(payment.getId())
        .amount(payment.getAmount())
        .currency("USD") // Default currency as specified in requirements
        .status(payment.getStatus().name())
        .paymentMethod(payment.getPaymentMethod())
        .createdAt(payment.getCreatedAt())
        .course(courseInfo)
        .build();
  }
}
