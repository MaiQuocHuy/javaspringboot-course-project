package project.ktc.springboot_app.discount.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.discount.enums.DiscountType;
import project.ktc.springboot_app.discount.validation.ValidDiscountRequest;

/**
 * Request DTO for creating a new discount Includes comprehensive validation for
 * all business rules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidDiscountRequest // Custom validation annotation
@Schema(description = "Request object for creating a new discount")
public class CreateDiscountRequest {

	@NotBlank(message = "Discount code is required")
	@Size(min = 2, max = 50, message = "Discount code must be between 2 and 50 characters")
	@Pattern(regexp = "^[A-Z0-9\\-_]+$", message = "Discount code can only contain uppercase letters, numbers, hyphens, and underscores")
	@Schema(description = "Unique discount code", example = "WELCOME10", required = true)
	private String code;

	@NotNull(message = "Discount percentage is required")
	@DecimalMin(value = "0.01", message = "Discount percentage must be greater than 0")
	@DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100")
	@Digits(integer = 3, fraction = 2, message = "Discount percentage must have at most 3 integer digits and 2 decimal places")
	@Schema(description = "Discount percentage (0.01 to 100.00)", example = "10.00", required = true)
	private BigDecimal discountPercent;

	@Size(max = 255, message = "Description cannot exceed 255 characters")
	@Schema(description = "Description of the discount", example = "Welcome discount for new users")
	private String description;

	@NotNull(message = "Discount type is required")
	@Schema(description = "Type of discount", example = "GENERAL", allowableValues = { "GENERAL",
			"REFERRAL" }, required = true)
	private DiscountType type;

	/** Required for REFERRAL type discounts, must be null for GENERAL type */
	@Schema(description = "Owner user ID (required for REFERRAL type, must be null for GENERAL type)", example = "user-001")
	private String ownerUserId;

	@NotNull(message = "Start date is required")
	@Future(message = "Start date must be in the future")
	@Schema(description = "Discount start date", example = "2024-01-01T00:00:00", required = true)
	private LocalDateTime startDate;

	@NotNull(message = "End date is required")
	@Future(message = "End date must be in the future")
	@Schema(description = "Discount end date", example = "2024-12-31T23:59:59", required = true)
	private LocalDateTime endDate;

	@Min(value = 1, message = "Usage limit must be at least 1 if specified")
	@Schema(description = "Maximum number of times this discount can be used", example = "1000")
	private Integer usageLimit;

	@Min(value = 1, message = "Per user limit must be at least 1 if specified")
	@Schema(description = "Maximum number of times a single user can use this discount", example = "1")
	private Integer perUserLimit;

	/** Validate that start date is before end date */
	@AssertTrue(message = "Start date must be before end date")
	private boolean isStartDateBeforeEndDate() {
		if (startDate == null || endDate == null) {
			return true; // Let other validations handle null checks
		}
		return startDate.isBefore(endDate);
	}
}
