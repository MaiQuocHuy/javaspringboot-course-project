package project.ktc.springboot_app.refund.controllers;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.refund.dto.RefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.InstructorRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.InstructorRefundResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;
import project.ktc.springboot_app.refund.interfaces.InstructorRefundService;

@RestController
@RequestMapping("/api/instructor/refund")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Instructor Refunds API", description = "APIs for managing instructor refunds")
public class InstructorRefundController {
        private final InstructorRefundService instructorRefundService;

        /**
         * Get all refunds with pagination
         * 
         * @param pageable Pagination parameters
         * @return ResponseEntity containing paginated list of refunds
         */
        @GetMapping
        @Operation(summary = "Get all refunds", description = """
                        Retrieves all refunds in the system with pagination support and advanced filtering for instructor view.

                        **Features:**
                        - Returns all refunds for instructor's courses
                        - Includes user and course information for each refund
                        - Refunds are ordered by creation date (most recent first)
                        - Supports pagination for better performance
                        - Shows refund status, amount, and method
                        - Advanced search and filtering capabilities

                        **Search & Filter Options:**
                        - Search by refund ID, user name, or reason
                        - Filter by refund status (PENDING, COMPLETED, FAILED)
                        - Filter by creation date range

                        **Instructor Only:**
                        - This endpoint requires INSTRUCTOR role
                        - Provides complete refund overview for instructor purposes
                        - Includes sensitive refund information for audit purposes
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<InstructorRefundResponseDto>>> getAllRefundsByInstructorId(
                        @Parameter(description = "Search by refund ID, user name, or reason") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by refund status", example = "COMPLETED") @RequestParam(required = false) project.ktc.springboot_app.refund.entity.Refund.RefundStatus status,

                        @Parameter(description = "Filter by creation date from (ISO format: yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String fromDate,

                        @Parameter(description = "Filter by creation date to (ISO format: yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) String toDate,

                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {

                Pageable pageable = PageRequest.of(page, size);
                log.info("Instructor requesting all refunds with pagination: page={}, size={}, search={}, status={}, fromDate={}, toDate={}",
                                page, size, search, status, fromDate, toDate);
                return instructorRefundService.getAllRefundsByInstructorId(search, status, fromDate, toDate, pageable);
        }

        /**
         * Get all refunds without pagination
         * 
         * @return ResponseEntity containing list of all refunds
         */
        @GetMapping("/all")
        @Operation(summary = "Get all refunds (no pagination)", description = """
                        Retrieves all refunds in the system without pagination for instructor view with search and filtering.

                        **Warning:** This endpoint returns all refunds at once and should be used carefully
                        for systems with large numbers of refunds as it may impact performance.

                        **Search & Filter Options:**
                        - Search by refund ID, user name, or reason
                        - Filter by refund status (PENDING, COMPLETED, FAILED)
                        - Filter by creation date range

                        **Use cases:**
                        - Generating reports that need complete data
                        - Exporting refund data
                        - Small systems with limited refund records

                        **Instructor Only:**
                        - This endpoint requires INSTRUCTOR role
                        - Returns complete refund data for instructor's courses
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All refunds retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<InstructorRefundResponseDto>>> getAllRefundsByInstructorIdAll(
                        @Parameter(description = "Search by refund ID, user name, or reason") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by refund status", example = "COMPLETED") @RequestParam(required = false) project.ktc.springboot_app.refund.entity.Refund.RefundStatus status,

                        @Parameter(description = "Filter by creation date from (ISO format: yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) String fromDate,

                        @Parameter(description = "Filter by creation date to (ISO format: yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) String toDate) {
                log.info("Instructor requesting all refunds without pagination with filters: search={}, status={}, fromDate={}, toDate={}",
                                search, status, fromDate, toDate);
                return instructorRefundService.getAllRefundsByInstructorId(search, status, fromDate, toDate);
        }

        /**
         * Get refund details by ID
         * 
         * @param refundId The refund ID to retrieve
         * @return ResponseEntity containing detailed refund information
         */
        @GetMapping("/{refundId}")
        @Operation(summary = "Get refund details", description = """
                        Retrieves detailed information about a specific refund by ID for instructor view.

                        **Features:**
                        - Returns comprehensive refund information
                        - Includes user and course details
                        - Shows Stripe payment data if applicable (transaction ID, receipt URL, card info)
                        - Provides complete audit trail information

                        **Instructor Only:**
                        - This endpoint requires INSTRUCTOR role
                        - Access to any refund regardless of user ownership
                        - Includes sensitive payment gateway information
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Refund details retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required"),
                        @ApiResponse(responseCode = "404", description = "Refund not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<InstructorRefundDetailsResponseDto>> getRefundByIdAndInstructorIdWithDetails(
                        @Parameter(description = "Refund ID", required = true) @PathVariable String refundId) {
                log.info("Instructor requesting refund details for refund: {}", refundId);
                return instructorRefundService.getRefundByIdAndInstructorIdWithDetails(refundId);
        }

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('INSTRUCTOR')")
        @Operation(summary = "Update refund status", description = "Updates the status of a specific refund by its ID. Only INSTRUCTOR role is allowed.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Refund status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RefundStatusUpdateResponseDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value or refund not in PENDING state", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Instructor role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Refund not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<RefundStatusUpdateResponseDto>> updateRefundStatus(
                        @Parameter(description = "The ID of the refund to update", required = true) @PathVariable String id,
                        @Parameter(description = "The status update request", required = true) @Valid @RequestBody UpdateRefundStatusDto updateDto) {

                log.info("Instructor refund status update request for ID: {} with status: {}", id,
                                updateDto.getStatus());
                return instructorRefundService.updateRefundStatus(id, updateDto);
        }
}
