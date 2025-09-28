package project.ktc.springboot_app.course.controllers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import project.ktc.springboot_app.common.dto.ApiErrorResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseProgressDto;
import project.ktc.springboot_app.course.services.StudentCourseServiceImp;
import project.ktc.springboot_app.enrollment.dto.MyEnrolledCourseDto;
import project.ktc.springboot_app.enrollment.dto.StudentActivityDto;
import project.ktc.springboot_app.enrollment.dto.StudentDashboardStatsDto;
import project.ktc.springboot_app.enrollment.entity.Enrollment;
import project.ktc.springboot_app.enrollment.services.EnrollmentServiceImp;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/student/courses")
@Tag(name = "Student Courses API", description = "Endpoints for managing student courses")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
public class StudentCourseController {
        private final EnrollmentServiceImp enrollmentService;
        private final StudentCourseServiceImp studentCourseService;

        @GetMapping()
        @Operation(summary = "Get my enrolled courses", description = "Retrieve a paginated list of all courses the currently authenticated student is enrolled in, including course metadata and progress status with search, filter, and sort capabilities.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Enrolled courses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have STUDENT role", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(
                        @Parameter(description = "Search by course title or instructor name") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by course progress (completed: 100% progress, in_progress: 0-99% progress, not_started: 0% progress)", example = "completed") @RequestParam(required = false) String progressFilter,

                        @Parameter(description = "Filter by enrollment status (IN_PROGRESS, COMPLETED)", example = "IN_PROGRESS") @RequestParam(required = false) Enrollment.CompletionStatus status,

                        @Parameter(description = "Sort by field (recent, progress, title, instructor)", example = "recent") @RequestParam(defaultValue = "recent") String sortBy,

                        @Parameter(description = "Sort direction (asc, desc)", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection,

                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) Integer page,

                        @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size) {

                Pageable pageable = PageRequest.of(page, size);
                log.info("Fetching enrolled courses with search: {}, progressFilter: {}, status: {}, sortBy: {}, sortDirection: {}, pagination: {}",
                                search, progressFilter, status, sortBy, sortDirection, pageable);
                return enrollmentService.getMyCourses(search, progressFilter, status, sortBy, sortDirection, pageable);
        }

        @GetMapping("/all")
        @Operation(summary = "Get my enrolled courses without pagination", description = "Retrieve a list of all courses the currently authenticated student is enrolled in, including course metadata and progress status with search and filter capabilities.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Enrolled courses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have STUDENT role", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<MyEnrolledCourseDto>>> getMyCoursesAll(
                        @Parameter(description = "Search by course title or instructor name") @RequestParam(required = false) String search,

                        @Parameter(description = "Filter by course progress (completed: 100% progress, in_progress: 0-99% progress, not_started: 0% progress)", example = "completed") @RequestParam(required = false) String progressFilter,

                        @Parameter(description = "Filter by enrollment status (IN_PROGRESS, COMPLETED)", example = "IN_PROGRESS") @RequestParam(required = false) Enrollment.CompletionStatus status

        ) {

                log.info("Fetching all enrolled courses with search: {}, progressFilter: {}, status: {}", search,
                                progressFilter, status);
                return enrollmentService.getMyCourses(search, progressFilter, status);
        }

        @GetMapping("/recent")
        @Operation(summary = "Get 3 most recent enrolled courses", description = "Retrieve the 3 most recently enrolled courses for the currently authenticated student, ordered by enrollment date.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Recent courses retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have STUDENT role", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<MyEnrolledCourseDto>>> getRecentCourses() {
                log.info("Fetching 3 most recent enrolled courses for current student");
                return enrollmentService.getRecentCourses();
        }

        @GetMapping("/dashboard-stats")
        @Operation(summary = "Get student dashboard statistics", description = "Retrieve comprehensive dashboard statistics for the currently authenticated student including total courses, completed courses, in-progress courses, and lessons completed.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have STUDENT role", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<StudentDashboardStatsDto>> getDashboardStats() {
                log.info("Fetching dashboard statistics for current student");
                return enrollmentService.getDashboardStats();
        }

        @GetMapping("/recent-activities")
        @Operation(summary = "Get recent student activities", description = "Retrieve recent activities for the currently authenticated student including course enrollments, lesson completions, and quiz submissions.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Recent activities retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User does not have STUDENT role", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<StudentActivityDto>>> getRecentActivities(
                        @Parameter(description = "Maximum number of activities to return (default: 20)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
                log.info("Fetching recent activities for current student with limit: {}", limit);
                return enrollmentService.getRecentActivities(limit);
        }

        @GetMapping("/{id}")
        @Operation(summary = "Get course sections and lessons", description = "Retrieve all sections and lessons of a specific course for the enrolled student.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Sections retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not enrolled in this course", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
                        @Parameter(description = "Course ID", required = true) @PathVariable String id) {

                log.info("Fetching sections for course: {}", id);
                return studentCourseService.getCourseSections(id);
        }

        @GetMapping("/{courseId}/structure")
        @Operation(summary = "Get course structure", description = "Retrieve the complete structure of a specific course for the enrolled student, including all sections, lessons, videos, and quiz questions.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Course structure retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not enrolled in this course", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<project.ktc.springboot_app.course.dto.CourseStructureSectionDto>>> getCourseStructure(
                        @Parameter(description = "Course ID", required = true) @PathVariable String courseId) {

                log.info("Fetching course structure for course: {}", courseId);
                return studentCourseService.getCourseStructureForStudent(courseId);
        }

        @GetMapping("/{courseId}/progress")
        @Operation(summary = "Get course progress", description = "Retrieve the real-time progress of a specific course for the enrolled student, including completion status and summary statistics.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Progress retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = project.ktc.springboot_app.common.dto.ApiResponse.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "403", description = "Forbidden - User is not enrolled in this course", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class))),
                        @ApiResponse(responseCode = "404", description = "Course not found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiErrorResponse.class)))
        })
        public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<CourseProgressDto>> getCourseProgress(
                        @Parameter(description = "Course ID", required = true) @PathVariable String courseId) {

                log.info("Fetching course progress for course: {}", courseId);
                return studentCourseService.getCourseProgressForStudent(courseId);
        }
}
