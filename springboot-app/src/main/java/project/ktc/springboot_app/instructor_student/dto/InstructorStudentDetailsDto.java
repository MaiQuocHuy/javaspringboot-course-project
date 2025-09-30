package project.ktc.springboot_app.instructor_student.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import project.ktc.springboot_app.common.dto.PaginatedResponse;

@Getter
@Setter
@Builder
public class InstructorStudentDetailsDto {

	private String id;
	private String name;
	private String email;
	private String thumbnailUrl;

	private PaginatedResponse<EnrolledCoursesDetails> enrolledCourses;
}
