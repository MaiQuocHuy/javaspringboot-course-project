package project.ktc.springboot_app.course.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseAdminResponseDto;
import project.ktc.springboot_app.course.dto.CourseApprovalResponseDto;
import project.ktc.springboot_app.course.dto.CourseReviewDetailResponseDto;
import project.ktc.springboot_app.course.dto.CourseReviewFilterDto;
import project.ktc.springboot_app.course.dto.CourseReviewResponseDto;
import project.ktc.springboot_app.course.dto.CourseReviewStatusUpdateResponseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseReviewStatusDto;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.services.AdminCourseServiceImp;
import project.ktc.springboot_app.course.services.CourseServiceImp;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Course Management API", description = "APIs for admin course management")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminCourseController {

        private final CourseServiceImp courseService;
        private final AdminCourseServiceImp adminCourseService;

        @GetMapping
        @PreAuthorize("hasPermission('Course', 'course:READ')")
        @Operation(summary = "Get all courses for admin", description = "Retrieves a paginated list of all courses with filtering and sorting options. Only accessible by admins.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Courses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - course:read permission required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CourseAdminResponseDto>>> findCoursesForAdmin(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

                        @Parameter(description = "Filter by approval status", example = "true") @RequestParam(required = false) Boolean status,

                        @Parameter(description = "Sort criteria (format: field,direction)", example = "createdAt,asc") @RequestParam(defaultValue = "createdAt,asc") String sort,

                        @Parameter(description = "Filter by category ID", example = "") @RequestParam(required = false) String categoryId,

                        @Parameter(description = "Search by course title, description, or instructor name", example = "") @RequestParam(required = false) String search,

                        @Parameter(description = "Minimum course price", example = "0.00") @RequestParam(required = false) @DecimalMin(value = "0.0", inclusive = true) BigDecimal minPrice,

                        @Parameter(description = "Maximum course price", example = "999.99") @RequestParam(required = false) @DecimalMin(value = "0.0", inclusive = true) BigDecimal maxPrice,

                        @Parameter(description = "Filter by course level", example = "") @RequestParam(required = false) CourseLevel level) {
                log.info(
                                "Admin retrieving courses with filters: status={}, categoryId={}, search={}, minPrice={}, maxPrice={}, level={}, page={}, size={}",
                                status, categoryId, search, minPrice, maxPrice, level, page, size);

                // Parse sort parameter
                String[] sortParts = sort.split(",");
                String sortField = sortParts.length > 0 ? sortParts[0] : "createdAt";
                String sortDirection = sortParts.length > 1 ? sortParts[1] : "asc";

                Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;

                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

                return courseService.findCoursesForAdmin(status, categoryId, search, minPrice, maxPrice, level,
                                pageable);
        }

        @PatchMapping("/{id}/approve")
        @PreAuthorize("hasPermission('Course', 'course:APPROVE')")
        @Operation(summary = "Approve a pending course", description = "Approves a course that is currently in pending status. Only accessible by admins.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course approved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseApprovalResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Course already approved or deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - course:approve permission required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseApprovalResponseDto>> approveCourse(
                        @Parameter(description = "Course ID to approve", example = "course-uuid-123", required = true) @PathVariable String id) {
                log.info("Admin approving course with ID: {}", id);
                return courseService.approveCourse(id);
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasPermission('Course', 'course:READ')")
        @Operation(summary = "Get course details for admin review", description = "Retrieves complete course details including sections and lessons for admin review. Only accessible by admins.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - course:read permission required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<SectionWithLessonsDto>>> getCourseDetailsForAdmin(
                        @Parameter(description = "Course ID to retrieve details for", example = "course-uuid-123", required = true) @PathVariable String id) {
                log.info("Admin retrieving course details for course ID: {}", id);
                return courseService.getCourseDetailsForAdmin(id);
        }

        /**
         * Retrieves courses for review based on filtering criteria.
         * If no status is provided, defaults to PENDING and RESUBMITTED courses.
         * 
         * @param status    Comma-separated list of statuses to filter by
         * @param createdBy User ID of the course creator
         * @param dateFrom  Start date for filtering by creation date
         * @param dateTo    End date for filtering by creation date
         * @param page      Page number (default: 0)
         * @param size      Page size (default: 10)
         * @param sort      Sort criteria (default: "createdAt,desc")
         * @return Paginated list of courses for review
         */
        @GetMapping("/review-course")
        @PreAuthorize("hasPermission('Course', 'course:READ')")
        @Operation(summary = "Get courses for review", description = "Retrieves a paginated list of courses filtered by review status (defaults to PENDING and RESUBMITTED), creator, and creation date range. Admins only.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Pending review courses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<CourseReviewResponseDto>>> getReviewCourses(
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String createdBy,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
                        @RequestParam(defaultValue = "0") @Min(0) Integer page,
                        @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
                        @RequestParam(defaultValue = "createdAt,desc") String sort) {

                log.info("Admin request to get review courses - status: {}, createdBy: {}, page: {}, size: {}",
                                status, createdBy, page, size);
                // Parse comma-separated status values
                List<String> statusList = null;
                if (status != null && !status.trim().isEmpty()) {
                        statusList = Arrays.asList(status.split(","));
                }

                CourseReviewFilterDto filterDto = CourseReviewFilterDto.builder()
                                .status(statusList)
                                .createdBy(createdBy)
                                .dateFrom(dateFrom)
                                .dateTo(dateTo)
                                .build();

                // build pageable from sort like other controllers
                Sort.Direction sortDirection = Sort.Direction.ASC;
                String sortField = "createdAt";
                if (sort != null && sort.contains(",")) {
                        String[] sortParams = sort.split(",");
                        sortField = sortParams[0];
                        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
                                sortDirection = Sort.Direction.DESC;
                        }
                }
                Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

                return adminCourseService.getReviewCourses(filterDto, pageable);
        }

        @GetMapping("/review-course/{id}")
        @PreAuthorize("hasPermission('Course', 'course:READ')")
        @Operation(summary = "Get course review detail by ID", description = "Retrieves detailed course information for review including sections, lessons, videos, and quiz questions. Admins only.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course review detail retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseReviewDetailResponseDto>> getCourseReviewDetail(
                        @Parameter(description = "Course ID to retrieve review details for", example = "course-uuid-123", required = true) @PathVariable String id) {
                log.info("Admin request to get course review detail for course ID: {}", id);
                return adminCourseService.getCourseReviewDetail(id);
        }

        @PatchMapping("/review-course/{id}")
        @PreAuthorize("hasPermission('Course', 'course:APPROVE')")
        @Operation(summary = "Update course review status", description = "Updates the review status of a specific course by its ID. Only Admin role is allowed. If status is DENIED, a reason must be provided.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course review status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Bad Request - Invalid status value or missing reason for DENIED status", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseReviewStatusUpdateResponseDto>> updateCourseReviewStatus(
                        @Parameter(description = "Course ID to update review status for", example = "course-uuid-123", required = true) @PathVariable String id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Course review status update request", required = true) @RequestBody @Validated UpdateCourseReviewStatusDto updateDto,
                        java.security.Principal principal) {
                log.info("Admin request to update course review status - courseId: {}, status: {}, admin: {}",
                                id, updateDto.getStatus(), principal.getName());
                return adminCourseService.updateCourseReviewStatus(id, updateDto, principal.getName());
        }

}
