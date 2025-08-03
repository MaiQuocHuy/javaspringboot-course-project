package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.ktc.springboot_app.course.enums.CourseLevel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDashboardResponseDto {
    private String id;
    private String title;
    private BigDecimal price;
    private String description;
    private CourseLevel level;
    private String thumbnailUrl;

    private CategoryInfo category;

    private String status; // DRAFT, PENDING, PUBLISHED, etc.
    private boolean isApproved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastContentUpdate;

    private int totalStudents;
    private int sectionCount;
    private double averageRating;
    private BigDecimal revenue;

    private boolean canEdit;
    private boolean canUnpublish;
    private boolean canDelete;
    private boolean canPublish;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInfo {
        private String id;
        private String name;
    }
}
