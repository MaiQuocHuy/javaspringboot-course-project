package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursePublicResponseDto {
    private String id;
    private String title;
    private String description;
    private BigDecimal price;
    private CourseLevel level;
    private String thumbnailUrl;
    private Long enrollCount;
    private Double averageRating;
    private Long sectionCount;
    private CategorySummary category;
    private InstructorSummary instructor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstructorSummary {
        private String id;
        private String name;
        private String avatar;
    }
}
