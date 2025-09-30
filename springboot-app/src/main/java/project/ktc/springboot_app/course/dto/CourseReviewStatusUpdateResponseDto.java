package project.ktc.springboot_app.course.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewStatusUpdateResponseDto {

  private String id;
  private String title;
  private String description;
  private String status;
  private String reason; // Only included when status is REJECTED/DENIED
}
