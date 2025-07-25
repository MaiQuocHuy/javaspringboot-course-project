package project.ktc.springboot_app.section.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.interfaces.SectionService;
import project.ktc.springboot_app.utils.SecurityUtil;

@RestController
@Tag(name = "Instructor Course Sections API", description = "Endpoints for managing course sections and lessons")
@RequestMapping("/api/instructor/courses")
@RequiredArgsConstructor
@Slf4j
public class SectionController {

    private final SectionService sectionService;

    @GetMapping("/{id}/sections")
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    @Operation(summary = "Get course sections with lessons", description = """
            Retrieve all sections and their lessons for a course owned by the instructor.

            **Permission Requirements:**
            - User must have INSTRUCTOR role
            - User must own the course (course.instructor_id == currentUser.id)

            **Response includes:**
            - Section details (id, title, order, lesson count)
            - All lessons in each section ordered by order index
            - Video content details for VIDEO type lessons
            - Quiz questions for QUIZ type lessons

            **Business Rules:**
            - Only course owner can access sections
            - Returns empty array if course has no sections
            - Lessons are ordered by orderIndex within each section
            - Quiz questions include options, correct answers, and explanations
            """, security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sections retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "You are not allowed to access this course's sections"),
            @ApiResponse(responseCode = "404", description = "Course not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
            @Parameter(description = "Course ID", required = true) @PathVariable("id") String courseId) {

        log.info("Received request to get sections for course: {}", courseId);

        String instructorId = SecurityUtil.getCurrentUserId();

        // Call service to get course sections
        return sectionService.getCourseSections(courseId, instructorId);
    }
}
