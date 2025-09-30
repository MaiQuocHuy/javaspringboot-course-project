package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for creating a new lesson with video content")
public class CreateLessonDto {

  @NotBlank(message = "Lesson title is required")
  @Size(min = 3, max = 255, message = "Lesson title must be between 3 and 255 characters")
  @Schema(
      description = "Lesson title",
      example = "Introduction to Spring Boot Basics",
      required = true)
  private String title;

  @NotBlank(message = "Lesson type is required")
  @Pattern(regexp = "^(VIDEO|QUIZ)$", message = "Lesson type must be either 'VIDEO' or 'QUIZ'")
  @Schema(
      description = "Lesson type",
      example = "VIDEO",
      allowableValues = {"VIDEO", "QUIZ"},
      required = true)
  private String type;
}
