package project.ktc.springboot_app.course.interfaces;

import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CourseDetailResponseDto;
import project.ktc.springboot_app.course.dto.CoursePublicResponseDto;
import project.ktc.springboot_app.course.enums.CourseLevel;

public interface CourseService {
    ResponseEntity<ApiResponse<PaginatedResponse<CoursePublicResponseDto>>> findAllPublic(
            String search,
            String categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CourseLevel level,
            Pageable pageable);

    ResponseEntity<ApiResponse<CourseDetailResponseDto>> findOnePublic(String courseId);
}
