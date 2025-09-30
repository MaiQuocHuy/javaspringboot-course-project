package project.ktc.springboot_app.course.interfaces;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseAdminResponseDto;
import project.ktc.springboot_app.course.dto.CourseApprovalResponseDto;
import project.ktc.springboot_app.course.dto.CourseDetailResponseDto;
import project.ktc.springboot_app.course.dto.CourseFilterMetadataResponseDto;
import project.ktc.springboot_app.course.dto.CoursePublicResponseDto;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

public interface CourseService {
	ResponseEntity<ApiResponse<PaginatedResponse<CoursePublicResponseDto>>> findAllPublic(
			String search,
			List<String> categoryIds,
			BigDecimal minPrice,
			BigDecimal maxPrice,
			CourseLevel level,
			Double averageRating,
			Pageable pageable);

	ResponseEntity<ApiResponse<CourseDetailResponseDto>> findOnePublic(String courseId);

	ResponseEntity<ApiResponse<CourseDetailResponseDto>> findOneBySlug(String slug);

	ResponseEntity<ApiResponse<PaginatedResponse<CourseAdminResponseDto>>> findCoursesForAdmin(
			Boolean isApproved,
			List<String> categoryIds,
			String search,
			BigDecimal minPrice,
			BigDecimal maxPrice,
			CourseLevel level,
			Double averageRating,
			Pageable pageable);

	ResponseEntity<ApiResponse<CourseApprovalResponseDto>> approveCourse(String courseId);

	ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseDetailsForAdmin(
			String courseId);

	ResponseEntity<ApiResponse<CourseFilterMetadataResponseDto>> getCourseFilterMetadata();
}
