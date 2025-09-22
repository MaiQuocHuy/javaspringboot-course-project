package project.ktc.springboot_app.cache.mappers;

import project.ktc.springboot_app.course.dto.cache.InstructorCourseBaseCacheDto;
import project.ktc.springboot_app.course.dto.cache.InstructorCourseDynamicCacheDto;
import project.ktc.springboot_app.course.dto.CourseDashboardResponseDto;
import project.ktc.springboot_app.course.dto.common.BaseCourseResponseDto;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.category.entity.Category;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Course entities and instructor-specific cache
 * DTOs
 */
public class InstructorCoursesCacheMapper {

    /**
     * Converts Course entity to InstructorCourseBaseCacheDto for Redis storage
     * Note: statusReview and reason should be set separately via
     * updateCacheWithReviewInfo()
     */
    public static InstructorCourseBaseCacheDto toBaseCacheDto(Course course) {
        if (course == null) {
            return null;
        }

        InstructorCourseBaseCacheDto.InstructorCourseBaseCacheDtoBuilder builder = InstructorCourseBaseCacheDto
                .builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .isApproved(course.getIsApproved())
                .isPublished(course.getIsPublished())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .statusReview(null) // Will be set by service from course_review_status_history
                .reason(null); // Will be set by service from course_review_status_history

        // Map categories (simplified for cache)
        if (course.getCategories() != null) {
            List<InstructorCourseBaseCacheDto.CategoryCacheDto> categoryDtos = course.getCategories().stream()
                    .map(InstructorCoursesCacheMapper::toCategoryCacheDto)
                    .collect(Collectors.toList());
            builder.categories(categoryDtos);
        }

        return builder.build();
    }

    /**
     * Converts Course entity to InstructorCourseBaseCacheDto for Redis storage
     * with review status information
     */
    public static InstructorCourseBaseCacheDto toBaseCacheDtoWithReviewInfo(Course course, String statusReview,
            String reason) {
        InstructorCourseBaseCacheDto baseDto = toBaseCacheDto(course);
        if (baseDto != null) {
            baseDto.setStatusReview(statusReview);
            baseDto.setReason(reason);
        }
        return baseDto;
    }

    /**
     * Converts CourseDashboardResponseDto to InstructorCourseDynamicCacheDto
     */
    public static InstructorCourseDynamicCacheDto toDynamicCacheDto(CourseDashboardResponseDto courseDto) {
        if (courseDto == null) {
            return null;
        }

        return InstructorCourseDynamicCacheDto.builder()
                .courseId(courseDto.getId())
                .enrollmentCount(courseDto.getTotalStudents())
                .averageRating(courseDto.getAverageRating())
                .revenue(courseDto.getRevenue())
                .sectionCount(courseDto.getSectionCount())
                .lastContentUpdate(courseDto.getLastContentUpdate())
                .canEdit(courseDto.isCanEdit())
                .canDelete(courseDto.isCanDelete())
                .canUnpublish(courseDto.isCanUnpublish())
                .canResubmit(courseDto.isCanPublish()) // Reusing canPublish logic
                .cacheCreatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Merges base cache info and dynamic cache info into CourseDashboardResponseDto
     */
    public static CourseDashboardResponseDto mergeCacheData(InstructorCourseBaseCacheDto baseInfo,
            InstructorCourseDynamicCacheDto dynamicInfo) {
        if (baseInfo == null) {
            return null;
        }

        // Convert categories to CategoryInfo list
        List<BaseCourseResponseDto.CategoryInfo> categoryInfos = null;
        if (baseInfo.getCategories() != null) {
            categoryInfos = baseInfo.getCategories().stream()
                    .map(cat -> BaseCourseResponseDto.CategoryInfo.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .build())
                    .collect(Collectors.toList());
        }

        return CourseDashboardResponseDto.dashboardBuilder()
                // Base fields
                .id(baseInfo.getId())
                .title(baseInfo.getTitle())
                .description(baseInfo.getDescription())
                .price(baseInfo.getPrice())
                .level(baseInfo.getLevel())
                .thumbnailUrl(baseInfo.getThumbnailUrl())
                .isApproved(baseInfo.getIsApproved())
                .createdAt(baseInfo.getCreatedAt())
                .updatedAt(baseInfo.getUpdatedAt())
                .categories(categoryInfos)
                // Dynamic fields (with defaults if not available)
                .totalStudents(dynamicInfo != null ? dynamicInfo.getEnrollmentCount() : 0)
                .averageRating(dynamicInfo != null ? dynamicInfo.getAverageRating() : 0.0)
                .sectionCount(dynamicInfo != null ? dynamicInfo.getSectionCount() : 0)
                // Dashboard-specific fields
                .status(determineStatusFromCache(baseInfo))
                .lastContentUpdate(dynamicInfo != null ? dynamicInfo.getLastContentUpdate() : baseInfo.getUpdatedAt())
                .revenue(dynamicInfo != null ? dynamicInfo.getRevenue() : null)
                .canEdit(dynamicInfo != null ? dynamicInfo.getCanEdit() : true)
                .canUnpublish(dynamicInfo != null ? dynamicInfo.getCanUnpublish() : false)
                .canDelete(dynamicInfo != null ? dynamicInfo.getCanDelete() : true)
                .canPublish(dynamicInfo != null ? dynamicInfo.getCanResubmit() : true)
                .statusReview(baseInfo.getStatusReview()) // Now from cache
                .reason(baseInfo.getReason()) // Now from cache
                .build();
    }

    /**
     * Converts Category entity to CategoryCacheDto
     */
    private static InstructorCourseBaseCacheDto.CategoryCacheDto toCategoryCacheDto(Category category) {
        if (category == null) {
            return null;
        }

        return InstructorCourseBaseCacheDto.CategoryCacheDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getName().toLowerCase().replaceAll(" ", "-")) // Generate slug if not available
                .build();
    }

    /**
     * Converts list of Course entities to list of InstructorCourseBaseCacheDto
     */
    public static List<InstructorCourseBaseCacheDto> toBaseCacheDtoList(List<Course> courses) {
        if (courses == null) {
            return null;
        }

        return courses.stream()
                .map(InstructorCoursesCacheMapper::toBaseCacheDto)
                .collect(Collectors.toList());
    }

    /**
     * Determines course status from cache data based on isPublished field
     * This logic mirrors the determineStatus method in InstructorCourseServiceImp
     * but includes null checks since cache DTOs may have null values after
     * deserialization
     */
    private static String determineStatusFromCache(InstructorCourseBaseCacheDto baseInfo) {
        if (baseInfo == null) {
            return "DRAFT";
        }

        if (baseInfo.getIsPublished() != null && baseInfo.getIsPublished()) {
            return "PUBLISHED";
        } else if (baseInfo.getIsPublished() != null && !baseInfo.getIsPublished()) {
            return "UNPUBLISHED";
        }
        return "DRAFT";
    }

    /**
     * Converts list of CourseDashboardResponseDto to list of
     * InstructorCourseDynamicCacheDto
     */
    public static List<InstructorCourseDynamicCacheDto> toDynamicCacheDtoList(
            List<CourseDashboardResponseDto> courseDtos) {
        if (courseDtos == null) {
            return null;
        }

        return courseDtos.stream()
                .map(InstructorCoursesCacheMapper::toDynamicCacheDto)
                .collect(Collectors.toList());
    }
}