package project.ktc.springboot_app.log.utils;

import java.util.stream.Collectors;
import project.ktc.springboot_app.category.entity.Category;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.log.dto.CourseLogDto;

/** Utility class for converting Course entity to CourseLogDto for system logging */
public class CourseLogMapper {

  /** Convert Course entity to CourseLogDto for logging purposes */
  public static CourseLogDto toLogDto(Course course) {
    if (course == null) {
      return null;
    }

    return CourseLogDto.builder()
        .id(course.getId())
        .title(course.getTitle())
        .description(course.getDescription())
        .price(course.getPrice())
        .level(course.getLevel())
        .thumbnailUrl(course.getThumbnailUrl())
        .thumbnailId(course.getThumbnailId())
        .isPublished(course.getIsPublished())
        .isApproved(course.getIsApproved())
        .isDeleted(course.getIsDeleted())
        .instructorId(course.getInstructor() != null ? course.getInstructor().getId() : null)
        .instructorName(course.getInstructor() != null ? course.getInstructor().getName() : null)
        .categoryIds(
            course.getCategories() != null
                ? course.getCategories().stream().map(Category::getId).collect(Collectors.toList())
                : null)
        .categoryNames(
            course.getCategories() != null
                ? course.getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.toList())
                : null)
        .createdAt(course.getCreatedAt())
        .updatedAt(course.getUpdatedAt())
        .build();
  }
}
