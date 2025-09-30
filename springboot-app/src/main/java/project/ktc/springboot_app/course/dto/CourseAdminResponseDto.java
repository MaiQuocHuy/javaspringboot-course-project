package project.ktc.springboot_app.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAdminResponseDto {
  private String id;
  private String title;
  private String description;
  private String thumbnailUrl;
  private InstructorInfo instructor;
  private Boolean isApproved;
  private Boolean isPublished;
  private CourseLevel level;
  private BigDecimal price;
  private Long enrollmentCount;
  private Double averageRating;
  private Long ratingCount;
  private Long sectionCount;
  private List<CategoryInfo> categories;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class InstructorInfo {
    private String id;
    private String name;
    private String email;
    private String avatar;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryInfo {
    private String id;
    private String name;
  }
}
