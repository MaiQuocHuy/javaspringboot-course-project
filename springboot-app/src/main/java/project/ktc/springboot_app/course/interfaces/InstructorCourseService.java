package project.ktc.springboot_app.course.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseDashboardResponseDto;
import project.ktc.springboot_app.course.dto.CreateCourseDto;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseStatusDto;
import project.ktc.springboot_app.course.dto.CourseStatusUpdateResponseDto;

public interface InstructorCourseService {

    ResponseEntity<ApiResponse<PaginatedResponse<CourseDashboardResponseDto>>> findInstructorCourses(
            String instructorId,
            String search,
            String status,
            Pageable pageable);

    ResponseEntity<ApiResponse<CourseResponseDto>> createCourse(
            CreateCourseDto createCourseDto,
            MultipartFile thumbnailFile,
            String instructorId);

    ResponseEntity<ApiResponse<CourseResponseDto>> updateCourse(
            String courseId,
            UpdateCourseDto updateCourseDto,
            MultipartFile thumbnailFile,
            String instructorId);

    ResponseEntity<ApiResponse<Void>> deleteCourse(
            String courseId,
            String instructorId);

    ResponseEntity<ApiResponse<CourseStatusUpdateResponseDto>> updateCourseStatus(
            String courseId,
            UpdateCourseStatusDto updateStatusDto,
            String instructorId);

}
