package project.ktc.springboot_app.course.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
