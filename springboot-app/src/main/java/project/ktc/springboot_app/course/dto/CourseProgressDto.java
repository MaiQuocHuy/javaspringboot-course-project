package project.ktc.springboot_app.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course progress information including summary and lesson details")
public class CourseProgressDto {

	@Schema(description = "Progress summary with counts and percentage")
	private CourseProgressSummaryDto summary;

	@Schema(description = "List of lessons with their completion status")
	private List<CourseProgressLessonDto> lessons;
}
