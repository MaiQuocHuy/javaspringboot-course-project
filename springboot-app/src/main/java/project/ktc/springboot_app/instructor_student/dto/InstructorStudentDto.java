package project.ktc.springboot_app.instructor_student.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InstructorStudentDto {
  private String id;
  private String name;
  private String email;
  private String thumbnailUrl;
  private List<EnrolledCourses> enrolledCourses;
}
