package project.ktc.springboot_app.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentActivityDto {
    private String activityType; // ENROLLMENT, LESSON_COMPLETION, QUIZ_SUBMISSION
    private String title;
    private String description;
    private LocalDateTime timestamp;
    private String courseId;
    private String courseTitle;
    private String lessonId;
    private String lessonTitle;
    private Double score; // For quiz submissions

    public enum ActivityType {
        ENROLLMENT("Enrolled in Course"),
        LESSON_COMPLETION("Completed Lesson"),
        QUIZ_SUBMISSION("Submitted Quiz");

        private final String displayName;

        ActivityType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}