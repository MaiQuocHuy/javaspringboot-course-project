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

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.refund.dto.AdminRefundDetailsResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundStatisticsResponseDto;
import project.ktc.springboot_app.refund.dto.AdminRefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;
import project.ktc.springboot_app.refund.interfaces.AdminRefundService;

@RestController
@RequestMapping("/api/admin/refund")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Refund API", description = "Endpoints for admin refund management")
public class AdminRefundController {

        private final AdminRefundService adminRefundService;

        /**
         * Get all refunds with pagination
         * 
         * @param pageable Pagination parameters
         * @return ResponseEntity containing paginated list of refunds
         */
        @GetMapping
        @Operation(summary = "Get all refunds", description = """
                        Retrieves all refunds in the system with pagination support for admin view.

                        **Features:**
                        - Returns all refunds across all users
                        - Includes user and course information for each refund
                        - Refunds are ordered by creation date (most recent first)
                        - Supports pagination for better performance
                        - Shows refund status, amount, and method

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Provides complete refund overview for administrative purposes
                        - Includes sensitive refund information for audit purposes
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<AdminRefundResponseDto>>> getAllRefunds(
                        @PageableDefault(size = 10) Pageable pageable) {
                log.info("Admin retrieving all refunds with pagination: page={}, size={}", pageable.getPageNumber(),
                                pageable.getPageSize());
                return adminRefundService.getAllRefunds(pageable);
        }

        /**
         * Get all refunds without pagination
         * 
         * @return ResponseEntity containing list of all refunds
         */
        @GetMapping("/all")
        @Operation(summary = "Get all refunds (no pagination)", description = """
                        Retrieves all refunds in the system without pagination for admin view.

                        **Warning:** This endpoint returns all refunds at once and should be used carefully
                        for systems with large numbers of refunds as it may impact performance.

                        **Use cases:**
                        - Generating reports that need complete data
                        - Exporting refund data
                        - Small systems with limited refund records

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Returns complete refund data for all users
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "All refunds retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<AdminRefundResponseDto>>> getAllRefunds() {
                log.info("Admin requesting all refunds without pagination");
                return adminRefundService.getAllRefunds();
        }

        /**
         * Get refund details by ID
         * 
         * @param refundId The refund ID to retrieve
         * @return ResponseEntity containing detailed refund information
         */
        @GetMapping("/{refundId}")
        @Operation(summary = "Get refund details", description = """
                        Retrieves detailed information about a specific refund by ID for admin view.

                        **Features:**
                        - Returns comprehensive refund information
                        - Includes user and course details
                        - Shows Stripe payment data if applicable (transaction ID, receipt URL, card info)
                        - Provides complete audit trail information

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Access to any refund regardless of user ownership
                        - Includes sensitive payment gateway information
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Refund details retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "404", description = "Refund not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminRefundDetailsResponseDto>> getRefundDetail(
                        @Parameter(description = "Refund ID", required = true) @PathVariable String refundId) {
                log.info("Admin requesting refund details for refund: {}", refundId);
                return adminRefundService.getRefundDetail(refundId);
        }

        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update refund status", description = "Updates the status of a specific refund by its ID. Only ADMIN role is allowed.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Refund status updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AdminRefundStatusUpdateResponseDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status value or refund not in PENDING state", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin role required", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Refund not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminRefundStatusUpdateResponseDto>> updateRefundStatus(
                        @Parameter(description = "The ID of the refund to update", required = true) @PathVariable String id,
                        @Parameter(description = "The status update request", required = true) @Valid @RequestBody UpdateRefundStatusDto updateDto) {

                log.info("Admin refund status update request for ID: {} with status: {}", id, updateDto.getStatus());
                return adminRefundService.updateRefundStatus(id, updateDto);
        }

        /**
         * Get refund statistics for admin dashboard
         * 
         * @return ResponseEntity containing refund counts by status
         */
        @GetMapping("/statistics")
        @Operation(summary = "Get refund statistics", description = """
                        **Purpose:**
                        Retrieves comprehensive statistics about refunds for admin dashboard.

                        **Response Includes:**
                        - Refund counts by status (total, pending, completed, failed)

                        **Use Cases:**
                        - Admin dashboard overview
                        - Financial reporting
                        - System health monitoring

                        **Admin Only:**
                        - This endpoint requires ADMIN role
                        - Provides system-wide statistics
                        """)
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<AdminRefundStatisticsResponseDto>> getRefundStatistics() {
                log.info("Admin retrieving refund statistics");
                return adminRefundService.getRefundStatistics();
        }
}
