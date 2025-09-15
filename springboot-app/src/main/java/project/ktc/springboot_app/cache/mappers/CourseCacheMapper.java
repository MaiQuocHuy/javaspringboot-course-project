package project.ktc.springboot_app.cache.mappers;

import project.ktc.springboot_app.course.dto.cache.CourseCacheDto;
import project.ktc.springboot_app.course.dto.cache.SharedCourseCacheDto;
import project.ktc.springboot_app.course.dto.SharedCourseDataDto;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.category.entity.Category;
import project.ktc.springboot_app.course.entity.Course;
// import project.ktc.springboot_app.entity.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting between JPA entities and cache DTOs
 */
public class CourseCacheMapper {

    /**
     * Converts Course entity to CourseCacheDto for Redis storage
     */
    public static CourseCacheDto toCacheDto(Course course) {
        if (course == null) {
            return null;
        }

        CourseCacheDto.CourseCacheDtoBuilder builder = CourseCacheDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .isApproved(course.getIsApproved())
                .isPublished(course.getIsPublished())
                .isDeleted(course.getIsDeleted())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt());

        // Map instructor information (flattened to avoid circular references)
        if (course.getInstructor() != null) {
            builder.instructorId(course.getInstructor().getId())
                    .instructorName(course.getInstructor().getName())
                    .instructorBio(course.getInstructor().getBio())
                    .instructorThumbnailUrl(course.getInstructor().getThumbnailUrl());
        }

        // Map categories (simplified)
        if (course.getCategories() != null) {
            List<CourseCacheDto.CategoryCacheDto> categoryDtos = course.getCategories().stream()
                    .map(CourseCacheMapper::toCategoryCacheDto)
                    .collect(Collectors.toList());
            builder.categories(categoryDtos);
        }

        return builder.build();
    }

    /**
     * Converts Category entity to CategoryCacheDto
     */
    public static CourseCacheDto.CategoryCacheDto toCategoryCacheDto(Category category) {
        if (category == null) {
            return null;
        }

        return CourseCacheDto.CategoryCacheDto.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .isActive(true) // Categories are active by default
                .build();
    }

    /**
     * Converts CourseCacheDto back to Course entity for service layer usage
     */
    public static Course fromCacheDto(CourseCacheDto cacheDto) {
        if (cacheDto == null) {
            return null;
        }

        Course course = new Course();
        course.setId(cacheDto.getId());
        course.setTitle(cacheDto.getTitle());
        course.setDescription(cacheDto.getDescription());
        course.setPrice(cacheDto.getPrice());
        course.setLevel(cacheDto.getLevel());
        course.setThumbnailUrl(cacheDto.getThumbnailUrl());
        course.setIsApproved(cacheDto.getIsApproved());
        course.setIsPublished(cacheDto.getIsPublished());
        course.setIsDeleted(cacheDto.getIsDeleted());
        course.setCreatedAt(cacheDto.getCreatedAt());
        course.setUpdatedAt(cacheDto.getUpdatedAt());

        // Create instructor if data exists
        if (cacheDto.getInstructorId() != null) {
            User instructor = new User();
            instructor.setId(cacheDto.getInstructorId());
            instructor.setName(cacheDto.getInstructorName());
            instructor.setBio(cacheDto.getInstructorBio());
            instructor.setThumbnailUrl(cacheDto.getInstructorThumbnailUrl());
            course.setInstructor(instructor);
        }

        // Convert categories back
        if (cacheDto.getCategories() != null) {
            List<Category> categories = cacheDto.getCategories().stream()
                    .map(CourseCacheMapper::fromCategoryCacheDto)
                    .collect(Collectors.toList());
            course.setCategories(categories);
        }

        return course;
    }

    /**
     * Converts CategoryCacheDto back to Category entity
     */
    public static Category fromCategoryCacheDto(CourseCacheDto.CategoryCacheDto cacheDto) {
        if (cacheDto == null) {
            return null;
        }

        Category category = new Category();
        category.setId(cacheDto.getId());
        category.setName(cacheDto.getName());
        category.setDescription(cacheDto.getDescription());
        // Note: isActive is not set as Category entity doesn't have this field

        return category;
    }

    /**
     * Converts SharedCourseDataDto to SharedCourseCacheDto for caching
     */
    public static SharedCourseCacheDto toSharedCacheDto(SharedCourseDataDto sharedData) {
        if (sharedData == null) {
            return null;
        }

        List<CourseCacheDto> courseCacheDtos = null;
        if (sharedData.getCoursesWithCategories() != null) {
            courseCacheDtos = sharedData.getCoursesWithCategories().stream()
                    .map(CourseCacheMapper::toCacheDto)
                    .collect(Collectors.toList());
        }

        return SharedCourseCacheDto.builder()
                .coursesWithCategories(courseCacheDtos)
                .enrollmentCounts(sharedData.getEnrollmentCounts())
                .totalPages(sharedData.getTotalPages())
                .totalElements(sharedData.getTotalElements())
                .pageNumber(sharedData.getPageNumber())
                .pageSize(sharedData.getPageSize())
                .first(sharedData.isFirst())
                .last(sharedData.isLast())
                .build();
    }

    /**
     * Converts SharedCourseCacheDto back to SharedCourseDataDto for service usage
     */
    public static SharedCourseDataDto fromSharedCacheDto(SharedCourseCacheDto cacheDto) {
        if (cacheDto == null) {
            return null;
        }

        List<Course> courses = null;
        if (cacheDto.getCoursesWithCategories() != null) {
            courses = cacheDto.getCoursesWithCategories().stream()
                    .map(CourseCacheMapper::fromCacheDto)
                    .collect(Collectors.toList());
        }

        return SharedCourseDataDto.builder()
                .coursesWithCategories(courses)
                .enrollmentCounts(cacheDto.getEnrollmentCounts())
                .totalPages(cacheDto.getTotalPages())
                .totalElements(cacheDto.getTotalElements())
                .pageNumber(cacheDto.getPageNumber())
                .pageSize(cacheDto.getPageSize())
                .first(cacheDto.getFirst())
                .last(cacheDto.getLast())
                .build();
    }
}