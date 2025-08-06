package project.ktc.springboot_app.review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.review.entity.Review;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for student review response containing course information")
public class StudentReviewResponseDto {

    @Schema(description = "Review ID", example = "review-uuid-123")
    private String id;

    @Schema(description = "Course information for the reviewed course")
    private CourseInfo course;

    @Schema(description = "Course rating from 1 to 5", example = "5")
    private Integer rating;

    @Schema(description = "Review text content", example = "Excellent course! Very helpful and well-structured.")
    private String reviewText;

    @Schema(description = "When the review was submitted", example = "2025-08-01T10:30:00Z")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime reviewedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Course information summary")
    public static class CourseInfo {
        @Schema(description = "Course ID", example = "course-uuid-123")
        private String id;

        @Schema(description = "Course title", example = "KTC Backend Spring Boot")
        private String title;
    }

    /**
     * Factory method to create StudentReviewResponseDto from Review entity
     */
    public static StudentReviewResponseDto fromEntity(Review review) {
        CourseInfo courseInfo = CourseInfo.builder()
                .id(review.getCourse().getId())
                .title(review.getCourse().getTitle())
                .build();

        return StudentReviewResponseDto.builder()
                .id(review.getId())
                .course(courseInfo)
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .reviewedAt(review.getReviewedAt())
                .build();
    }
}
