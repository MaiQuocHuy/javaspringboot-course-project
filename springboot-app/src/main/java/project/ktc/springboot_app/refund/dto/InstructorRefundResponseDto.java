package project.ktc.springboot_app.refund.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.refund.entity.Refund;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorRefundResponseDto {
    private String id;
    private PaymentInfo payment;
    private String reason;
    private String rejectedReason;
    private BigDecimal amount;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private String id;
        private BigDecimal amount;
        private String status;
        private UserInfoDto user;
        private CourseInfoDto course;
        private LocalDateTime createdAt;
    }

    /**
     * Nested DTO for user information in payment response
     */
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

    /**
     * Nested DTO for course information in payment response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseInfoDto {
        private String id;
        private String title;
        private String thumbnailUrl;
        private BigDecimal price;
    }

    public static InstructorRefundResponseDto fromEntity(Refund refund) {
        return InstructorRefundResponseDto.builder()
                .id(refund.getId())
                .payment(PaymentInfo.builder()
                        .id(refund.getPayment().getId())
                        .amount(refund.getPayment().getAmount())
                        .status(refund.getPayment().getStatus().name())
                        .user(UserInfoDto.builder()
                                .id(refund.getPayment().getUser().getId())
                                .name(refund.getPayment().getUser().getName())
                                .email(refund.getPayment().getUser().getEmail())
                                .thumbnailUrl(refund.getPayment().getUser().getThumbnailUrl())
                                .build())
                        .course(CourseInfoDto.builder()
                                .id(refund.getPayment().getCourse().getId())
                                .title(refund.getPayment().getCourse().getTitle())
                                .thumbnailUrl(refund.getPayment().getCourse().getThumbnailUrl())
                                .price(refund.getPayment().getCourse().getPrice())
                                .build())
                        .createdAt(refund.getPayment().getCreatedAt())
                        .build())
                .reason(refund.getReason())
                .rejectedReason(refund.getRejectedReason())
                .amount(refund.getAmount())
                .status(refund.getStatus().name())
                .requestedAt(refund.getRequestedAt())
                .processedAt(refund.getProcessedAt())
                .build();
    }
}
