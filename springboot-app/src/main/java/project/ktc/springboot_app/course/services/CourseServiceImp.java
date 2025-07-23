package project.ktc.springboot_app.course.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.course.dto.CoursePublicResponseDto;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.entity.Category;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImp {
    private final CourseRepository courseRepository;

    public PaginatedResponse<CoursePublicResponseDto> findAllPublic(
            String search,
            String categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CourseLevel level,
            Pageable pageable) {

        log.info(
                "Finding public courses with filters: search={}, categoryId={}, minPrice={}, maxPrice={}, level={}, page={}",
                search, categoryId, minPrice, maxPrice, level, pageable.getPageNumber());

        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        Page<Course> coursePage = courseRepository.findPublishedCoursesWithFilters(
                search, categoryId, minPrice, maxPrice, level, pageable);

        List<CoursePublicResponseDto> courseResponses = coursePage.getContent().stream()
                .map(this::mapToCoursePublicResponse)
                .collect(Collectors.toList());

        PaginatedResponse.PageInfo pageInfo = PaginatedResponse.PageInfo.builder()
                .number(coursePage.getNumber())
                .size(coursePage.getSize())
                .totalPages(coursePage.getTotalPages())
                .totalElements(coursePage.getTotalElements())
                .first(coursePage.isFirst())
                .last(coursePage.isLast())
                .build();

        return PaginatedResponse.<CoursePublicResponseDto>builder()
                .content(courseResponses)
                .page(pageInfo)
                .build();
    }

    private CoursePublicResponseDto mapToCoursePublicResponse(Course course) {
        // Get primary category (first one if multiple)
        CoursePublicResponseDto.CategorySummary categorySum = null;
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            Category primaryCategory = course.getCategories().get(0);
            categorySum = CoursePublicResponseDto.CategorySummary.builder()
                    .id(primaryCategory.getId())
                    .name(primaryCategory.getName())
                    .build();
        }

        // Get instructor info
        CoursePublicResponseDto.InstructorSummary instructorSum = null;
        if (course.getInstructor() != null) {
            instructorSum = CoursePublicResponseDto.InstructorSummary.builder()
                    .id(course.getInstructor().getId())
                    .name(course.getInstructor().getName())
                    .avatar(course.getInstructor().getThumbnailUrl()) // Assuming thumbnail_url is avatar
                    .build();
        }

        return CoursePublicResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .category(categorySum)
                .instructor(instructorSum)
                .build();
    }
}
