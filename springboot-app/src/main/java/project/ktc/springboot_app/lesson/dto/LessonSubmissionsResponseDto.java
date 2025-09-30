package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.common.dto.PaginatedResponse;

/**
 * DTO for the complete lesson submissions API response. Contains both summary statistics and
 * paginated submission data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete lesson submissions response with summary and paginated data")
public class LessonSubmissionsResponseDto {

  @Schema(description = "Summary statistics for submissions")
  private SubmissionSummaryDto summary;

  @Schema(description = "Paginated submission data")
  private PaginatedResponse<LessonSubmissionResponseDto> submissions;
}
