package project.ktc.springboot_app.user.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.ktc.springboot_app.auth.dto.UserResponseDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDetailResponseDto extends UserResponseDto {
	private List<EnrolledCourseDto> enrolledCourses;
	private BigDecimal totalPayments;
	private Long totalStudyTimeSeconds; // Total study time in seconds

	@Getter
	@Setter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EnrolledCourseDto {
		private String courseId;
		private String courseTitle;
		private String instructorName;
		private String enrolledAt;
		private String completionStatus;
		private BigDecimal paidAmount;
		private Long totalTimeStudying; // in seconds
	}
}
