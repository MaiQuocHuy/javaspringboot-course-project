package project.ktc.springboot_app.instructor_student.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDetailsDto;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDto;
import project.ktc.springboot_app.instructor_student.interfaces.InstructorStudentService;

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
			@Parameter(description = "Search term for student name or email") @RequestParam(required = false) String search,
			// @PageableDefault(size = 10, sort = "createdAt,DESC", page = 0) Pageable
			// pageable,
			@Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be greater than or equal to 0") int page,
			@Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") int size,
			@Parameter(description = "Sort criteria", example = "createdAt,DESC") @RequestParam(defaultValue = "createdAt,DESC") String sort) {

		Pageable pageable = createPageable(page, size, sort);
		return instructorStudentService.getEnrolledStudents(search, pageable);
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

	@GetMapping("/count")
	@Operation(summary = "Get number of enrolled students", description = "Retrieve the number of students enrolled in the instructor's courses", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Number of enrolled students retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
			@ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Long>> getNumOfEnrolledStudent() {
		return instructorStudentService.getNumOfEnrolledStudent();
	}

	private Pageable createPageable(int page, int size, String sort) {
		// Parse sort parameter (e.g., "createdAt,DESC" or "title,ASC")
		String[] sortParts = sort.split(",");
		String sortProperty = sortParts.length > 0 ? sortParts[0] : "createdAt";
		Sort.Direction direction = sortParts.length > 1 && "ASC".equalsIgnoreCase(sortParts[1])
				? Sort.Direction.ASC
				: Sort.Direction.DESC;

		return PageRequest.of(page, size, Sort.by(direction, sortProperty));
	}
}
