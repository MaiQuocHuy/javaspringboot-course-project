package project.ktc.springboot_app.discount.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.discount.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for discount information
 * Includes all relevant discount details for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Discount response object")
public class DiscountResponseDto {

    @Schema(description = "Discount unique identifier", example = "discount-uuid-123")
    private String id;

    @Schema(description = "Discount code", example = "WELCOME10")
    private String code;

    @Schema(description = "Discount percentage", example = "10.00")
    private BigDecimal discountPercent;

    @Schema(description = "Discount description", example = "Welcome discount for new users")
    private String description;

    @Schema(description = "Discount type", example = "GENERAL")
    private DiscountType type;

    @Schema(description = "Owner user information (only for REFERRAL type)")
    private OwnerUserInfo ownerUser;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Discount start date", example = "2024-01-01T00:00:00")
    private LocalDateTime startDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Discount end date", example = "2024-12-31T23:59:59")
    private LocalDateTime endDate;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(description = "Maximum total usage limit", example = "1000")
    private Integer usageLimit;

    @Schema(description = "Maximum usage per user", example = "1")
    private Integer perUserLimit;

    @Schema(description = "Whether the discount is currently active", example = "true")
    private Boolean isActive;

    @Schema(description = "Current usage count", example = "25")
    private Integer currentUsageCount;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Schema(description = "Remaining usage count (-1 for unlimited, 0+ for limited)", example = "75")
    private Integer remainingUsageCount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Creation timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Last update timestamp", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Nested class for owner user information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Owner user information for referral discounts")
    public static class OwnerUserInfo {
        @Schema(description = "User ID", example = "user-001")
        private String id;

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User email", example = "john.doe@example.com")
        private String email;
    }
}
