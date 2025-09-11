package project.ktc.springboot_app.discount.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.discount.entity.DiscountUsage;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorDiscountUsageResponseDto {
    private String id;
    private DiscountInfo discount;
    private UserInfo user;
    private CourseInfo course;
    private UserInfo referredByUser;
    private LocalDateTime usedAt;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class DiscountInfo {
        private String id;
        private String code;
        private String description;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserInfo {
        private String name;
        private String email;
        private String bio;
        private String thumbnailUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class CourseInfo {
        private String id;
        private String title;
        private BigDecimal price;
    }

    public static InstructorDiscountUsageResponseDto fromEntity(DiscountUsage usage) {
        DiscountInfo discountInfo = DiscountInfo.builder()
                .id(usage.getDiscount().getId())
                .code(usage.getDiscount().getCode())
                .description(usage.getDiscount().getDescription())
                .type(usage.getDiscount().getType().name())
                .build();
        UserInfo userInfo = UserInfo.builder()
                .name(usage.getUser().getName())
                .email(usage.getUser().getEmail())
                .bio(usage.getUser().getBio())
                .thumbnailUrl(usage.getUser().getThumbnailUrl())
                .build();
        CourseInfo courseInfo = CourseInfo.builder()
                .id(usage.getCourse().getId())
                .title(usage.getCourse().getTitle())
                .price(usage.getCourse().getPrice())
                .build();
        UserInfo referredByUserInfo = UserInfo.builder()
                .name(usage.getReferredByUser().getName())
                .email(usage.getReferredByUser().getEmail())
                .bio(usage.getReferredByUser().getBio())
                .thumbnailUrl(usage.getReferredByUser().getThumbnailUrl())
                .build();
        return InstructorDiscountUsageResponseDto.builder()
                .id(usage.getId())
                .discount(discountInfo)
                .user(userInfo)
                .course(courseInfo)
                .referredByUser(referredByUserInfo)
                .usedAt(usage.getUsedAt())
                .discountPercent(usage.getDiscountPercent())
                .discountAmount(usage.getDiscountAmount())
                .build();
    }
}
