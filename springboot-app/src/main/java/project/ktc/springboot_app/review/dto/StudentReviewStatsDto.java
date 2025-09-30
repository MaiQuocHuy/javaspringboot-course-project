package project.ktc.springboot_app.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO for student review statistics */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student review statistics containing total reviews and average rating")
public class StudentReviewStatsDto {

	@Schema(description = "Total number of reviews submitted by the student", example = "15")
	private Long totalReviews;

	@Schema(description = "Average rating given by the student across all reviews", example = "4.2")
	private Double averageRating;
}
