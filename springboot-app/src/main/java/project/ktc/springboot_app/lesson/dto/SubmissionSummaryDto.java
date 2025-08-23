package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO containing summary statistics for lesson submissions.
 * Provides aggregated data about student submissions for a specific lesson.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary statistics for lesson submissions")
public class SubmissionSummaryDto {

    @Schema(description = "Total number of enrolled students", example = "25")
    private Long totalStudents;

    @Schema(description = "Number of students who have submitted", example = "18")
    private Long submittedCount;

    @Schema(description = "Number of students who haven't submitted", example = "7")
    private Long notSubmittedCount;

    @Schema(description = "Average score of all submissions (null if no submissions)", example = "78.5")
    private BigDecimal averageScore;

    /**
     * Constructor for repository projection query
     * 
     * @param totalStudents  Total enrolled students
     * @param submittedCount Students who submitted
     * @param averageScore   Average score (JPA returns Double for AVG function)
     */
    public SubmissionSummaryDto(Long totalStudents, Long submittedCount, Double averageScore) {
        this.totalStudents = totalStudents != null ? totalStudents : 0L;
        this.submittedCount = submittedCount != null ? submittedCount : 0L;
        this.notSubmittedCount = this.totalStudents - this.submittedCount;
        this.averageScore = averageScore != null ? BigDecimal.valueOf(averageScore) : null;
    }
}