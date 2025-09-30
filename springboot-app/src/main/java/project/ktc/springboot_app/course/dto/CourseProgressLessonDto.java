package project.ktc.springboot_app.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual lesson progress information")
public class CourseProgressLessonDto {

	@Schema(description = "Lesson ID", example = "lesson-001")
	private String lessonId;

	@Schema(description = "Lesson order in the course", example = "0")
	private Integer order;

	@Schema(description = "Lesson completion status", example = "COMPLETED", allowableValues = { "COMPLETED",
			"UNLOCKED", "LOCKED" })
	private String status;

	@Schema(description = "Timestamp when lesson was completed (only for COMPLETED status)", example = "2025-01-15T11:30:00")
	private LocalDateTime completedAt;
}
