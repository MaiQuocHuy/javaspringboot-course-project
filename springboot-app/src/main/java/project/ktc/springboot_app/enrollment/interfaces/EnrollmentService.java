package project.ktc.springboot_app.enrollment.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.enrollment.dto.EnrollmentResponseDto;
import project.ktc.springboot_app.enrollment.dto.MyEnrolledCourseDto;

public interface EnrollmentService {
    ResponseEntity<ApiResponse<EnrollmentResponseDto>> enroll(String courseId);
    ResponseEntity<ApiResponse<PaginatedResponse<MyEnrolledCourseDto>>> getMyCourses(String status, Pageable pageable);
}
