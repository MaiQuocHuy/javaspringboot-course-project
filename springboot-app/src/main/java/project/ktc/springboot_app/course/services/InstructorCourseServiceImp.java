package project.ktc.springboot_app.course.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.interfaces.InstructorCourseService;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.course.repositories.InstructorCourseRepository;
import project.ktc.springboot_app.upload.dto.ImageUploadResponseDto;
import project.ktc.springboot_app.upload.service.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.service.FileValidationService;
import project.ktc.springboot_app.user.repositories.UserRepository;

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
        CourseDashboardResponseDto.CategoryInfo categoryInfo = null;
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            Category category = course.getCategories().get(0);
            categoryInfo = CourseDashboardResponseDto.CategoryInfo.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();
        }

        // Determine permissions
        boolean canEdit = !course.getIsApproved();
        boolean canDelete = !course.getIsApproved() && enrollmentCount == 0;
        boolean canPublish = !course.getIsPublished();
        boolean canUnpublish = course.getIsApproved() && course.getIsPublished();

        return CourseDashboardResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .category(categoryInfo)
                .status(status)
                .isApproved(course.getIsApproved())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .lastContentUpdate(lastContentUpdate.orElse(course.getUpdatedAt()))
                .totalStudents(enrollmentCount.intValue())
                .averageRating(averageRating)
                .revenue(revenue)
                .canEdit(canEdit)
                .canUnpublish(canUnpublish)
                .canDelete(canDelete)
                .canPublish(canPublish)
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

            // Check permissions based on course approval status and enrollments
            Long enrollmentCount = instructorCourseRepository.countEnrollmentsByCourseId(courseId);

            // Determine permissions
            boolean canEdit = !existingCourse.getIsApproved();

            if (!canEdit) {
                log.warn("Course {} cannot be edited - already approved", courseId);
                return ApiResponseUtil.forbidden("Cannot edit approved courses");
            }

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

            // Delete the course (soft delete by setting isDeleted = true)
            course.setIsDeleted(true);
            courseRepository.save(course);

            log.info("Course {} deleted successfully by instructor {}", courseId, instructorId);
            return ApiResponseUtil.success(null, "Course deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting course: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to delete course. Please try again later.");
        }
    }

    @Override
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

            } else if ("UNPUBLISHED".equals(newStatus)) {
                // Check if course can be unpublished
                if (!canUnpublishCourse(course)) {
                    return ApiResponseUtil.badRequest(
                            "Course cannot be unpublished. Only approved and currently published courses can be unpublished.");
                }

                // Update to unpublished
                course.setIsPublished(false);
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
}
