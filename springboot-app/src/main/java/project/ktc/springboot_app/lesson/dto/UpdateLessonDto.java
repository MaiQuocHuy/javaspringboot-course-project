package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
    description =
        "DTO for updating an existing lesson - all fields are optional for PATCH operation")
public class UpdateLessonDto {

  @Size(min = 3, max = 255, message = "Lesson title must be between 3 and 255 characters")
  @Schema(
      description = "Updated lesson title (optional)",
      example = "Advanced Spring Boot Concepts",
      required = false)
  private String title;

  @Pattern(regexp = "^(VIDEO|QUIZ)$", message = "Lesson type must be either 'VIDEO' or 'QUIZ'")
  @Schema(
      description = "Lesson type (cannot be changed during update, used for validation only)",
      example = "VIDEO",
      allowableValues = {"VIDEO", "QUIZ"},
      required = false)
  private String type;
}
