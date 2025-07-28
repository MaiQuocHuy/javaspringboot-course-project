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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseAdminResponseDto;
import project.ktc.springboot_app.course.dto.CourseApprovalResponseDto;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.interfaces.CourseService;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Course Management API", description = "APIs for admin course management")
@SecurityRequirement(name = "bearerAuth")
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all courses for admin", description = "Retrieves a paginated list of all courses with filtering and sorting options. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Courses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
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

        return courseService.findCoursesForAdmin(status, categoryId, search, minPrice, maxPrice, level, pageable);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a pending course", description = "Approves a course that is currently in pending status. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course approved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CourseApprovalResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Course already approved or deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseApprovalResponseDto>> approveCourse(
            @Parameter(description = "Course ID to approve", example = "course-uuid-123", required = true) @PathVariable String id) {
        log.info("Admin approving course with ID: {}", id);
        return courseService.approveCourse(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get course details for admin review", description = "Retrieves complete course details including sections and lessons for admin review. Only accessible by admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Course details retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing authentication token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<SectionWithLessonsDto>>> getCourseDetailsForAdmin(
            @Parameter(description = "Course ID to retrieve details for", example = "course-uuid-123", required = true) @PathVariable String id) {
        log.info("Admin retrieving course details for course ID: {}", id);
        return courseService.getCourseDetailsForAdmin(id);
    }
}
