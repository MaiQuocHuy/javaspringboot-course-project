package project.ktc.springboot_app.enrollment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDashboardStatsDto {
	private Long totalCourses;
	private Long completedCourses;
	private Long inProgressCourses;
	private Long lessonsCompleted;
	private Long totalLessons;
}
