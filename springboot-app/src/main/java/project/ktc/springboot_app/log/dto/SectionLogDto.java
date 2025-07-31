package project.ktc.springboot_app.log.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for logging Section entity changes
 * Contains only the essential fields needed for audit trail
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SectionLogDto {

    private String id;
    private String title;
    private String description;
    private Integer orderIndex;
    private String courseId;
    private String courseTitle; // For better context in logs
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional context fields
    private Integer lessonCount; // Number of lessons in this section
}
