package project.ktc.springboot_app.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a course review")
public class CreateReviewDto {

	@NotNull(message = "Rating is required")
	@Min(value = 1, message = "Rating must be at least 1")
	@Max(value = 5, message = "Rating must be at most 5")
	@Schema(description = "Course rating from 1 to 5", example = "5", minimum = "1", maximum = "5")
	private Integer rating;

	@NotBlank(message = "Review text is required")
	@Size(max = 1000, message = "Review text must not exceed 1000 characters")
	@Schema(description = "Review text content", example = "Excellent course! Very helpful and well-structured.", maxLength = 1000)
	private String review_text;
}
