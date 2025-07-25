package project.ktc.springboot_app.lesson.interfaces;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.lesson.dto.CreateLessonDto;
import project.ktc.springboot_app.lesson.dto.CreateLessonResponseDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonResponseDto;
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

    /**
     * Creates a new lesson in a section owned by the instructor
     * 
     * @param sectionId The ID of the section
     * @param lessonDto The lesson creation data
     * @param videoFile The video file for video lessons (required for VIDEO type)
     * @return LessonCreateResponseDto containing the created lesson details
     */
    ResponseEntity<ApiResponse<CreateLessonResponseDto>> createLesson(String sectionId, CreateLessonDto lessonDto,
            MultipartFile videoFile);

    /**
     * Updates an existing lesson in a section owned by the instructor
     * 
     * @param sectionId The ID of the section
     * @param lessonId  The ID of the lesson to update
     * @param lessonDto The lesson update data
     * @param videoFile The video file for video lessons (optional)
     * @return UpdateLessonResponseDto containing the updated lesson details
     */
    ResponseEntity<ApiResponse<UpdateLessonResponseDto>> updateLesson(String sectionId, String lessonId,
            UpdateLessonDto lessonDto, MultipartFile videoFile);

    /**
     * Deletes a lesson from a section owned by the instructor
     * If the lesson is of type VIDEO, the associated video file will also be
     * deleted from cloud storage
     * 
     * @param sectionId The ID of the section
     * @param lessonId  The ID of the lesson to delete
     * @return ApiResponse with success message
     */
    ResponseEntity<ApiResponse<String>> deleteLesson(String sectionId, String lessonId);
}
