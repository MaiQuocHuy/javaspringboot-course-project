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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import project.ktc.springboot_app.course.dto.UpdateCourseDto;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseStatusDto;
import project.ktc.springboot_app.course.dto.CourseStatusUpdateResponseDto;
import project.ktc.springboot_app.course.enums.CourseInstructorStatus;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.services.InstructorCourseServiceImp;
import project.ktc.springboot_app.utils.SecurityUtil;

@RestController
@Tag(name = "Instructor Courses API", description = "Endpoints for managing instructor-specific courses")
@RequestMapping("/api/instructor/courses")
@Validated
@RequiredArgsConstructor
@Slf4j
public class InstructorCourseController {

        private final InstructorCourseServiceImp instructorCourseService;

        @GetMapping
        @PreAuthorize("hasRole('INSTRUCTOR')")
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
        @PreAuthorize("hasRole('INSTRUCTOR')")
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
                        @Parameter(description = " course thumbnail image file", required = true) @RequestPart(value = "thumbnail", required = true) MultipartFile thumbnailFile) {

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

        @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasRole('INSTRUCTOR')")
        @Operation(summary = "Update an existing course", description = """
                        Update an existing course with  thumbnail upload for instructors.

                        **Permission Requirements:**
                        - Course must not be approved yet (canEdit = !isApproved)
                        - Only the course owner can update it

                        **Fields:** (All fields)
                        - `title` (text): Course title (5-100 characters)
                        - `description` (text): Course description (20-1000 characters)
                        - `price` (number): Course price (0-9999.99)
                        - `categoryIds` (array): Category IDs (at least one if provided)
                        - `level` (text): Course level - BEGINNER, INTERMEDIATE, ADVANCED
                        - `thumbnail` (file): Course thumbnail image

                        **Supported Image Formats:** JPEG, PNG, GIF, BMP, WebP
                        **Max Size:** 10MB

                        **Note:** For categoryIds, send multiple parameters with the same name, e.g., categoryIds=id1&categoryIds=id2
                        """, security = @SecurityRequirement(name = "bearerAuth"))
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseResponseDto>> updateCourse(
                        @Parameter(description = "Course ID", required = true) @PathVariable("id") String courseId,
                        @Parameter(description = "Course title", required = false) @RequestParam(value = "title", required = false) String title,
                        @Parameter(description = "Course description", required = false) @RequestParam(value = "description", required = false) String description,
                        @Parameter(description = "Course price", required = false) @RequestParam(value = "price", required = false) String price,
                        @Parameter(description = "Category IDs (can send multiple)", required = false) @RequestParam(value = "categoryIds", required = false) java.util.List<String> categoryIds,
                        @Parameter(description = "Course level", required = false, schema = @Schema(implementation = CourseLevel.class)) @RequestParam(value = "level", required = false) String level,
                        @Parameter(description = "Course thumbnail image file", required = false) @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile) {

                log.info("Received request to update course {} with title: {}", courseId, title);

                String instructorId = SecurityUtil.getCurrentUserId();

                // Create DTO from request parameters
                UpdateCourseDto updateCourseDto = UpdateCourseDto.builder().build();

                try {
                        if (title != null && !title.trim().isEmpty()) {
                                updateCourseDto.setTitle(title.trim());
                        }

                        if (description != null && !description.trim().isEmpty()) {
                                updateCourseDto.setDescription(description.trim());
                        }

                        if (price != null && !price.trim().isEmpty()) {
                                updateCourseDto.setPrice(new java.math.BigDecimal(price));
                        }

                        if (categoryIds != null && !categoryIds.isEmpty()) {
                                updateCourseDto.setCategoryIds(categoryIds);
                        }

                        if (level != null && !level.trim().isEmpty()) {
                                updateCourseDto.setLevel(CourseLevel.valueOf(level.toUpperCase()));
                        }

                } catch (NumberFormatException e) {
                        log.warn("Invalid price format: {}", price);
                        return ApiResponseUtil.badRequest("Invalid price format");
                } catch (IllegalArgumentException e) {
                        log.warn("Invalid input data: {}", e.getMessage());
                        return ApiResponseUtil.badRequest("Invalid input data: " + e.getMessage());
                }

                // Validate fields if provided
                if (title != null && (title.trim().length() < 5 || title.trim().length() > 100)) {
                        return ApiResponseUtil.badRequest("Title must be between 5 and 100 characters");
                }

                if (description != null && (description.trim().length() < 20 || description.trim().length() > 1000)) {
                        return ApiResponseUtil.badRequest("Description must be between 20 and 1000 characters");
                }

                if (categoryIds != null && categoryIds.isEmpty()) {
                        return ApiResponseUtil
                                        .badRequest("At least one category must be selected when updating categories");
                }

                // Call service to update course
                return instructorCourseService.updateCourse(courseId, updateCourseDto, thumbnailFile, instructorId);
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasRole('INSTRUCTOR')")
        @Operation(summary = "Delete a course", description = """
                        Delete a course created by the instructor.

                        **Permission Requirements:**
                        - Course must not be approved yet (canDelete = !isApproved && enrollmentCount == 0)
                        - Course must not have any enrolled students
                        - Only the course owner can delete it

                        **Business Rules:**
                        - ✅ Instructor must be the owner of the course
                        - ❌ Course must not be approved
                        - ❌ Course must not have any enrolled students
                        """, security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course deleted successfully"),
                        @ApiResponse(responseCode = "400", description = "Cannot delete a course that is approved or has enrolled students"),
                        @ApiResponse(responseCode = "403", description = "You are not allowed to delete this course"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteCourse(
                        @Parameter(description = "Course ID", required = true) @PathVariable("id") String courseId) {

                log.info("Received request to delete course: {}", courseId);

                String instructorId = SecurityUtil.getCurrentUserId();

                // Call service to delete course
                return instructorCourseService.deleteCourse(courseId, instructorId);
        }

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('INSTRUCTOR')")
        @Operation(summary = "Update course status", description = """
                        Update the visibility status of a course between PUBLISHED and UNPUBLISHED.

                        **Permission Requirements:**
                        - Only the course owner can update its status

                        **Business Rules for PUBLISH:**
                        - Course must have complete details: title, description, thumbnail
                        - Course must have at least one section with lessons

                        **Business Rules for UNPUBLISH:**
                        - Course must be approved (isApproved = true)
                        - Course must be currently published (isPublished = true)

                        **Not allowed:**
                        - UNPUBLISH a course that has never been published
                        - Re-publish a course that is already published
                        """, security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course status updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Course does not meet the requirements for status change"),
                        @ApiResponse(responseCode = "403", description = "You are not allowed to update this course status"),
                        @ApiResponse(responseCode = "404", description = "Course not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseStatusUpdateResponseDto>> updateCourseStatus(
                        @Parameter(description = "Course ID", required = true) @PathVariable("id") String courseId,
                        @Parameter(description = "Status update request", required = true) @RequestBody @jakarta.validation.Valid UpdateCourseStatusDto updateStatusDto) {

                log.info("Received request to update course status: {} to {}", courseId, updateStatusDto.getStatus());

                String instructorId = SecurityUtil.getCurrentUserId();

                // Call service to update course status
                return instructorCourseService.updateCourseStatus(courseId, updateStatusDto, instructorId);
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
