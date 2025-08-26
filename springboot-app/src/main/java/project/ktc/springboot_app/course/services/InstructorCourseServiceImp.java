package project.ktc.springboot_app.course.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.category.entity.Category;
import project.ktc.springboot_app.category.repositories.CategoryRepository;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.dto.CourseDashboardResponseDto;
import project.ktc.springboot_app.course.dto.CreateCourseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseDto;
import project.ktc.springboot_app.course.dto.CourseResponseDto.CategoryInfo;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseStatusDto;
import project.ktc.springboot_app.course.dto.CourseStatusUpdateResponseDto;
import project.ktc.springboot_app.course.dto.InstructorCourseDetailResponseDto;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.entity.CourseReviewStatus;
import project.ktc.springboot_app.course.entity.CourseReviewStatusHistory;
import project.ktc.springboot_app.course.interfaces.InstructorCourseService;
import project.ktc.springboot_app.course.dto.common.BaseCourseResponseDto;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.course.repositories.InstructorCourseRepository;
import project.ktc.springboot_app.course.repositories.CourseReviewStatusRepository;
import project.ktc.springboot_app.course.repositories.CourseReviewStatusHistoryRepository;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.services.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.services.FileValidationService;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.utils.StringUtil;
import project.ktc.springboot_app.utils.MathUtil;
import project.ktc.springboot_app.log.services.SystemLogHelper;
import project.ktc.springboot_app.log.dto.CourseLogDto;
import project.ktc.springboot_app.log.utils.CourseLogMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorCourseServiceImp implements InstructorCourseService {
    private final InstructorCourseRepository instructorCourseRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CloudinaryServiceImp cloudinaryService;
    private final FileValidationService fileValidationService;
    private final InstructorSectionRepository sectionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final CourseReviewStatusRepository courseReviewStatusRepository;
    private final CourseReviewStatusHistoryRepository courseReviewStatusHistoryRepository;
    private final SystemLogHelper systemLogHelper;

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<CourseDashboardResponseDto>>> findInstructorCourses(
            String instructorId,
            String search,
            String status, Pageable pageable) {
        log.info("Finding courses for instructor: {} with filters: search={}, status={}, page={}",
                instructorId, search, status, pageable.getPageNumber());

        Page<Course> coursePage = instructorCourseRepository.findByInstructorIdWithFilters(
                instructorId, search, status, pageable);

        List<CourseDashboardResponseDto> courseResponses = coursePage.getContent().stream()
                .map(this::mapToCourseDashboard)
                .collect(Collectors.toList());

        PaginatedResponse<CourseDashboardResponseDto> paginatedResponse = PaginatedResponse
                .<CourseDashboardResponseDto>builder()
                .content(courseResponses)
                .page(PaginatedResponse.PageInfo.builder()
                        .number(coursePage.getNumber())
                        .size(coursePage.getSize())
                        .totalElements(coursePage.getTotalElements())
                        .totalPages(coursePage.getTotalPages())
                        .first(coursePage.isFirst())
                        .last(coursePage.isLast())
                        .build())
                .build();

        return ApiResponseUtil.success(paginatedResponse, "Instructor courses retrieved successfully");
    }

    /**
     * Get instructor's courses with pagination and filtering
     */

    private CourseDashboardResponseDto mapToCourseDashboard(Course course) {
        // Get enrollment count
        Long enrollmentCount = instructorCourseRepository.countEnrollmentsByCourseId(course.getId());

        // Get average rating
        Double averageRating = courseRepository.findAverageRatingByCourseId(course.getId()).orElse(0.0);
        averageRating = MathUtil.roundToTwoDecimals(averageRating); // Consistent rounding

        // Get section count
        Long sectionCount = sectionRepository.countSectionsByCourseId(course.getId());

        // Get total revenue
        BigDecimal revenue = instructorCourseRepository.getTotalRevenueByCourseId(course.getId());
        if (revenue == null) {
            revenue = BigDecimal.ZERO;
        }

        // Get last content update
        Optional<LocalDateTime> lastContentUpdate = instructorCourseRepository
                .getLastContentUpdateByCourseId(course.getId());

        // Determine status based on boolean fields
        String status = determineStatus(course);

        // Get primary category (first one if multiple)
        List<CourseDashboardResponseDto.CategoryInfo> categoryInfos = new ArrayList<>();
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            for (Category category : course.getCategories()) {
                CourseDashboardResponseDto.CategoryInfo info = CourseDashboardResponseDto.CategoryInfo.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build();
                categoryInfos.add(info);
            }
        }

        // Determine permissions
        boolean canEdit = !course.getIsApproved();
        boolean canDelete = !course.getIsApproved() && enrollmentCount == 0;
        boolean canPublish = !course.getIsPublished();
        boolean canUnpublish = course.getIsApproved() && course.getIsPublished();

        // Get review status information
        ReviewStatusInfo reviewInfo = getReviewStatusInfo(course.getId());

        return CourseDashboardResponseDto.dashboardBuilder()
                .id(course.getId())
                .title(course.getTitle())
                .price(course.getPrice())
                .description(course.getDescription())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .categories(categoryInfos)
                .status(status)
                .isApproved(course.getIsApproved())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .lastContentUpdate(lastContentUpdate.orElse(course.getUpdatedAt()))
                .totalStudents(enrollmentCount.intValue())
                .averageRating(averageRating)
                .sectionCount(sectionCount.intValue())
                .revenue(revenue)
                .canEdit(canEdit)
                .canUnpublish(canUnpublish)
                .canDelete(canDelete)
                .canPublish(canPublish)
                .statusReview(reviewInfo.getStatusReview())
                .reason(reviewInfo.getReason())
                .build();
    }

    private String determineStatus(Course course) {
        if (course.getIsPublished()) {
            return "PUBLISHED";
        } else if (!course.getIsPublished()) {
            return "UNPUBLISHED";
        }
        return "DRAFT";
    }

    @Override
    public ResponseEntity<ApiResponse<CourseResponseDto>> createCourse(
            CreateCourseDto createCourseDto,
            MultipartFile thumbnailFile,
            String instructorId) {
        try {
            log.info("Creating course for instructor: {} with title: {}", instructorId, createCourseDto.getTitle());

            // Validate instructor exists
            User instructor = userRepository.findById(instructorId).orElse(null);
            if (instructor == null) {
                log.warn("Instructor not found with ID: {}", instructorId);
                return ApiResponseUtil.notFound("Instructor not found");
            }

            // Check slug for uniqueness with normalization and case-insensitive comparison
            String normalizedSlug = StringUtil.normalizeSlugForComparison(createCourseDto.getSlug());
            if (courseRepository.existsBySlugIgnoreCase(normalizedSlug)) {
                log.warn("Duplicate slug found (case-insensitive): {}", createCourseDto.getSlug());
                return ApiResponseUtil.conflict("Slug is already in use (duplicate found)");
            }

            // Validate categories exist
            List<Category> categories = new ArrayList<>();
            for (String categoryId : createCourseDto.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                if (category == null) {
                    log.warn("Category not found with ID: {}", categoryId);
                    return ApiResponseUtil.notFound("Category not found with ID: " + categoryId);
                }
                categories.add(category);
            }

            // Create new course entity
            Course course = new Course();
            course.setTitle(createCourseDto.getTitle());
            course.setDescription(createCourseDto.getDescription());
            course.setPrice(createCourseDto.getPrice());
            course.setLevel(createCourseDto.getLevel());
            course.setInstructor(instructor);
            course.setCategories(categories);
            course.setSlug(normalizedSlug); // Use normalized slug for consistency

            // Set default values for new course
            course.setIsPublished(false);
            course.setIsApproved(false);
            course.setIsDeleted(false);

            // Handle thumbnail upload if provided
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                try {
                    // Validate the uploaded file
                    fileValidationService.validateImageFile(thumbnailFile);

                    // Upload image to Cloudinary
                    ImageUploadResponseDto uploadResult = cloudinaryService.uploadImage(thumbnailFile);

                    // Update course thumbnail information
                    course.setThumbnailUrl(uploadResult.getUrl());
                    course.setThumbnailId(uploadResult.getPublicId());

                    log.info("Thumbnail uploaded for course: {}", uploadResult.getPublicId());

                } catch (Exception e) {
                    log.error("Error uploading thumbnail for course: {}", e.getMessage(), e);
                    return ApiResponseUtil.badRequest("Failed to upload thumbnail: " + e.getMessage());
                }
            }

            // Save the course
            Course savedCourse = courseRepository.save(course);
            log.info("Course created successfully with ID: {}", savedCourse.getId());

            // Log the course creation
            try {
                CourseLogDto courseLogDto = CourseLogMapper.toLogDto(savedCourse);
                systemLogHelper.logCreate(instructor, "Course", savedCourse.getId(), courseLogDto);
                log.debug("Course creation logged successfully for courseId: {}", savedCourse.getId());
            } catch (Exception logException) {
                log.warn("Failed to log course creation: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            // Create response DTO
            List<CategoryInfo> categoryInfoList = categories.stream()
                    .map(cat -> CourseResponseDto.CategoryInfo.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .build())
                    .collect(Collectors.toList());

            CourseResponseDto responseDto = CourseResponseDto.builder()
                    .id(savedCourse.getId())
                    .title(savedCourse.getTitle())
                    .description(savedCourse.getDescription())
                    .price(savedCourse.getPrice())
                    .level(savedCourse.getLevel())
                    .thumbnailUrl(savedCourse.getThumbnailUrl())
                    .thumbnailId(savedCourse.getThumbnailId())
                    .isPublished(savedCourse.getIsPublished())
                    .isApproved(savedCourse.getIsApproved())
                    .instructor(CourseResponseDto.InstructorInfo.builder()
                            .id(instructor.getId())
                            .name(instructor.getName())
                            .email(instructor.getEmail())
                            .build())
                    .categories(categoryInfoList)
                    .createdAt(savedCourse.getCreatedAt())
                    .updatedAt(savedCourse.getUpdatedAt())
                    .build();

            return ApiResponseUtil.created(responseDto, "Course created successfully");

        } catch (Exception e) {
            log.error("Error creating course: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to create course. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<CourseResponseDto>> updateCourse(
            String courseId,
            UpdateCourseDto updateCourseDto,
            MultipartFile thumbnailFile,
            String instructorId) {
        try {
            log.info("Updating course {} for instructor: {}", courseId, instructorId);
            // Check if title course is duplicate
            if (updateCourseDto.getTitle() != null && !updateCourseDto.getTitle().trim().isEmpty()) {
                String slugTitle = StringUtil.generateSlug(updateCourseDto.getTitle());
                if (courseRepository.existsBySlug(slugTitle)) {
                    log.warn("Duplicate course title detected: {}", updateCourseDto.getTitle());
                    return ApiResponseUtil
                            .conflict("Course with title '" + updateCourseDto.getTitle() + "' already exists");
                }
            }
            // Find the course and validate ownership
            Course existingCourse = courseRepository.findById(courseId).orElse(null);
            if (existingCourse == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Validate instructor ownership
            if (!existingCourse.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} attempted to update course {} owned by {}",
                        instructorId, courseId, existingCourse.getInstructor().getId());
                return ApiResponseUtil.forbidden("You can only update your own courses");
            }

            // Determine permissions
            boolean canEdit = !existingCourse.getIsApproved();

            if (!canEdit) {
                log.warn("Course {} cannot be edited - already approved", courseId);
                return ApiResponseUtil.forbidden("Cannot edit approved courses");
            }

            // Capture old values for logging
            CourseLogDto oldCourseLogDto = CourseLogMapper.toLogDto(existingCourse);

            // Validate instructor exists
            User instructor = userRepository.findById(instructorId).orElse(null);
            if (instructor == null) {
                log.warn("Instructor not found with ID: {}", instructorId);
                return ApiResponseUtil.notFound("Instructor not found");
            }

            // Update course fields if provided
            if (updateCourseDto.getTitle() != null && !updateCourseDto.getTitle().trim().isEmpty()) {
                existingCourse.setTitle(updateCourseDto.getTitle().trim());
            }

            if (updateCourseDto.getDescription() != null && !updateCourseDto.getDescription().trim().isEmpty()) {
                existingCourse.setDescription(updateCourseDto.getDescription().trim());
            }

            if (updateCourseDto.getPrice() != null) {
                existingCourse.setPrice(updateCourseDto.getPrice());
            }

            if (updateCourseDto.getLevel() != null) {
                existingCourse.setLevel(updateCourseDto.getLevel());
            }

            // Update categories if provided
            if (updateCourseDto.getCategoryIds() != null && !updateCourseDto.getCategoryIds().isEmpty()) {
                List<Category> categories = new ArrayList<>();
                for (String categoryId : updateCourseDto.getCategoryIds()) {
                    Category category = categoryRepository.findById(categoryId).orElse(null);
                    if (category == null) {
                        log.warn("Category not found with ID: {}", categoryId);
                        return ApiResponseUtil.notFound("Category not found: " + categoryId);
                    }
                    categories.add(category);
                }
                existingCourse.setCategories(categories);
            }

            // Handle thumbnail upload if provided
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                try {
                    // Validate the uploaded file
                    fileValidationService.validateImageFile(thumbnailFile);

                    // Delete old image if it exists
                    if (existingCourse.getThumbnailId() != null && !existingCourse.getThumbnailId().isEmpty()) {
                        boolean deleted = cloudinaryService.deleteImage(existingCourse.getThumbnailId());
                        log.info("Old thumbnail deletion result for course {}: {}", courseId, deleted);
                    }

                    // Upload new image to Cloudinary
                    ImageUploadResponseDto uploadResult = cloudinaryService.uploadImage(thumbnailFile);

                    // Update course thumbnail information
                    existingCourse.setThumbnailUrl(uploadResult.getUrl());
                    existingCourse.setThumbnailId(uploadResult.getPublicId());

                    log.info("New thumbnail uploaded for course {}: {}", courseId, uploadResult.getPublicId());

                } catch (Exception e) {
                    log.error("Error uploading thumbnail for course: {}", e.getMessage(), e);
                    return ApiResponseUtil.badRequest("Failed to upload thumbnail: " + e.getMessage());
                }
            }

            // Save the updated course
            Course updatedCourse = courseRepository.save(existingCourse);
            log.info("Course updated successfully with ID: {}", updatedCourse.getId());

            // Log the course update
            try {
                CourseLogDto newCourseLogDto = CourseLogMapper.toLogDto(updatedCourse);
                systemLogHelper.logUpdate(instructor, "Course", updatedCourse.getId(), oldCourseLogDto,
                        newCourseLogDto);
                log.debug("Course update logged successfully for courseId: {}", updatedCourse.getId());
            } catch (Exception logException) {
                log.warn("Failed to log course update: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            // Create response DTO
            CourseResponseDto responseDto = CourseResponseDto.builder()
                    .id(updatedCourse.getId())
                    .title(updatedCourse.getTitle())
                    .description(updatedCourse.getDescription())
                    .price(updatedCourse.getPrice())
                    .level(updatedCourse.getLevel())
                    .thumbnailUrl(updatedCourse.getThumbnailUrl())
                    .thumbnailId(updatedCourse.getThumbnailId())
                    .isPublished(updatedCourse.getIsPublished())
                    .isApproved(updatedCourse.getIsApproved())
                    .instructor(CourseResponseDto.InstructorInfo.builder()
                            .id(instructor.getId())
                            .name(instructor.getName())
                            .email(instructor.getEmail())
                            .build())
                    .categories(updatedCourse.getCategories().stream()
                            .map(category -> CourseResponseDto.CategoryInfo.builder()
                                    .id(category.getId())
                                    .name(category.getName())
                                    .build())
                            .collect(Collectors.toList()))
                    .createdAt(updatedCourse.getCreatedAt())
                    .updatedAt(updatedCourse.getUpdatedAt())
                    .build();

            return ApiResponseUtil.success(responseDto, "Course updated successfully");

        } catch (Exception e) {
            log.error("Error updating course: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update course. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteCourse(String courseId, String instructorId) {
        try {
            log.info("Attempting to delete course: {} by instructor: {}", courseId, instructorId);

            // Validate instructor exists
            User instructor = userRepository.findById(instructorId).orElse(null);
            if (instructor == null) {
                log.warn("Instructor not found with ID: {}", instructorId);
                return ApiResponseUtil.notFound("Instructor not found");
            }

            // Find the course and verify ownership
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to delete this course");
            }

            // Check if course is approved
            if (course.getIsApproved()) {
                log.warn("Cannot delete approved course: {}", courseId);
                return ApiResponseUtil.badRequest("Cannot delete a course that is approved");
            }

            // Check if course has enrolled students
            Long enrollmentCount = instructorCourseRepository.countEnrollmentsByCourseId(courseId);
            if (enrollmentCount > 0) {
                log.warn("Cannot delete course {} with {} enrolled students", courseId, enrollmentCount);
                return ApiResponseUtil.badRequest("Cannot delete a course that has enrolled students");
            }

            // Verify deletion permission (same logic as in dashboard)
            boolean canDelete = !course.getIsApproved() && enrollmentCount == 0;
            if (!canDelete) {
                log.warn("Course {} cannot be deleted due to business rules", courseId);
                return ApiResponseUtil.badRequest("Cannot delete a course that is approved or has enrolled students");
            }

            // Capture course data for logging before deletion
            CourseLogDto courseLogDto = CourseLogMapper.toLogDto(course);

            // Delete the course (soft delete by setting isDeleted = true)
            course.setIsDeleted(true);
            courseRepository.save(course);

            // Log the course deletion
            try {
                systemLogHelper.logDelete(instructor, "Course", courseId, courseLogDto);
                log.debug("Course deletion logged successfully for courseId: {}", courseId);
            } catch (Exception logException) {
                log.warn("Failed to log course deletion: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            log.info("Course {} deleted successfully by instructor {}", courseId, instructorId);
            return ApiResponseUtil.success(null, "Course deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting course: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to delete course. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<CourseStatusUpdateResponseDto>> updateCourseStatus(
            String courseId,
            UpdateCourseStatusDto updateStatusDto,
            String instructorId) {

        log.info("Updating course status for courseId: {}, status: {}, instructorId: {}",
                courseId, updateStatusDto.getStatus(), instructorId);

        try {
            // Find the course and verify ownership
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to update this course status");
            }

            // Get current status
            String currentStatus = course.getIsPublished() ? "PUBLISHED" : "UNPUBLISHED";
            String newStatus = updateStatusDto.getStatus();
            log.info("Current status: {}, New status: {}", currentStatus, newStatus);
            // No change check
            if (currentStatus.equals(newStatus)) {
                log.info("Course {} status is already {}", courseId, newStatus);
                return ApiResponseUtil.success(
                        CourseStatusUpdateResponseDto.builder()
                                .id(course.getId())
                                .title(course.getTitle())
                                .previousStatus(currentStatus)
                                .currentStatus(newStatus)
                                .build(),
                        "Course status is already " + newStatus.toLowerCase());
            }

            // Business rules validation
            if ("PUBLISHED".equals(newStatus)) {
                // Check if course meets publish requirements
                if (!canPublishCourse(course)) {
                    return ApiResponseUtil.badRequest(
                            "Course cannot be published. Please ensure it has complete details: title, description, thumbnail, and at least one section with lessons.");
                }

                // Update to published
                course.setIsPublished(true);

                // Handle course review status
                handleCourseReviewStatus(course);

            } else if ("UNPUBLISHED".equals(newStatus)) {
                // Check if course can be unpublished
                if (!canUnpublishCourse(course)) {
                    return ApiResponseUtil.badRequest(
                            "Course cannot be unpublished. Only approved and currently published courses can be unpublished.");
                }

                // Update to unpublished
                course.setIsPublished(false);
                course.setIsApproved(false);
                handleCourseReviewStatusUnPublished(course);
            }

            // Save changes
            courseRepository.save(course);

            // Create response
            CourseStatusUpdateResponseDto response = CourseStatusUpdateResponseDto.builder()
                    .id(course.getId())
                    .title(course.getTitle())
                    .previousStatus(currentStatus)
                    .currentStatus(newStatus)
                    .build();

            log.info("Course {} status updated successfully from {} to {}",
                    courseId, currentStatus, newStatus);

            return ApiResponseUtil.success(response, "Course status updated successfully");

        } catch (Exception e) {
            log.error("Error updating course status: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update course status. Please try again later.");
        }
    }

    /**
     * Check if course can be published
     * Requirements: title, description, thumbnail, and at least one section with
     * lessons
     */
    private boolean canPublishCourse(Course course) {
        // Check basic course details
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            log.debug("Course {} cannot be published: missing title", course.getId());
            return false;
        }

        if (course.getDescription() == null || course.getDescription().trim().isEmpty()) {
            log.debug("Course {} cannot be published: missing description", course.getId());
            return false;
        }

        if (course.getThumbnailUrl() == null || course.getThumbnailUrl().trim().isEmpty()) {
            log.debug("Course {} cannot be published: missing thumbnail", course.getId());
            return false;
        }

        // Check if course has at least one section with lessons
        Long lessonCount = courseRepository.countLessonsByCourseId(course.getId());
        if (lessonCount == 0) {
            log.debug("Course {} cannot be published: no lessons found", course.getId());
            return false;
        }

        return true;
    }

    /**
     * Check if course can be unpublished
     * Requirements: course must be approved and currently published
     */
    private boolean canUnpublishCourse(Course course) {
        if (!course.getIsApproved()) {
            log.debug("Course {} cannot be unpublished: not approved", course.getId());
            return false;
        }

        if (!course.getIsPublished()) {
            log.debug("Course {} cannot be unpublished: not currently published", course.getId());
            return false;
        }

        return true;
    }

    @Override
    public ResponseEntity<ApiResponse<InstructorCourseDetailResponseDto>> getCourseDetails(
            String courseId,
            String instructorId) {

        log.info("Getting course details for courseId: {} by instructor: {}", courseId, instructorId);

        // Step 1: Find the course with instructor
        Course course = instructorCourseRepository.findByIdAndInstructorId(courseId, instructorId)
                .orElseThrow(() -> new RuntimeException("Course not found or you don't have permission to access it"));

        // Step 2: Fetch categories separately to avoid MultipleBagFetchException
        Optional<Course> courseWithCategories = courseRepository.findCourseWithCategories(courseId);
        if (courseWithCategories.isPresent()) {
            course.setCategories(courseWithCategories.get().getCategories());
        }

        // Step 3: Fetch sections with lessons separately
        List<Section> sectionsWithLessons = courseRepository.findSectionsWithLessonsByCourseId(courseId);
        course.setSections(sectionsWithLessons);

        // Step 4: Get course statistics
        Long enrollmentCount = courseRepository.countUserEnrolledInCourse(courseId);
        Double averageRating = courseRepository.findAverageRatingByCourseId(courseId).orElse(0.0);
        averageRating = MathUtil.roundToTwoDecimals(averageRating); // Consistent rounding
        Long ratingCount = courseRepository.countReviewsByCourseId(courseId);
        Long sectionCountLong = sectionRepository.countSectionsByCourseId(courseId);
        Integer sectionCount = sectionCountLong != null ? sectionCountLong.intValue() : 0;
        Optional<LocalDateTime> lastContentUpdate = instructorCourseRepository
                .getLastContentUpdateByCourseId(course.getId());

        // Step 5: Map to response DTO
        InstructorCourseDetailResponseDto responseDto = mapToInstructorCourseDetailResponse(
                course, enrollmentCount.intValue(), averageRating, ratingCount, sectionCount, lastContentUpdate);

        return ApiResponseUtil.success(responseDto, "Course details retrieved successfully");
    }

    private InstructorCourseDetailResponseDto mapToInstructorCourseDetailResponse(
            Course course,
            Integer enrollmentCount,
            Double averageRating,
            Long ratingCount,
            Integer sectionCount,
            Optional<LocalDateTime> lastContentUpdate) {

        // Map instructor info using common base class
        BaseCourseResponseDto.InstructorInfo instructorInfo = null;
        if (course.getInstructor() != null) {
            instructorInfo = BaseCourseResponseDto.InstructorInfo.builder()
                    .id(course.getInstructor().getId())
                    .name(course.getInstructor().getName())
                    .email(course.getInstructor().getEmail())
                    .avatar(course.getInstructor().getThumbnailUrl())
                    .build();
        }

        // Map categories using common base class
        List<BaseCourseResponseDto.CategoryInfo> categoryInfos = new ArrayList<>();
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            categoryInfos = course.getCategories().stream()
                    .map(category -> BaseCourseResponseDto.CategoryInfo.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        // Map sections with lessons
        List<InstructorCourseDetailResponseDto.SectionInfo> sectionInfos = new ArrayList<>();
        if (course.getSections() != null && !course.getSections().isEmpty()) {
            sectionInfos = course.getSections().stream()
                    .map(this::mapToSectionInfo)
                    .collect(Collectors.toList());
        }

        // Get review status information
        ReviewStatusInfo reviewInfo = getReviewStatusInfo(course.getId());

        return new InstructorCourseDetailResponseDto(
                // Base class fields in order
                course.getId(), // id
                course.getTitle(), // title
                course.getDescription(), // description
                course.getPrice(), // price
                course.getLevel(), // level
                course.getThumbnailUrl(), // thumbnailUrl
                course.getIsApproved(), // isApproved
                course.getCreatedAt(), // createdAt
                course.getUpdatedAt(), // updatedAt
                categoryInfos, // categories
                enrollmentCount, // totalStudents
                sectionCount, // sectionCount
                averageRating, // averageRating
                // Child class fields in order
                course.getSlug(), // slug
                instructorInfo, // instructor
                lastContentUpdate.orElse(course.getUpdatedAt()), // lastContentUpdate
                course.getIsPublished(), // isPublished
                enrollmentCount, // enrollmentCount
                ratingCount, // ratingCount
                sectionInfos, // sections
                reviewInfo.getStatusReview(), // statusReview
                reviewInfo.getReason() // reason
        );
    }

    private InstructorCourseDetailResponseDto.SectionInfo mapToSectionInfo(Section section) {
        int totalVideoDuration = 0;
        int totalQuizQuestions = 0;
        List<InstructorCourseDetailResponseDto.LessonInfo> lessonInfos = new ArrayList<>();

        if (section.getLessons() != null) {
            for (Lesson lesson : section.getLessons()) {
                InstructorCourseDetailResponseDto.LessonInfo lessonInfo = mapToLessonInfo(lesson);
                lessonInfos.add(lessonInfo);

                // Calculate totals based on lesson type
                if ("VIDEO".equals(lesson.getLessonType().getName()) && lesson.getContent() != null) {
                    totalVideoDuration += lesson.getContent().getDuration() != null ? lesson.getContent().getDuration()
                            : 0;
                } else if ("QUIZ".equals(lesson.getLessonType().getName())) {
                    // Count quiz questions for this lesson
                    Long questionCount = quizQuestionRepository.countByLessonId(lesson.getId());
                    totalQuizQuestions += questionCount != null ? questionCount.intValue() : 0;
                }
            }
        }

        return InstructorCourseDetailResponseDto.SectionInfo.builder()
                .id(section.getId())
                .title(section.getTitle())
                .totalVideoDuration(totalVideoDuration > 0 ? totalVideoDuration : null)
                .totalQuizQuestion(totalQuizQuestions > 0 ? totalQuizQuestions : null)
                .lessons(lessonInfos)
                .build();
    }

    private InstructorCourseDetailResponseDto.LessonInfo mapToLessonInfo(Lesson lesson) {
        InstructorCourseDetailResponseDto.LessonInfo.LessonInfoBuilder builder = InstructorCourseDetailResponseDto.LessonInfo
                .builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getLessonType().getName());

        if ("VIDEO".equals(lesson.getLessonType().getName()) && lesson.getContent() != null) {
            builder.videoUrl(lesson.getContent().getUrl())
                    .duration(lesson.getContent().getDuration());
        } else if ("QUIZ".equals(lesson.getLessonType().getName())) {
            // Count quiz questions for this lesson
            Long questionCount = quizQuestionRepository.countByLessonId(lesson.getId());
            builder.quizQuestionCount(questionCount != null ? questionCount.intValue() : 0);
        }

        return builder.build();
    }

    /**
     * Handle course review status when publishing a course.
     * - First time publish: Create PENDING status
     * - Subsequent publishes (when record exists, even with null status): Set to
     * RESUBMITTED
     */
    private void handleCourseReviewStatus(Course course) {
        try {
            log.info("Handling course review status for course: {}", course.getId());

            // Check if there's an existing review status record for this course
            Optional<CourseReviewStatus> existingReviewStatus = courseReviewStatusRepository
                    .findByCourseId(course.getId());

            CourseReviewStatus reviewStatus;
            CourseReviewStatus.ReviewStatus newStatus;
            String action;

            if (existingReviewStatus.isPresent()) {
                // Existing record found (including unpublished with null status) - RESUBMITTED
                reviewStatus = existingReviewStatus.get();
                newStatus = CourseReviewStatus.ReviewStatus.RESUBMITTED;
                action = "RESUBMITTED";
                reviewStatus.setStatus(newStatus);
                reviewStatus.setUpdatedAt(LocalDateTime.now());
                log.info("Course {} has existing review record, setting status to RESUBMITTED", course.getId());
            } else {
                // First time publish - PENDING
                newStatus = CourseReviewStatus.ReviewStatus.PENDING;
                action = "PENDING";
                reviewStatus = CourseReviewStatus.builder()
                        .course(course)
                        .status(newStatus)
                        .build();
                log.info("Course {} has no history, setting status to PENDING", course.getId());
            }

            // Save the review status
            reviewStatus = courseReviewStatusRepository.save(reviewStatus);

            // Create a new history record
            CourseReviewStatusHistory historyRecord = CourseReviewStatusHistory.builder()
                    .id(UUID.randomUUID().toString())
                    .courseReview(reviewStatus)
                    .action(action)
                    .reason("") // Empty reason for instructor-initiated publish actions
                    .reviewer(course.getInstructor()) // Set instructor as the reviewer for their own publish action
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            courseReviewStatusHistoryRepository.save(historyRecord);
            log.info("Created new course review status history record for course: {} with action: {}", course.getId(),
                    action);

        } catch (Exception e) {
            log.error("Error handling course review status for course {}: {}", course.getId(), e.getMessage(), e);
            // Don't throw exception to avoid breaking the publish flow
        }
    }

    /**
     * Handle course review status when unpublishing a course.
     * When unpublishing, we remove/set the review status to null since
     * only published courses should have review status.
     */
    private void handleCourseReviewStatusUnPublished(Course course) {
        try {
            log.info("Handling course review status for unpublished course: {}", course.getId());

            // Find existing review status record for this course
            Optional<CourseReviewStatus> existingReviewStatus = courseReviewStatusRepository
                    .findByCourseId(course.getId());

            if (existingReviewStatus.isPresent()) {
                CourseReviewStatus reviewStatus = existingReviewStatus.get();

                // Set status to null (instead of deleting) to maintain record for resubmission
                // detection
                // This preserves the record so next publish will be detected as RESUBMITTED
                reviewStatus.setStatus(null);
                reviewStatus.setUpdatedAt(LocalDateTime.now());

                courseReviewStatusRepository.save(reviewStatus);

                CourseReviewStatusHistory reviewStatusHistory = CourseReviewStatusHistory.builder()
                        .id(UUID.randomUUID().toString())
                        .courseReview(reviewStatus)
                        .action("UNPUBLISHED")
                        .reason("Course unpublished")
                        .reviewer(course.getInstructor())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                courseReviewStatusHistoryRepository.save(reviewStatusHistory);
                log.info("Set course review status to null for unpublished course: {}", course.getId());
            } else {
                log.info("No existing review status found for course: {}", course.getId());
            }

        } catch (Exception e) {
            log.error("Error handling unpublish course review status for course {}: {}", course.getId(), e.getMessage(),
                    e);
            // Don't throw exception to avoid breaking the unpublish flow
        }
    }

    /**
     * Helper method to get review status and reason from
     * course_review_status_history
     * Returns a ReviewStatusInfo object with statusReview and reason
     */
    private ReviewStatusInfo getReviewStatusInfo(String courseId) {
        return courseReviewStatusHistoryRepository.findLatestByCourseId(courseId)
                .map(history -> {
                    String statusReview = history.getAction(); // action field contains the status
                    String reason = "";

                    if ("DENIED".equals(statusReview)) {
                        reason = history.getReason() != null ? history.getReason() : "";
                    }
                    if ("UNPUBLISHED".equals(statusReview)) {
                        statusReview = null;
                    }

                    return new ReviewStatusInfo(statusReview, reason);
                })
                .orElse(new ReviewStatusInfo(null, null));
    }

    /**
     * Helper class to hold review status information
     */
    private static class ReviewStatusInfo {
        private final String statusReview;
        private final String reason;

        public ReviewStatusInfo(String statusReview, String reason) {
            this.statusReview = statusReview;
            this.reason = reason;
        }

        public String getStatusReview() {
            return statusReview;
        }

        public String getReason() {
            return reason;
        }
    }
}
