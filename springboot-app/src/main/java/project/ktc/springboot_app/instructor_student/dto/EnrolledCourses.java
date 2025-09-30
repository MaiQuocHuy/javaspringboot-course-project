package project.ktc.springboot_app.instructor_student.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class EnrolledCourses {
	private String courseId;
	private String title;
	private double progress;
}
