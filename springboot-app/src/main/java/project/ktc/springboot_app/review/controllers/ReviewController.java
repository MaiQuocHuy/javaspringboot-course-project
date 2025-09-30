package project.ktc.springboot_app.review.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.CreateReviewDto;
import project.ktc.springboot_app.review.dto.ReviewResponseDto;
import project.ktc.springboot_app.review.services.ReviewServiceImp;

@RestController
@RequestMapping("/api/courses")
@Tag(name = "Course Reviews API", description = "Endpoints for managing course reviews")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

  private final ReviewServiceImp reviewService;

  @PostMapping("/{id}/reviews")
  @PreAuthorize("hasRole('STUDENT')")
  @Operation(
      summary = "Submit a review for a course",
      description =
          "Allow authenticated students to submit a review for a course only if they are enrolled and have not already reviewed the course",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Review submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - user not enrolled in course"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict - user has already reviewed this course")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<ReviewResponseDto>>
      createReview(
          @Parameter(description = "Course ID", required = true, example = "course-uuid-123")
              @PathVariable("id")
              String courseId,
          @Parameter(description = "Review data", required = true) @Valid @RequestBody
              CreateReviewDto reviewDto) {

    log.info("Received request to create review for course: {}", courseId);
    return reviewService.createReview(courseId, reviewDto);
  }

  @GetMapping("/{id}/reviews")
  @Operation(
      summary = "Get all reviews for a course",
      description = "Retrieves a paginated list of all reviews for a specific course")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<ReviewResponseDto>>>
      getCourseReviews(
          @Parameter(description = "Course ID", required = true, example = "course-uuid-123")
              @PathVariable("id")
              String courseId,
          @Parameter(description = "Page number (0-based)")
              @RequestParam(defaultValue = "0")
              @Min(0)
              Integer page,
          @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100)
              Integer size,
          @Parameter(description = "Sort field and direction (e.g., 'reviewedAt,desc')")
              @RequestParam(defaultValue = "reviewedAt,desc")
              String sort) {

    log.info(
        "Received request to get reviews for course: {} with page: {}, size: {}",
        courseId,
        page,
        size);

    // Parse sort parameter
    Sort.Direction sortDirection = Sort.Direction.DESC;
    String sortField = "reviewedAt";

    if (sort != null && sort.contains(",")) {
      String[] sortParams = sort.split(",");
      sortField = sortParams[0];
      if (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])) {
        sortDirection = Sort.Direction.ASC;
      }
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
    return reviewService.getCourseReviews(courseId, pageable);
  }

  @DeleteMapping("/reviews/{reviewId}")
  @PreAuthorize("hasRole('STUDENT')")
  @Operation(
      summary = "Delete a review",
      description = "Allow students to delete their own reviews",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
        @ApiResponse(responseCode = "403", description = "Forbidden - can only delete own reviews"),
        @ApiResponse(responseCode = "404", description = "Review not found")
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteReview(
      @Parameter(description = "Review ID", required = true, example = "review-uuid-123")
          @PathVariable
          String reviewId) {

    log.info("Received request to delete review: {}", reviewId);
    return reviewService.deleteReview(reviewId);
  }

  @GetMapping("/slug/{slug}/reviews")
  @Operation(
      summary = "Get all reviews for a course",
      description = "Retrieves a paginated list of all reviews for a specific course")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Course not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
      })
  public ResponseEntity<
          project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<ReviewResponseDto>>>
      getCourseReviewsSlug(
          @Parameter(description = "Course slug", required = true, example = "course-slug-123")
              @PathVariable("slug")
              String courseSlug,
          @Parameter(description = "Page number (0-based)")
              @RequestParam(defaultValue = "0")
              @Min(0)
              Integer page,
          @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100)
              Integer size,
          @Parameter(description = "Sort field and direction (e.g., 'reviewedAt,desc')")
              @RequestParam(defaultValue = "reviewedAt,desc")
              String sort) {

    log.info(
        "Received request to get reviews for course: {} with page: {}, size: {}",
        courseSlug,
        page,
        size);

    // Parse sort parameter
    Sort.Direction sortDirection = Sort.Direction.DESC;
    String sortField = "reviewedAt";

    if (sort != null && sort.contains(",")) {
      String[] sortParams = sort.split(",");
      sortField = sortParams[0];
      if (sortParams.length > 1 && "asc".equalsIgnoreCase(sortParams[1])) {
        sortDirection = Sort.Direction.ASC;
      }
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
    return reviewService.getCourseReviewsBySlug(courseSlug, pageable);
  }
}
