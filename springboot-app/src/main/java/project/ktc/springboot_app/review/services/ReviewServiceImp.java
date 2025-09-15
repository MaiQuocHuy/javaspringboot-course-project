package project.ktc.springboot_app.review.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.cache.services.domain.ReviewsCacheService;
import project.ktc.springboot_app.cache.services.infrastructure.CacheInvalidationService;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.CreateReviewDto;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.review.dto.ReviewResponseDto;
import project.ktc.springboot_app.review.dto.StudentReviewResponseDto;
import project.ktc.springboot_app.review.dto.UpdateReviewDto;
import project.ktc.springboot_app.review.entity.Review;
import project.ktc.springboot_app.review.interfaces.ReviewService;
import project.ktc.springboot_app.review.repositories.ReviewRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImp implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ReviewsCacheService reviewsCacheService;
    private final CacheInvalidationService cacheInvalidationService;

    @Override
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(String courseId, CreateReviewDto reviewDto) {
        log.info("Creating review for course {} by user", courseId);

        // Get current authenticated user
        String currentUserId = SecurityUtil.getCurrentUserId();

        // 1. Validate course exists
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Course not found");
        }
        Course course = courseOpt.get();

        // 2. Validate user exists
        Optional<User> userOpt = userRepository.findById(currentUserId);
        if (userOpt.isEmpty()) {
            return ApiResponseUtil.notFound("User not found");
        }
        User user = userOpt.get();

        // 3. Check if user is enrolled in the course
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId);
        if (!isEnrolled) {
            return ApiResponseUtil.forbidden("You must be enrolled in this course to submit a review");
        }

        // 4. Check if user has already reviewed this course
        boolean hasReviewed = reviewRepository.existsByUserIdAndCourseId(currentUserId, courseId);
        if (hasReviewed) {
            return ApiResponseUtil.conflict("You have already reviewed this course");
        }

        // 5. Create and save the review
        Review review = new Review();
        review.setUser(user);
        review.setCourse(course);
        review.setRating(reviewDto.getRating());
        review.setReviewText(reviewDto.getReview_text());
        review.setReviewedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);
        log.info("Successfully created review {} for course {} by user {}", savedReview.getId(), courseId,
                currentUserId);

        // Invalidate cached reviews for this course
        reviewsCacheService.invalidateCourseReviews(courseId);
        cacheInvalidationService.invalidateInstructorStatisticsOnReview(course.getInstructor().getId());

        // 6. Convert to DTO and return
        ReviewResponseDto responseDto = mapToResponseDto(savedReview);
        return ApiResponseUtil.created(responseDto, "Review submitted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PaginatedResponse<ReviewResponseDto>>> getCourseReviews(String courseId,
            Pageable pageable) {
        log.info("Fetching reviews for course {} with pagination: {}", courseId, pageable);

        // Validate course exists
        if (!courseRepository.existsById(courseId)) {
            return ApiResponseUtil.notFound("Course not found");
        }

        // Fetch reviews with pagination
        Page<Review> reviewsPage = reviewRepository.findByCourseIdWithUser(courseId, pageable);

        // Convert to DTOs
        List<ReviewResponseDto> reviewDtos = reviewsPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        // Create paginated response
        PaginatedResponse<ReviewResponseDto> paginatedResponse = PaginatedResponse.<ReviewResponseDto>builder()
                .content(reviewDtos)
                .page(PaginatedResponse.PageInfo.builder()
                        .number(reviewsPage.getNumber())
                        .size(reviewsPage.getSize())
                        .totalElements(reviewsPage.getTotalElements())
                        .totalPages(reviewsPage.getTotalPages())
                        .first(reviewsPage.isFirst())
                        .last(reviewsPage.isLast())
                        .build())
                .build();

        return ApiResponseUtil.success(paginatedResponse, "Course reviews retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<ReviewResponseDto>> updateReview(String reviewId, UpdateReviewDto reviewDto) {
        log.info("Updating review {} by user", reviewDto);

        String currentUserId = SecurityUtil.getCurrentUserId();

        // Find the review
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Review not found");
        }

        Review review = reviewOpt.get();
        log.info("Found review {}", review.getReviewText());
        // Check if current user owns this review
        if (!review.getUser().getId().equals(currentUserId)) {
            return ApiResponseUtil.forbidden("You can only update your own reviews");
        }

        // PATCH semantics: only update provided fields
        boolean isUpdated = false;

        if (reviewDto.getRating() != null) {
            review.setRating(reviewDto.getRating());
            isUpdated = true;
        }

        if (reviewDto.getReviewText() != null) {
            review.setReviewText(reviewDto.getReviewText());
            isUpdated = true;
        }

        if (!isUpdated) {
            return ApiResponseUtil.badRequest("At least one field (rating or reviewText) must be provided for update");
        }

        review.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        log.info("Successfully updated review {} by user {}", reviewId, currentUserId);

        ReviewResponseDto responseDto = mapToResponseDto(updatedReview);
        return ApiResponseUtil.success(responseDto, "Review updated successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteReview(String reviewId) {
        log.info("Deleting review {} by user", reviewId);

        String currentUserId = SecurityUtil.getCurrentUserId();

        // Find the review
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Review not found");
        }

        Review review = reviewOpt.get();

        // Check if current user owns this review
        if (!review.getUser().getId().equals(currentUserId)) {
            return ApiResponseUtil.forbidden("You can only delete your own reviews");
        }

        // Delete the review
        reviewRepository.delete(review);
        log.info("Successfully deleted review {} by user {}", reviewId, currentUserId);

        return ApiResponseUtil.success(null, "Review deleted successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<StudentReviewResponseDto>>> getStudentReviews(
            Pageable pageable) {
        log.info("Retrieving reviews for current student with pagination: {}", pageable);

        try {
            // Get current authenticated user
            String currentUserId = SecurityUtil.getCurrentUserId();

            if (currentUserId == null || currentUserId.trim().isEmpty()) {
                log.warn("No authenticated user found when retrieving student reviews");
                return ApiResponseUtil.unauthorized("User not authenticated");
            }

            // Fetch paginated reviews for the current user
            Page<Review> reviewPage = reviewRepository.findByUserIdWithCourse(currentUserId, pageable);

            // Convert to DTOs
            List<StudentReviewResponseDto> reviewDtos = reviewPage.getContent().stream()
                    .map(StudentReviewResponseDto::fromEntity)
                    .collect(Collectors.toList());

            // Create paginated response
            PaginatedResponse.PageInfo pageInfo = PaginatedResponse.PageInfo.builder()
                    .number(reviewPage.getNumber())
                    .size(reviewPage.getSize())
                    .totalElements(reviewPage.getTotalElements())
                    .totalPages(reviewPage.getTotalPages())
                    .first(reviewPage.isFirst())
                    .last(reviewPage.isLast())
                    .build();

            PaginatedResponse<StudentReviewResponseDto> paginatedResponse = PaginatedResponse
                    .<StudentReviewResponseDto>builder()
                    .content(reviewDtos)
                    .page(pageInfo)
                    .build();

            log.info("Successfully retrieved {} reviews for user {} (page {} of {})",
                    reviewDtos.size(), currentUserId, reviewPage.getNumber(), reviewPage.getTotalPages());

            return ApiResponseUtil.success(paginatedResponse, "Reviews retrieved successfully");

        } catch (Exception e) {
            log.error("Error retrieving reviews for current student: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve reviews. Please try again later.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PaginatedResponse<ReviewResponseDto>>> getCourseReviewsBySlug(String courseSlug,
            Pageable pageable) {
        log.info("Fetching reviews for course {} with pagination: {}", courseSlug, pageable);

        // Validate course exists and get course details
        Optional<Course> courseOpt = courseRepository.findPublishedCourseBySlugWithDetails(courseSlug);
        if (courseOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Course not found");
        }

        Course course = courseOpt.get();
        String courseId = course.getId();

        try {
            // Try to get from cache first
            PaginatedResponse<ReviewResponseDto> cachedResponse = reviewsCacheService.getCourseReviews(courseId,
                    pageable);

            if (cachedResponse != null) {
                log.debug("Cache hit for reviews for course: {}", courseId);
                return ApiResponseUtil.success(cachedResponse, "Course reviews retrieved successfully");
            }

            log.debug("Cache miss for reviews for course: {}", courseId);

        } catch (Exception e) {
            log.warn("Error accessing cache for course {}: {}", courseId, e.getMessage());
            // Continue with database query if cache fails
        }

        // Cache miss or error - fetch from database
        Page<Review> reviewsPage = reviewRepository.findByCourseSlugWithUser(courseSlug, pageable);

        // Convert to DTOs
        List<ReviewResponseDto> reviewDtos = reviewsPage.getContent().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());

        // Create paginated response
        PaginatedResponse<ReviewResponseDto> paginatedResponse = PaginatedResponse.<ReviewResponseDto>builder()
                .content(reviewDtos)
                .page(PaginatedResponse.PageInfo.builder()
                        .number(reviewsPage.getNumber())
                        .size(reviewsPage.getSize())
                        .totalElements(reviewsPage.getTotalElements())
                        .totalPages(reviewsPage.getTotalPages())
                        .first(reviewsPage.isFirst())
                        .last(reviewsPage.isLast())
                        .build())
                .build();

        // Store in cache for future requests
        reviewsCacheService.storeCourseReviews(courseId, pageable, paginatedResponse);

        return ApiResponseUtil.success(paginatedResponse, "Course reviews retrieved successfully");
    }

    private ReviewResponseDto mapToResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .reviewedAt(review.getReviewedAt())
                .user(ReviewResponseDto.UserSummary.builder()
                        .id(review.getUser().getId())
                        .name(review.getUser().getName())
                        .avatar(review.getUser().getThumbnailUrl())
                        .build())
                .build();
    }
}
