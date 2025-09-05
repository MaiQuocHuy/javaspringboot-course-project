package project.ktc.springboot_app.instructor_student.interfaces;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDetailsDto;
import project.ktc.springboot_app.instructor_student.dto.InstructorStudentDto;

public interface InstructorStudentService {
  public ResponseEntity<ApiResponse<PaginatedResponse<InstructorStudentDto>>> getEnrolledStudents(Pageable pageable);

  public ResponseEntity<ApiResponse<InstructorStudentDetailsDto>> getEnrolledStudentDetails(String studentId,
      Pageable pageable);
}
