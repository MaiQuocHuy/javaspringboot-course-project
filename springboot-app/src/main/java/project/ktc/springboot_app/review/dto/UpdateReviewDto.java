package project.ktc.springboot_app.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for partially updating a course review (PATCH operation)")
public class UpdateReviewDto {

	@Min(value = 1, message = "Rating must be at least 1")
	@Max(value = 5, message = "Rating must be at most 5")
	@Schema(description = "Course rating from 1 to 5 (optional for partial update)", example = "5", minimum = "1", maximum = "5")
	private Integer rating;

	@Size(max = 1000, message = "Review text must not exceed 1000 characters")
	@Schema(description = "Review text content (optional for partial update)", example = "Excellent course! Very helpful and well-structured.", maxLength = 1000)
	private String reviewText;
}
