package project.ktc.springboot_app.refund.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.refund.dto.AdminRefundStatusUpdateResponseDto;
import project.ktc.springboot_app.refund.dto.UpdateRefundStatusDto;
import project.ktc.springboot_app.refund.interfaces.AdminRefundService;

@RestController
@RequestMapping("/api/admin/refund")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Refund API", description = "Endpoints for admin refund management")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

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
    public ResponseEntity<ApiResponse<AdminRefundStatusUpdateResponseDto>> updateRefundStatus(
            @Parameter(description = "The ID of the refund to update", required = true) @PathVariable String id,
            @Parameter(description = "The status update request", required = true) @Valid @RequestBody UpdateRefundStatusDto updateDto) {

        log.info("Admin refund status update request for ID: {} with status: {}", id, updateDto.getStatus());
        return adminRefundService.updateRefundStatus(id, updateDto);
    }
}
