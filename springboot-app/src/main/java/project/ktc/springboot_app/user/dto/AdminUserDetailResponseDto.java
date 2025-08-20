package project.ktc.springboot_app.user.dto;

import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.auth.dto.UserResponseDto;
import project.ktc.springboot_app.auth.entitiy.User;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class AdminUserDetailResponseDto extends UserResponseDto {
    private List<EnrolledCourseDto> enrolledCourses;
    private BigDecimal totalPayments;
    private Long totalStudyTimeMinutes;

    public AdminUserDetailResponseDto(User user) {
        super(user);
    }

    @Getter
    @Setter
    public static class EnrolledCourseDto {
        private String courseId;
        private String courseTitle;
        private String instructorName;
        private String enrolledAt;
        private String completionStatus;
        private BigDecimal paidAmount;
        private Long totalTimeStudying; // in minutes

        public EnrolledCourseDto(String courseId, String courseTitle, String instructorName,
                String enrolledAt, String completionStatus, BigDecimal paidAmount) {
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.instructorName = instructorName;
            this.enrolledAt = enrolledAt;
            this.completionStatus = completionStatus;
            this.paidAmount = paidAmount;
        }

        public EnrolledCourseDto(String courseId, String courseTitle, String instructorName,
                String enrolledAt, String completionStatus, BigDecimal paidAmount, Long totalTimeStudying) {
            this.courseId = courseId;
            this.courseTitle = courseTitle;
            this.instructorName = instructorName;
            this.enrolledAt = enrolledAt;
            this.completionStatus = completionStatus;
            this.paidAmount = paidAmount;
            this.totalTimeStudying = totalTimeStudying;
        }
    }
}
