package project.ktc.springboot_app.instructor_student.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import project.ktc.springboot_app.course.dto.common.BaseCourseResponseDto.CategoryInfo;

@Getter
@Setter
@SuperBuilder
public class EnrolledCoursesDetails extends EnrolledCourses {
  private String description;
  private BigDecimal price;
  private String thumbnailUrl;
  private String level;
  private List<CategoryInfo> categories;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  private Double averageRating;
  private Long totalRating;

  private LocalDateTime enrolledAt;

}
