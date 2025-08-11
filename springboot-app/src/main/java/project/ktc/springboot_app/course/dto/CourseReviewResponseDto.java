package project.ktc.springboot_app.course.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseReviewResponseDto {

    private String id;
    private String title;
    private String description;
    private CreatedByDto createdBy;
    private LocalDateTime createdAt;
    private String status;
    private Integer countSection;
    private Integer countLesson;
    private Integer totalDuration;
    private LocalDateTime statusUpdatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreatedByDto {
        private String id;
        private String name;
    }
}