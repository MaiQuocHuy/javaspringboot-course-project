package project.ktc.springboot_app.instructor_dashboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.instructor_dashboard.dto.InsDashboardDto;
import project.ktc.springboot_app.instructor_dashboard.interfaces.InsDashboardService;

@RestController
@RequestMapping("/api/instructor")
@RequiredArgsConstructor
@Tag(name = "Instructor Dashboard API", description = "API for instructor dashboard functionalities")
public class InsDashboardController {

  private final InsDashboardService insDashboardService;

  @GetMapping("/statistics")
  @PreAuthorize("hasRole('INSTRUCTOR')")
  @Operation(summary = "Get instructor's statistics", description = "Retrieve statistics related to the instructor's performance and activities", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<InsDashboardDto>> getInstructorsStatistics() {
    return insDashboardService.getInsDashboardStatistics();
  }
}
