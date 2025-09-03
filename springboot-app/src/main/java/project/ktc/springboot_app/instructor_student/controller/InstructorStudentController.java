package project.ktc.springboot_app.instructor_student.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.common.dto.ApiResponse;
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

@RestController
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@RequestMapping("/api/instructor/enrolled-students")
public class InstructorStudentController {
  private final InstructorStudentService instructorStudentService;

  @GetMapping
  @Operation(summary = "Get instructor's enrolled students", description = "Retrieve a list of students enrolled in the instructor's courses", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<PaginatedResponse<InstructorStudentDto>>> getEnrolledStudents(
      @PageableDefault(size = 10) Pageable pageable) {
    return instructorStudentService.getEnrolledStudents(pageable);
  }

  @GetMapping("/{studentId}")
  @Operation(summary = "Get student details", description = "Retrieve details of a specific student", security = @SecurityRequirement(name = "bearerAuth"))
  public ResponseEntity<ApiResponse<InstructorStudentDetailsDto>> getEnrolledStudentDetails(
      @PathVariable String studentId) {
    return instructorStudentService.getEnrolledStudentDetails(studentId);
  }
}
