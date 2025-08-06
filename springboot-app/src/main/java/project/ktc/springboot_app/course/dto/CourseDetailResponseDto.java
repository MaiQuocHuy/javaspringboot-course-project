package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailResponseDto {
    private String id;
    private String slug;
    private String title;
    private String description;
    private BigDecimal price;
    private CourseLevel level;
    private String thumbnailUrl;
    private Integer lessonCount;
    private Integer quizCount; // Total number of quiz lessons in the course
    private Integer questionCount; // Total number of quiz questions in the course
    private Integer enrollCount;
    private String sampleVideoUrl;
    private Integer totalDuration; // Total course duration in seconds
    private RatingSummary rating;
    private Boolean isEnrolled;
    private InstructorSummary instructor;
    private List<SectionSummary> sections;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RatingSummary {
        private Double average;
        private Long totalReviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorSummary {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectionSummary {
        private String id;
        private String title;
        private Integer lessonCount; // Total number of lessons in this section
        private Integer quizCount; // Number of quiz lessons in this section
        private Integer duration; // Section total duration in seconds
        private List<LessonSummary> lessons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonSummary {
        private String id;
        private String title;
        private String type;
        private Integer duration; // Lesson duration in seconds (only for VIDEO type)
    }

}
