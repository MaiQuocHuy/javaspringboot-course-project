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
import project.ktc.springboot_app.course.dto.CourseResponseDto.CategoryInfo;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
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
        boolean canPublish = course.getIsApproved() && !course.getIsPublished();
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
}
