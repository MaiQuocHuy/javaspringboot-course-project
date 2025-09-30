package project.ktc.springboot_app.course.dto.projection;

import java.time.LocalDateTime;

public interface CourseReviewProjection {
  String getId();

  String getTitle();

  String getDescription();

  String getCreatedById();

  String getCreatedByName();

  LocalDateTime getCreatedAt();

  String getStatus();

  Integer getCountSection();

  Integer getCountLesson();

  Integer getTotalDuration();

  LocalDateTime getStatusUpdatedAt();
}
