package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatusUpdateResponseDto {

    private String id;
    private String title;
    private String previousStatus;
    private String currentStatus;

}
