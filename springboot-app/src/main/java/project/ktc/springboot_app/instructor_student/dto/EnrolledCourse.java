package project.ktc.springboot_app.instructor_student.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EnrolledCourse {
  private String courseId;
  private String title;
  private double progress;
}
