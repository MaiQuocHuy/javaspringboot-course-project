package project.ktc.springboot_app.instructor_student.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDetailsDto;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDto;
import project.ktc.springboot_app.instructor_student.interfaces.InstructorStudentService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Instructor's Enrolled Student Management", description = "Manage students enrolled in instructor's courses")
@RestController
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@RequestMapping("/api/instructor/enrolled-students")
public class InstructorStudentController {
  private final InstructorStudentService instructorStudentService;

  @Operation(summary = "Get instructor's enrolled students", description = "Retrieve a list of students enrolled in the instructor's courses", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Quiz scores retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  @GetMapping
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<InstructorStudentDto>>> getEnrolledStudents(
      @PageableDefault(size = 10, sort = "createdAt,DESC", page = 0) Pageable pageable) {
    return instructorStudentService.getEnrolledStudents(pageable);
  }

  @GetMapping("/{studentId}")
  @Operation(summary = "Get student details", description = "Retrieve details of a specific student", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Quiz scores retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
      @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
      @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<InstructorStudentDetailsDto>> getEnrolledStudentDetails(
      @PathVariable String studentId,
      @PageableDefault(size = 10, sort = "completedAt,desc", page = 0) Pageable pageable) {
    return instructorStudentService.getEnrolledStudentDetails(studentId, pageable);
  }
}
