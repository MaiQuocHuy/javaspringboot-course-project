package project.ktc.springboot_app.enrollment.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.enrollment.dto.EnrollmentResponseDto;
import project.ktc.springboot_app.enrollment.dto.MyEnrolledCourseDto;
import project.ktc.springboot_app.enrollment.entity.Enrollment;

public interface EnrollmentService {
    ResponseEntity<ApiResponse<EnrollmentResponseDto>> enroll(String courseId);

    ResponseEntity<ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(Enrollment.CompletionStatus status,
            Pageable pageable);

    /**
     * Creates enrollment from webhook (bypassing authentication)
     * 
     * @param userId          The user ID to enroll
     * @param courseId        The course ID to enroll in
     * @param stripeSessionId The Stripe session ID for reference
     */
    void createEnrollmentFromWebhook(String userId, String courseId, String stripeSessionId);
}
