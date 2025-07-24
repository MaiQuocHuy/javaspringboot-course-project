package project.ktc.springboot_app.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for review response")
public class ReviewResponseDto {

    @Schema(description = "Review ID", example = "review-uuid-123")
    private String id;

    @Schema(description = "Course rating from 1 to 5", example = "5")
    private Integer rating;

    @Schema(description = "Review text content", example = "Excellent course! Very helpful and well-structured.")
    private String review_text;

    @Schema(description = "When the review was submitted", example = "2025-07-24T10:30:00")
    private LocalDateTime reviewedAt;

    @Schema(description = "User information")
    private UserSummary user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "User summary information")
    public static class UserSummary {
        @Schema(description = "User ID", example = "user-uuid-123")
        private String id;

        @Schema(description = "User name", example = "John Doe")
        private String name;

        @Schema(description = "User avatar URL", example = "https://example.com/avatar.jpg")
        private String avatar;
    }
}
