package project.ktc.springboot_app.lesson.interfaces;

import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;

public interface StudentService {

    /**
     * Marks a lesson as completed by the instructor
     * Allows instructors to track which lessons have been finalized or verified
     * during course creation or update
     * 
     * @param sectionId The ID of the section
     * @param lessonId  The ID of the lesson to mark as completed
     * @return ApiResponse with success message
     */
    ResponseEntity<ApiResponse<String>> completeLesson(String sectionId, String lessonId);

}
