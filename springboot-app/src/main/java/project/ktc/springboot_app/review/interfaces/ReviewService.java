package project.ktc.springboot_app.review.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.CreateReviewDto;
import project.ktc.springboot_app.review.dto.ReviewResponseDto;
import project.ktc.springboot_app.review.dto.StudentReviewResponseDto;
import project.ktc.springboot_app.review.dto.UpdateReviewDto;

public interface ReviewService {

        /**
         * Creates a new review for a course by the authenticated user
         *
         * @param courseId  the ID of the course to review
         * @param reviewDto the review data
         * @return ResponseEntity with the created review
         */
        ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(String courseId, CreateReviewDto reviewDto);

        /**
         * Gets all reviews for a specific course with pagination
         *
         * @param courseId the ID of the course
         * @param pageable pagination information
         * @return ResponseEntity with paginated reviews
         */
        ResponseEntity<ApiResponse<PaginatedResponse<ReviewResponseDto>>> getCourseReviews(String courseId,
                        Pageable pageable);

        /**
         * Updates an existing review by the authenticated user (partial update)
         *
         * @param reviewId  the ID of the review to update
         * @param reviewDto the updated review data (partial)
         * @return ResponseEntity with the updated review
         */
        ResponseEntity<ApiResponse<ReviewResponseDto>> updateReview(String reviewId, UpdateReviewDto reviewDto);

        /**
         * Deletes a review by the authenticated user
         *
         * @param reviewId the ID of the review to delete
         * @return ResponseEntity with success message
         */
        ResponseEntity<ApiResponse<Void>> deleteReview(String reviewId);

        /**
         * Gets all reviews submitted by the currently authenticated student
         *
         * @param pageable pagination and sorting information
         * @return ResponseEntity with paginated student reviews
         */
        ResponseEntity<ApiResponse<PaginatedResponse<StudentReviewResponseDto>>> getStudentReviews(Pageable pageable);

        /**
         * Gets all reviews for a specific course by its slug with pagination
         *
         * @param courseSlug the slug of the course
         * @param pageable   pagination information
         * @return ResponseEntity with paginated reviews
         */
        ResponseEntity<ApiResponse<PaginatedResponse<ReviewResponseDto>>> getCourseReviewsBySlug(String courseSlug,
                        Pageable pageable);
}
