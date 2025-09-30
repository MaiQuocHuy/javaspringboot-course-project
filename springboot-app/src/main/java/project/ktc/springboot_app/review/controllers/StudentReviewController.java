package project.ktc.springboot_app.review.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.review.dto.ReviewResponseDto;
import project.ktc.springboot_app.review.dto.StudentReviewResponseDto;
import project.ktc.springboot_app.review.dto.StudentReviewStatsDto;
import project.ktc.springboot_app.review.dto.UpdateReviewDto;
import project.ktc.springboot_app.review.services.ReviewServiceImp;

/** REST Controller for student review operations */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
@Tag(name = "Student Review API", description = "Endpoints for students to view their submitted reviews")
@Slf4j
public class StudentReviewController {

	private final ReviewServiceImp reviewService;

	/**
	 * Retrieve all reviews submitted by the currently authenticated student
	 *
	 * @param pageable
	 *            Pagination and sorting parameters
	 * @return ResponseEntity containing paginated list of student reviews
	 */
	@GetMapping("/reviews")
	@Operation(summary = "Get student reviews", description = """
			Retrieves all reviews submitted by the currently authenticated student with pagination and sorting support.

			**Features:**
			- Returns all reviews submitted by the current student
			- Includes course information (ID, title) for each review
			- Supports pagination with customizable page size
			- Supports sorting by various fields (rating, reviewedAt, etc.)
			- Default sorting is by review date (most recent first)
			- Secure access with student role verification

			**Pagination:**
			- Default page size: 10 reviews per page
			- Page numbering starts from 0
			- Maximum page size: 100 reviews per page
			- Returns total count and page information

			**Sorting Options:**
			- `rating,desc` - Sort by rating (highest to lowest)
			- `rating,asc` - Sort by rating (lowest to highest)
			- `reviewedAt,desc` - Sort by review date (most recent first) - DEFAULT
			- `reviewedAt,asc` - Sort by review date (oldest first)
			- Multiple sort criteria can be combined

			**Response includes:**
			- Review ID and content (rating, text)
			- Course information (ID, title)
			- Review submission timestamp
			- Pagination metadata (page, size, total elements, total pages)

			**Security:**
			- Requires STUDENT role authentication
			- Students can only access their own reviews
			- No access to reviews from other students
			""")
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - STUDENT role required", content = @Content(mediaType = "application/json")),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during review retrieval", content = @Content(mediaType = "application/json"))
	})
	public ResponseEntity<ApiResponse<PaginatedResponse<StudentReviewResponseDto>>> getStudentReviews(
			@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,
			@Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
			@Parameter(description = "Sort field and direction (e.g., 'rating,asc', 'reviewedAt,desc')") @RequestParam(defaultValue = "reviewedAt,desc") String sort) {
		Sort.Direction sortDirection = Sort.Direction.ASC;
		String sortField = "reviewedAt";

		if (sort != null && sort.contains(",")) {
			String[] sortParams = sort.split(",");
			sortField = sortParams[0];
			if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
				sortDirection = Sort.Direction.DESC;
			}
		}
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

		return reviewService.getStudentReviews(pageable);
	}

	@PatchMapping("/reviews/{reviewId}")
	@PreAuthorize("hasRole('STUDENT')")
	@Operation(summary = "Update a review (partial)", description = """
			Allow students to partially update their own reviews using PATCH semantics.

			**PATCH Behavior:**
			- Only provided fields will be updated
			- Null or missing fields will be ignored
			- At least one field (rating or reviewText) must be provided
			- Supports partial updates for better API usability

			**Fields:**
			- rating: Optional - Course rating from 1 to 5
			- reviewText: Optional - Review text content (max 1000 characters)

			**Security:**
			- Students can only update their own reviews
			- Authentication required with STUDENT role
			""", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data or no fields provided"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - can only update own reviews"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ReviewResponseDto>> updateReview(
			@Parameter(description = "Review ID", required = true, example = "review-uuid-123") @PathVariable String reviewId,
			@Parameter(description = "Updated review data (partial)", required = true) @Valid @RequestBody UpdateReviewDto reviewDto) {

		log.info("=== DEBUG UpdateReviewDto in Controller ===");
		log.info("Received request to update review: {}", reviewId);
		log.info("UpdateReviewDto object: {}", reviewDto);
		log.info("Rating: {}", reviewDto.getRating());
		log.info("ReviewText: {}", reviewDto.getReviewText());
		log.info("Is rating null? {}", reviewDto.getRating() == null);
		log.info("Is reviewText null? {}", reviewDto.getReviewText() == null);
		log.info("=======================================");

		return reviewService.updateReview(reviewId, reviewDto);
	}

	/** Get review statistics for the currently authenticated student */
	@GetMapping("/review-stats")
	@Operation(summary = "Get student review statistics", description = "Retrieve comprehensive review statistics for the authenticated student, including total reviews submitted and average rating given.")
	@SecurityRequirement(name = "bearerAuth")
	@io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review statistics retrieved successfully"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token"),
			@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<StudentReviewStatsDto>> getReviewStats() {
		log.info("Fetching review statistics for authenticated student");
		return reviewService.getStudentReviewStats();
	}
}
