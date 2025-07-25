package project.ktc.springboot_app.lesson.interfaces;

import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

public interface LessonService {

    /**
     * Retrieves a section with all its lessons, including completion status for the
     * instructor
     * 
     * @param sectionId The ID of the section
     * @return SectionWithLessonsDto containing section details and lessons with
     *         completion status
     */
    ResponseEntity<ApiResponse<SectionWithLessonsDto>> getSectionWithLessons(String sectionId);
}
