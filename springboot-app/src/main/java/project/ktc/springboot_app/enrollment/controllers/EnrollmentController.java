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
@PreAuthorize("hasAuthority('STUDENT')")
@Slf4j
@Tag(name = "Enrollment", description = "Course enrollment management")
public class EnrollmentController {

    private final EnrollmentServiceImp enrollmentService;

    @PostMapping("/api/courses/{id}/enroll")
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
}
