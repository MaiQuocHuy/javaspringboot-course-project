package project.ktc.springboot_app.lesson.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.lesson.interfaces.LessonService;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

@RestController
@RequestMapping("/api/instructor")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@Tag(name = "Instructor Section Lesson API", description = "Endpoints for managing lessons in sections for instructors")
public class InstructorLessonController {

    private final LessonService lessonService;

    /**
     * Get section with all its lessons for an instructor
     * Endpoint: GET /api/instructor/sections/{sectionId}/lessons
     * 
     * @param sectionId      The ID of the section
     * @param authentication Current authenticated instructor
     * @return SectionWithLessonsDto containing section details and lessons
     */
    @GetMapping("/sections/{sectionId}/lessons")
    public ResponseEntity<ApiResponse<SectionWithLessonsDto>> getSectionWithLessons(
            @PathVariable String sectionId) {
        return lessonService.getSectionWithLessons(sectionId);
    }
}
