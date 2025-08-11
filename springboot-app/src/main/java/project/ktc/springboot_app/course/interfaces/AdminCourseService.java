package project.ktc.springboot_app.course.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseReviewDetailResponseDto;
import project.ktc.springboot_app.course.dto.CourseReviewFilterDto;
import project.ktc.springboot_app.course.dto.CourseReviewResponseDto;
import project.ktc.springboot_app.course.dto.CourseReviewStatusUpdateResponseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseReviewStatusDto;

public interface AdminCourseService {
    ResponseEntity<ApiResponse<PaginatedResponse<CourseReviewResponseDto>>> getReviewCourses(
            CourseReviewFilterDto filterDto, Pageable pageable);

    ResponseEntity<ApiResponse<CourseReviewDetailResponseDto>> getCourseReviewDetail(String courseId);

    ResponseEntity<ApiResponse<CourseReviewStatusUpdateResponseDto>> updateCourseReviewStatus(
            String courseId, UpdateCourseReviewStatusDto updateDto, String reviewerEmail);
}