package project.ktc.springboot_app.enrollment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyEnrolledCourseDto {
  private String courseId;
  private String title;
  private String thumbnailUrl;
  private String slug;
  private CourseLevel level;
  private BigDecimal price;
  private Double progress;
  private String completionStatus;
  private InstructorSummary instructor;
  private LocalDateTime enrolledAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InstructorSummary {
    private String id;
    private String name;
    private String avatar;
  }
}
