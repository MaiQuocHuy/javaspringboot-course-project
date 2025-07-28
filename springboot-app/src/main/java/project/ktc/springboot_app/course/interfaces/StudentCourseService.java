package project.ktc.springboot_app.course.interfaces;

import org.springframework.http.ResponseEntity;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

import java.util.List;

public interface StudentCourseService {
    ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(String courseId);
}
