package project.ktc.springboot_app.enrollment.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import project.ktc.springboot_app.common.dto.ApiErrorResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.enrollment.dto.EnrollmentResponseDto;
import project.ktc.springboot_app.enrollment.dto.MyEnrolledCourseDto;
import project.ktc.springboot_app.enrollment.services.EnrollmentServiceImp;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enrollment", description = "Course enrollment management")
public class EnrollmentController {

    private final EnrollmentServiceImp enrollmentService;

    @PostMapping("/api/courses/{id}/enroll")
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Enroll in a course", description = "Enrolls the authenticated student into a specified course (free or paid). For paid courses, payment must be completed before enrollment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully enrolled in the course", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Course is unpublished or deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "402", description = "Payment not completed or amount insufficient", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course does not exist", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "User already enrolled in this course", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<EnrollmentResponseDto>> enroll(
            @Parameter(description = "Course ID", required = true) @PathVariable String id) {
        return enrollmentService.enroll(id);
    }

    @GetMapping("/api/enrollments/my-courses")
    @PreAuthorize("hasAuthority('STUDENT')")
    @Operation(summary = "Get my enrolled courses", description = "Retrieve a paginated list of all courses the currently authenticated student is enrolled in, including course metadata and progress status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Enrolled courses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have STUDENT role", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(
            @Parameter(description = "Filter by enrollment status (IN_PROGRESS, COMPLETED)", example = "IN_PROGRESS") @RequestParam(required = false) String status,

            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {

        Pageable pageable = PageRequest.of(page, size);
        log.info("Fetching enrolled courses with status filter: {} and pagination: {}", status, pageable);
        return enrollmentService.getMyCourses(status, pageable);
    }
}
