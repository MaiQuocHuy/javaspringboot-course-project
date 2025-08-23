package project.ktc.springboot_app.lesson.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for internal use by repository queries to project student submission data
 * with enrollment information. Used to combine student info with their submission status.
 */
@Data
@Builder
@NoArgsConstructor
@Schema(description = "Internal DTO for repository projection of student submission data")
public class StudentSubmissionDto {
    
    @Schema(description = "Student ID", example = "student-uuid-123")
    private String studentId;
    
    @Schema(description = "Student name", example = "John Doe")
    private String studentName;
    
    @Schema(description = "Student email", example = "john@example.com")
    private String studentEmail;
    
    @Schema(description = "Submission ID (null if not submitted)", example = "submission-uuid-456")
    private String submissionId;
    
    @Schema(description = "Quiz score (null if not submitted)", example = "85.50")
    private BigDecimal score;
    
    @Schema(description = "Submission timestamp (null if not submitted)", example = "2025-07-26T10:30:00Z")
    private LocalDateTime submittedAt;
    
    /**
     * Constructor for repository projection query
     */
    public StudentSubmissionDto(String studentId, String studentName, String studentEmail, 
                               String submissionId, BigDecimal score, LocalDateTime submittedAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.submissionId = submissionId;
        this.score = score;
        this.submittedAt = submittedAt;
    }
    
    /**
     * Helper method to determine the effective status for API response
     * @return "not_submitted" or "submitted"
     */
    public String getEffectiveStatus() {
        return submissionId == null ? "not_submitted" : "submitted";
    }
}