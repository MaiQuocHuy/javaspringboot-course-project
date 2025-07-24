package project.ktc.springboot_app.course.controllers;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.dto.CourseDashboardResponseDto;
import project.ktc.springboot_app.course.dto.CreateCourseDto;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
import project.ktc.springboot_app.course.enums.CourseInstructorStatus;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.interfaces.InstructorCourseService;
import project.ktc.springboot_app.utils.SecurityUtil;

@RestController
@Tag(name = "Instructor Courses API", description = "Endpoints for managing instructor-specific courses")
@RequestMapping("/api/instructor/courses")
@Validated
@RequiredArgsConstructor
@Slf4j
public class InstructorCourseController {

        private final InstructorCourseService instructorCourseService;

        @GetMapping
        @PreAuthorize("hasAuthority('INSTRUCTOR')")
        @Operation(summary = "Get instructor's courses", description = "Retrieve paginated list of courses created by the instructor with filtering options", security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CourseDashboardResponseDto>>> getInstructorCourses(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") int size,

                        @Parameter(description = "Search term for course title", example = "") @RequestParam(required = false) String search,

                        @Parameter(description = "Course status filter", schema = @Schema(implementation = CourseInstructorStatus.class)) @RequestParam(required = false) CourseInstructorStatus status,

                        @Parameter(description = "Sort criteria", example = "createdAt,DESC") @RequestParam(defaultValue = "createdAt,DESC") String sort) {

                log.info("Received request to get instructor courses with page: {}, size: {}, search: {}, status: {}, sort: {}",
                                page, size, search, status, sort);

                // Get authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                User currentUser = (User) authentication.getPrincipal();
                String instructorId = currentUser.getId();

                // Parse sort parameter
                Pageable pageable = createPageable(page, size, sort);

                // Get instructor courses
                ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CourseDashboardResponseDto>>> response = instructorCourseService
                                .findInstructorCourses(
                                                instructorId, search,
                                                Optional.ofNullable(status).map(Enum::name).orElse(null), pageable);

                return response;
        }

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAuthority('INSTRUCTOR')")
        @Operation(summary = "Create a new course", description = """
                        Create a new course with thumbnail upload for instructors.

                        **Fields:**
                        - `title` (text): Course title (required, 5-100 characters)
                        - `description` (text): Course description (required, 20-1000 characters)
                        - `price` (number): Course price (required, 0-9999.99)
                        - `categoryIds` (array): Category IDs (required, at least one category)
                        - `level` (text): Course level - BEGINNER, INTERMEDIATE, ADVANCED (required)
                        - `thumbnail` (file): Course thumbnail image (optional)

                        **Supported Image Formats:** JPEG, PNG, GIF, BMP, WebP
                        **Max Size:** 10MB

                        **Note:** For categoryIds, send multiple parameters with the same name, e.g., categoryIds=id1&categoryIds=id2
                        """, security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseResponseDto>> createCourse(
                        @Parameter(description = "Course title", required = true) @RequestParam("title") String title,
                        @Parameter(description = "Course description", required = true) @RequestParam("description") String description,
                        @Parameter(description = "Course price", required = true) @RequestParam("price") String price,
                        @Parameter(description = "Category IDs (can send multiple)", required = true) @RequestParam("categoryIds") java.util.List<String> categoryIds,
                        @Parameter(description = "Course level", required = true, schema = @Schema(implementation = CourseLevel.class)) @RequestParam("level") String level,
                        @Parameter(description = "Optional course thumbnail image file", required = false) @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile) {

                log.info("Received request to create course with title: {} and {} categories", title,
                                categoryIds.size());

                String instructorId = SecurityUtil.getCurrentUserId();

                // Create DTO from request parameters
                CreateCourseDto createCourseDto;
                try {
                        createCourseDto = CreateCourseDto.builder()
                                        .title(title)
                                        .description(description)
                                        .price(new java.math.BigDecimal(price))
                                        .categoryIds(categoryIds)
                                        .level(CourseLevel.valueOf(level.toUpperCase()))
                                        .build();
                } catch (NumberFormatException e) {
                        log.warn("Invalid price format: {}", price);
                        return ApiResponseUtil
                                        .badRequest("Invalid price format");
                } catch (IllegalArgumentException e) {
                        log.warn("Invalid input data: {}", e.getMessage());
                        return ApiResponseUtil
                                        .badRequest("Invalid input data: " + e.getMessage());
                }

                // Validate the DTO manually since we're not using @Valid annotation with
                // multipart
                if (title == null || title.trim().length() < 5 || title.trim().length() > 100) {
                        return ApiResponseUtil
                                        .badRequest("Title must be between 5 and 100 characters");
                }
                if (description == null || description.trim().length() < 20 || description.trim().length() > 1000) {
                        return ApiResponseUtil
                                        .badRequest("Description must be between 20 and 1000 characters");
                }
                if (categoryIds == null || categoryIds.isEmpty()) {
                        return ApiResponseUtil
                                        .badRequest("At least one category must be selected");
                }

                // Call service to create course
                return instructorCourseService.createCourse(createCourseDto, thumbnailFile, instructorId);
        }

        private Pageable createPageable(int page, int size, String sort) {
                // Parse sort parameter (e.g., "createdAt,DESC" or "title,ASC")
                String[] sortParts = sort.split(",");
                String sortProperty = sortParts.length > 0 ? sortParts[0] : "createdAt";
                Sort.Direction direction = sortParts.length > 1 && "ASC".equalsIgnoreCase(sortParts[1])
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                return PageRequest.of(page, size, Sort.by(direction, sortProperty));
        }
}
