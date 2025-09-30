package project.ktc.springboot_app.refund.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.common.dto.ApiErrorResponse;
import project.ktc.springboot_app.refund.dto.RefundRequestDto;
import project.ktc.springboot_app.refund.dto.RefundResponseDto;
import project.ktc.springboot_app.refund.interfaces.StudentRefundService;

/** REST Controller for student refund operations */
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Student Refund API",
    description = "Endpoints for students to request refunds for purchased courses")
public class StudentRefundController {

  private final StudentRefundService studentRefundService;

  /** Request a refund for a purchased course */
  @PostMapping("/courses/{id}/refund-request")
  @Operation(
      summary = "Request a refund for a purchased course",
      description =
          "Students can request a refund within 3 days of payment completion for courses they have purchased")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Refund request submitted successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            implementation =
                                project.ktc.springboot_app.common.dto.ApiResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - refund not allowed",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Course or payment not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Refund request already exists",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApiErrorResponse.class)))
      })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<RefundResponseDto>>
      requestRefund(
          @Parameter(description = "Course ID", required = true) @PathVariable("id")
              String courseId,
          @Valid @RequestBody RefundRequestDto refundRequestDto) {

    log.info("Student refund request received for course: {}", courseId);

    return studentRefundService.requestRefund(courseId, refundRequestDto);
  }
}
