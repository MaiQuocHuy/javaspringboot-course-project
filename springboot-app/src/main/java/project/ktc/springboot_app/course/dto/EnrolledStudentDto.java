package project.ktc.springboot_app.course.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EnrolledStudentDto {
	private String id;
	private String name;
	private String email;
	private String thumbnailUrl;
	private double progress;
	private LocalDateTime enrolledAt;
}
