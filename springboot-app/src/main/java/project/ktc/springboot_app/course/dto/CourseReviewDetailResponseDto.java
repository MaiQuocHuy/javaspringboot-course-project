package project.ktc.springboot_app.course.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewDetailResponseDto {
  private String id;
  private String title;
  private String description;
  private CreatedByDto createdBy;
  private Integer countSection;
  private Integer countLesson;
  private Integer totalDuration;
  private CourseLevel level;
  private BigDecimal price;
  private String thumbnailUrl;
  private List<CategorySummary> categories;
  private List<SectionDetailDto> sections;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreatedByDto {
    private String id;
    private String name;
    private String email;
    private String avatar;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategorySummary {
    private String id;
    private String name;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SectionDetailDto {
    private String id;
    private String title;
    private Integer order;
    private Integer lessonCount;
    private Integer totalVideoDuration;
    private Integer totalQuizQuestion;
    private List<LessonDetailDto> lessons;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LessonDetailDto {
    private String id;
    private String title;
    private String type;
    private VideoDetailDto video;
    private QuizDetailDto quiz;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class VideoDetailDto {
    private String id;
    private String url;
    private Integer duration;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QuizDetailDto {
    private List<QuestionDetailDto> questions;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QuestionDetailDto {
    private String id;
    private String questionText;
    private Map<String, String> options;
    private String correctAnswer;
    private String explanation;
  }
}
