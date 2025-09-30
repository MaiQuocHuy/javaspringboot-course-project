package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.section.dto.VideoDto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for lesson creation response")
public class CreateLessonResponseDto {

  @Schema(description = "Lesson ID", example = "lesson-uuid-123")
  private String id;

  @Schema(description = "Lesson title", example = "Introduction to Spring Boot")
  private String title;

  @Schema(
      description = "Lesson type",
      example = "VIDEO",
      allowableValues = {"VIDEO", "QUIZ"})
  private String type;

  @Schema(description = "Video content information (only for VIDEO type lessons)")
  private VideoDto video;

  @Schema(description = "Lesson order index within the section", example = "0")
  private Integer orderIndex;
}
