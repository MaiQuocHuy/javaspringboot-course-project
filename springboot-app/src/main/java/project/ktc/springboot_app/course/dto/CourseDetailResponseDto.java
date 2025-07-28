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
    private Integer enrollCount;
    private String sampleVideoUrl;
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
    }

}
