package project.ktc.springboot_app.section.interfaces;

import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

import java.util.List;

public interface SectionService {

    ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
            String courseId,
            String instructorId);
}
