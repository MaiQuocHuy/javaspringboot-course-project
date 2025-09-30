package project.ktc.springboot_app.enrollment.interfaces;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.enrollment.dto.EnrollmentResponseDto;
import project.ktc.springboot_app.enrollment.dto.MyEnrolledCourseDto;
import project.ktc.springboot_app.enrollment.dto.StudentActivityDto;
import project.ktc.springboot_app.enrollment.dto.StudentDashboardStatsDto;
import project.ktc.springboot_app.enrollment.entity.Enrollment;

public interface EnrollmentService {
  ResponseEntity<ApiResponse<EnrollmentResponseDto>> enroll(String courseId);

  ResponseEntity<ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(
      Enrollment.CompletionStatus status, Pageable pageable);

  ResponseEntity<ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(
      String search,
      String progressFilter,
      Enrollment.CompletionStatus status,
      String sortBy,
      String sortDirection,
      Pageable pageable);

  ResponseEntity<ApiResponse<List<MyEnrolledCourseDto>>> getMyCourses(
      Enrollment.CompletionStatus status);

  ResponseEntity<ApiResponse<List<MyEnrolledCourseDto>>> getMyCourses(
      String search, String progressFilter, Enrollment.CompletionStatus status);

  ResponseEntity<ApiResponse<List<MyEnrolledCourseDto>>> getRecentCourses();

  ResponseEntity<ApiResponse<StudentDashboardStatsDto>> getDashboardStats();

  ResponseEntity<ApiResponse<List<StudentActivityDto>>> getRecentActivities(Integer limit);

  /**
   * Creates enrollment from webhook (bypassing authentication)
   *
   * @param userId The user ID to enroll
   * @param courseId The course ID to enroll in
   * @param stripeSessionId The Stripe session ID for reference
   */
  void createEnrollmentFromWebhook(String userId, String courseId, String stripeSessionId);
}
