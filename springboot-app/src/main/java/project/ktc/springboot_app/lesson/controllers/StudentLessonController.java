package project.ktc.springboot_app.lesson.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.lesson.services.StudentLessonServiceImp;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/student/sections")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
@Tag(name = "Student Section Lesson API", description = "Endpoints for managing lessons in sections for students")
public class StudentLessonController {
    private final StudentLessonServiceImp studentLessonService;

    /**
     * 
     * @param sectionId The ID of the section containing the lesson
     * @param lessonId  The ID of the lesson to mark as completed
     * @return Response indicating completion status
     */
    @PostMapping("/{sectionId}/lessons/{lessonId}/complete")
    @Operation(summary = "Mark lesson as completed", description = """
            Allows instructors to mark a lesson as completed for tracking purposes during course creation or management.

            **Features:**
            - Records lesson completion in instructor's tracking system
            - Idempotent operation (safe to call multiple times)
            - Verifies instructor ownership of the section
            - Validates lesson belongs to specified section
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson completion recorded successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request - lesson does not belong to section", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - instructor does not own this section", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section or lesson not found", content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during lesson completion", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<ApiResponse<String>> completeLesson(
            @Parameter(description = "Section ID where the lesson belongs", required = true) @PathVariable String sectionId,
            @Parameter(description = "Lesson ID to mark as completed", required = true) @PathVariable String lessonId) {

        return studentLessonService.completeLesson(sectionId, lessonId);
    }

}
