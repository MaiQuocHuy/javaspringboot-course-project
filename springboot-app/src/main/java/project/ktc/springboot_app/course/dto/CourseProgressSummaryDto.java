package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course progress summary information")
public class CourseProgressSummaryDto {

    @Schema(description = "Number of completed lessons", example = "3")
    private Integer completedCount;

    @Schema(description = "Total number of lessons in the course", example = "10")
    private Integer totalLessons;

    @Schema(description = "Progress percentage (0-100)", example = "30")
    private Integer percentage;
}