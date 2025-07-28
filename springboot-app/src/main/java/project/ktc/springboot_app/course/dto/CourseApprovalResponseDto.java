package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseApprovalResponseDto {
    private String id;
    private String title;
    private Boolean isApproved;
    private LocalDateTime approvedAt;
}
