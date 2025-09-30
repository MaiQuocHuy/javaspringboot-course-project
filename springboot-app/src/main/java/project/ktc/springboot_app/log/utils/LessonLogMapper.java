package project.ktc.springboot_app.log.utils;

import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.log.dto.LessonLogDto;

/** Mapper utility for converting Lesson entities to LessonLogDto for logging purposes */
public class LessonLogMapper {

  /**
   * Converts a Lesson entity to LessonLogDto for system logging
   *
   * @param lesson The lesson entity to convert
   * @return LessonLogDto containing lesson data for logging
   */
  public static LessonLogDto toLogDto(Lesson lesson) {
    if (lesson == null) {
      return null;
    }

    LessonLogDto.LessonLogDtoBuilder builder =
        LessonLogDto.builder()
            .id(lesson.getId())
            .title(lesson.getTitle())
            .type(lesson.getLessonType() != null ? lesson.getLessonType().getName() : null)
            .orderIndex(lesson.getOrderIndex())
            .createdAt(lesson.getCreatedAt())
            .updatedAt(lesson.getUpdatedAt());

    // Add section information if available
    if (lesson.getSection() != null) {
      builder.sectionId(lesson.getSection().getId()).sectionTitle(lesson.getSection().getTitle());

      // Add course information if available through section
      if (lesson.getSection().getCourse() != null) {
        builder
            .courseId(lesson.getSection().getCourse().getId())
            .courseTitle(lesson.getSection().getCourse().getTitle());
      }
    }

    // Add video content information if lesson has video content
    if (lesson.getContent() != null) {
      VideoContent videoContent = lesson.getContent();
      LessonLogDto.VideoContentLogDto videoLogDto =
          LessonLogDto.VideoContentLogDto.builder()
              .id(videoContent.getId())
              .url(videoContent.getUrl())
              .duration(videoContent.getDuration())
              .uploadedBy(
                  videoContent.getUploadedBy() != null
                      ? videoContent.getUploadedBy().getId()
                      : null)
              .createdAt(videoContent.getCreatedAt())
              .build();

      builder.videoContent(videoLogDto);
    }

    return builder.build();
  }
}
