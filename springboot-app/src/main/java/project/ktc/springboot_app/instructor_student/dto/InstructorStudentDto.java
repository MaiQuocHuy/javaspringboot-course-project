package project.ktc.springboot_app.instructor_student.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorStudentDto {
  private String id;
  private String name;
  private String email;
  private String thumbnailUrl;
  private List<EnrolledCourse> enrolledCourses;
}
