package project.ktc.springboot_app.course.interfaces;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseDashboardResponseDto;
import project.ktc.springboot_app.course.dto.CourseResponseDto;
import project.ktc.springboot_app.course.dto.CourseStatusUpdateResponseDto;
import project.ktc.springboot_app.course.dto.CreateCourseDto;
import project.ktc.springboot_app.course.dto.EnrolledStudentDto;
import project.ktc.springboot_app.course.dto.InstructorCourseDetailResponseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseStatusDto;
import project.ktc.springboot_app.course.entity.CourseReviewStatus.ReviewStatus;
import project.ktc.springboot_app.course.enums.CourseLevel;

public interface InstructorCourseService {

	ResponseEntity<ApiResponse<PaginatedResponse<CourseDashboardResponseDto>>> findInstructorCourses(
			String search,
			ReviewStatus status,
			List<String> categoryIds,
			Double minPrice,
			Double maxPrice,
			Integer rating,
			CourseLevel level,
			Boolean isPublished,
			Pageable pageable);

	ResponseEntity<ApiResponse<CourseResponseDto>> createCourse(
			CreateCourseDto createCourseDto, MultipartFile thumbnailFile, String instructorId);

	ResponseEntity<ApiResponse<CourseResponseDto>> updateCourse(
			String courseId,
			UpdateCourseDto updateCourseDto,
			MultipartFile thumbnailFile,
			String instructorId);

	ResponseEntity<ApiResponse<Void>> deleteCourse(String courseId, String instructorId);

	ResponseEntity<ApiResponse<CourseStatusUpdateResponseDto>> updateCourseStatus(
			String courseId, UpdateCourseStatusDto updateStatusDto, String instructorId);

	ResponseEntity<ApiResponse<InstructorCourseDetailResponseDto>> getCourseDetails(
			String courseId, String instructorId);

	ResponseEntity<ApiResponse<List<CourseDashboardResponseDto>>> getAllPublishedCourses(
			String instructorId);

	ResponseEntity<ApiResponse<PaginatedResponse<EnrolledStudentDto>>> getEnrolledStudents(
			String courseId, Pageable pageable);
}
