package project.ktc.springboot_app.log.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.ktc.springboot_app.log.dto.CreateLogDto;
import project.ktc.springboot_app.log.dto.LogResponseDto;
import project.ktc.springboot_app.log.services.LogServiceImp;

/**
 * REST Controller for admin system log operations
 * Requires ADMIN role for all endpoints
 */
@RestController
@RequestMapping("/api/admin/logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Log API", description = "API for managing system logs (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminLogController {

        private final LogServiceImp logService;

        /**
         * Creates a new system log entry
         * 
         * @param createLogDto The log data to create
         * @return ResponseEntity with the created log information
         */
        @PostMapping
        @Operation(summary = "Create system log entry", description = "Creates a new system log entry to track admin actions")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Log entry created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request data"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have ADMIN role"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<LogResponseDto>> createLog(
                        @Valid @RequestBody CreateLogDto createLogDto) {

                log.info("Admin creating system log entry - Action: {}, EntityType: {}",
                                createLogDto.getAction(), createLogDto.getEntityType());

                return logService.createLog(createLogDto);
        }
}
