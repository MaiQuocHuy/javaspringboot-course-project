package project.ktc.springboot_app.discount.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.discount.entity.AffiliatePayout;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAffiliatePayoutResponseDto {
    private String id;
    private CourseInfo course;
    private DiscountUsageInfo discountUsage;
    private BigDecimal commissionPercent;
    private BigDecimal commissionAmount;
    private String payoutStatus;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;
    private LocalDateTime cancelledAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserInfo {
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CourseInfo {
        private String id;
        private String title;
        private UserInfo instructor;
        private BigDecimal price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DiscountUsageInfo {
        private String id;
        private DiscountInfo discount;
        private UserInfo user;
        private LocalDateTime usedAt;
        private BigDecimal discountPercent;
        private BigDecimal discountAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DiscountInfo {
        private String id;
        private String code;
        private String description;
    }

    public static StudentAffiliatePayoutResponseDto fromEntity(AffiliatePayout affiliate) {
        CourseInfo courseInfo = CourseInfo.builder()
                .id(affiliate.getCourse().getId())
                .title(affiliate.getCourse().getTitle())
                .instructor(UserInfo.builder()
                        .name(affiliate.getCourse().getInstructor().getName())
                        .email(affiliate.getCourse().getInstructor().getEmail())
                        .build())
                .price(affiliate.getCourse().getPrice())
                .build();
        DiscountUsageInfo discountUsageInfo = DiscountUsageInfo.builder()
                .id(affiliate.getDiscountUsage().getId())
                .discount(DiscountInfo.builder()
                        .id(affiliate.getDiscountUsage().getDiscount().getId())
                        .code(affiliate.getDiscountUsage().getDiscount().getCode())
                        .description(affiliate.getDiscountUsage().getDiscount().getDescription())
                        .build())
                .user(UserInfo.builder()
                        .name(affiliate.getDiscountUsage().getUser().getName())
                        .email(affiliate.getDiscountUsage().getUser().getEmail())
                        .build())
                .usedAt(affiliate.getDiscountUsage().getUsedAt())
                .discountPercent(affiliate.getDiscountUsage().getDiscountPercent())
                .discountAmount(affiliate.getDiscountUsage().getDiscountAmount())
                .build();

        return StudentAffiliatePayoutResponseDto.builder()
                .id(affiliate.getId())
                .course(courseInfo)
                .discountUsage(discountUsageInfo)
                .commissionPercent(affiliate.getCommissionPercent())
                .commissionAmount(affiliate.getCommissionAmount())
                .payoutStatus(affiliate.getPayoutStatus().name())
                .createdAt(affiliate.getCreatedAt())
                .paidAt(affiliate.getPaidAt())
                .updatedAt(affiliate.getUpdatedAt())
                .cancelledAt(affiliate.getCancelledAt())
                .build();
    }

}
