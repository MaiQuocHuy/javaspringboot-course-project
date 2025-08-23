package project.ktc.springboot_app.lesson.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.lesson.dto.CreateLessonDto;
import project.ktc.springboot_app.lesson.dto.CreateLessonResponseDto;
import project.ktc.springboot_app.lesson.dto.CreateLessonWithQuizDto;
import project.ktc.springboot_app.lesson.dto.LessonSubmissionsResponseDto;
import project.ktc.springboot_app.lesson.dto.SubmissionDetailResponseDto;
import project.ktc.springboot_app.lesson.dto.LessonWithQuizResponseDto;
import project.ktc.springboot_app.lesson.dto.ReorderLessonsDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonResponseDto;
import project.ktc.springboot_app.lesson.services.InstructorLessonServiceImp;
import project.ktc.springboot_app.quiz.dto.UpdateQuizDto;
import project.ktc.springboot_app.quiz.dto.UpdateQuizResponseDto;
import project.ktc.springboot_app.quiz.dto.QuizQuestionResponseDto;
import project.ktc.springboot_app.quiz.services.QuizServiceImp;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.utils.SecurityUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/instructor/sections")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@Tag(name = "Instructor Section Lesson API", description = "Endpoints for managing lessons in sections for instructors")
public class InstructorLessonController {

        private final InstructorLessonServiceImp instructorLessonService;
        private final QuizServiceImp quizService;

        /**
         * Get section with all its lessons for an instructor
         * Endpoint: GET /api/instructor/{sectionId}/lessons
         * 
         * @param sectionId      The ID of the section
         * @param authentication Current authenticated instructor
         * @return SectionWithLessonsDto containing section details and lessons
         */
        @GetMapping("/{sectionId}/lessons")
        public ResponseEntity<ApiResponse<SectionWithLessonsDto>> getSectionWithLessons(
                        @PathVariable String sectionId) {
                return instructorLessonService.getSectionWithLessons(sectionId);
        }

        /**
         * Create a new lesson in a section owned by the instructor
         * Endpoint: POST /api/instructor/{sectionId}/lessons
         * 
         * @param sectionId The ID of the section
         * @param title     The lesson title
         * @param type      The lesson type (VIDEO or QUIZ)
         * @param videoFile The video file (required for VIDEO type lessons)
         * @return LessonCreateResponseDto containing the created lesson details
         */
        @PostMapping(value = "/{sectionId}/lessons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Create a new lesson", description = """
                        Create a new lesson in a section owned by the instructor.

                        **For VIDEO lessons:**
                        - Video file is required
                        - Supported video formats: MP4, MPEG, QuickTime, AVI, WMV, WebM, OGG
                        - Maximum file size: 100MB
                        - Video will be automatically processed and stored in Cloudinary

                        **For QUIZ lessons:**
                        - No video file needed
                        - Quiz questions can be added separately after lesson creation

                        **Order Index:**
                        - Automatically assigned based on existing lessons in the section (0-based)
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lesson created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateLessonResponseDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - missing video file for VIDEO lesson, invalid lesson type, or invalid video format", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - instructor does not own the section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during lesson creation", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<CreateLessonResponseDto>> createLesson(
                        @Parameter(description = "Section ID where the lesson will be created", required = true) @PathVariable String sectionId,

                        @Parameter(description = "Lesson title", required = true) @RequestParam("title") String title,

                        @Parameter(description = "Lesson type", required = true, schema = @Schema(allowableValues = {
                                        "VIDEO",
                                        "QUIZ" })) @RequestParam("type") String type,

                        @Parameter(description = "Video file for VIDEO lessons (max 100MB)", required = false) @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {

                // Create DTO from request parameters
                CreateLessonDto createLessonDto = CreateLessonDto.builder()
                                .title(title)
                                .type(type)
                                .build();

                return instructorLessonService.createLesson(sectionId, createLessonDto, videoFile);
        }

        /**
         * Update an existing lesson in a section owned by the instructor
         * Endpoint: PATCH /api/instructor/{sectionId}/lessons/{lessonId}
         * 
         * @param sectionId The ID of the section
         * @param lessonId  The ID of the lesson to update
         * @param title     The updated lesson title
         * @param type      The lesson type (cannot be changed)
         * @param videoFile The video file (optional for VIDEO lessons)
         * @return UpdateLessonResponseDto containing the updated lesson details
         */
        @PatchMapping(value = "/{sectionId}/lessons/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Update an existing lesson", description = """
                        Update an existing lesson in a section owned by the instructor.

                        **Business Rules:**
                        - Only the instructor who owns the section can update the lesson
                        - The lesson type cannot be changed from VIDEO to QUIZ or vice versa
                        - If a new videoFile is provided, it must be in a valid video format
                        - If a new videoFile is uploaded, the old video will be deleted automatically

                        **For VIDEO lessons:**
                        - Video file is optional during update
                        - If provided, supported video formats: MP4, MPEG, QuickTime, AVI, WMV, WebM, OGG
                        - Maximum file size: 100MB
                        - Video will be automatically processed and stored in Cloudinary
                        - Old video will be deleted from Cloudinary if a new one is uploaded

                        **For QUIZ lessons:**
                        - No video file needed
                        - Quiz questions are managed separately
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateLessonResponseDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - invalid lesson type change, invalid video format, or lesson not in section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - instructor does not own the section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section or lesson not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during lesson update", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<UpdateLessonResponseDto>> updateLesson(
                        @Parameter(description = "Section ID where the lesson belongs", required = true) @PathVariable String sectionId,

                        @Parameter(description = "Lesson ID to update", required = true) @PathVariable String lessonId,

                        @Parameter(description = "Updated lesson title (optional)", required = false) @RequestParam(value = "title", required = false) String title,

                        @Parameter(description = "Lesson type (cannot be changed, used for validation only)", required = false, schema = @Schema(allowableValues = {
                                        "VIDEO",
                                        "QUIZ" })) @RequestParam(value = "type", required = false) String type,

                        @Parameter(description = "Video file for VIDEO lessons (optional)", required = false) @RequestPart(value = "videoFile", required = false) MultipartFile videoFile) {

                // Create DTO from request parameters
                UpdateLessonDto updateLessonDto = UpdateLessonDto.builder()
                                .title(title)
                                .type(type)
                                .build();

                return instructorLessonService.updateLesson(sectionId, lessonId, updateLessonDto, videoFile);
        }

        /**
         * Delete a lesson from a section owned by the instructor
         * Endpoint: DELETE /api/instructor/{sectionId}/lessons/{lessonId}
         * 
         * @param sectionId The ID of the section
         * @param lessonId  The ID of the lesson to delete
         * @return ApiResponse with success message
         */
        @DeleteMapping("/{sectionId}/lessons/{lessonId}")
        @Operation(summary = "Delete a lesson", description = """
                        Delete a lesson from a section owned by the instructor.

                        **Business Rules:**
                        - Only the instructor who owns the section can delete the lesson
                        - If the lesson is of type VIDEO, the associated video file will also be deleted from cloud storage
                        - After deletion, remaining lessons will be automatically reordered to maintain continuous order indices

                        **Important Notes:**
                        - This action is irreversible
                        - All associated data (video files, quiz questions, lesson completions) will be permanently deleted
                        - The order of remaining lessons will be automatically adjusted
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lesson deleted successfully", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - lesson not in section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - instructor does not own the section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section or lesson not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during lesson deletion", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<String>> deleteLesson(
                        @Parameter(description = "Section ID where the lesson belongs", required = true) @PathVariable String sectionId,
                        @Parameter(description = "Lesson ID to delete", required = true) @PathVariable String lessonId) {

                return instructorLessonService.deleteLesson(sectionId, lessonId);
        }

        /**
         * Reorder lessons within a section owned by the instructor
         * Endpoint: PATCH /api/instructor/{sectionId}/lessons/reorder
         * 
         * @param sectionId         The ID of the section
         * @param reorderLessonsDto The lesson reorder data containing lesson IDs in
         *                          intended order
         * @return ApiResponse with success message
         */
        @PatchMapping("/{sectionId}/lessons/reorder")
        @Operation(summary = "Reorder lessons within a section", description = """
                        Reorder lessons within a section owned by the instructor.

                        **Business Rules:**
                        - Only the instructor who owns the section can reorder lessons
                        - The `lessonOrder` array must include all lesson IDs of the section in their intended order
                        - Any missing or duplicate IDs will result in a 400 Bad Request
                        - Lessons will be reordered to maintain continuous order indices (0-based)

                        **Important Notes:**
                        - All existing lesson IDs must be included in the request
                        - The order in the array determines the final lesson sequence
                        - Order indices will be automatically assigned based on array position
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lessons reordered successfully", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed - invalid lesson order, missing IDs, or duplicate IDs", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - instructor does not own the section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during lesson reordering", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<String>> reorderLessons(
                        @Parameter(description = "Section ID where the lessons belong", required = true) @PathVariable String sectionId,
                        @Parameter(description = "Request body containing lesson IDs in intended order", required = true) @Valid @RequestBody ReorderLessonsDto reorderLessonsDto) {

                return instructorLessonService.reorderLessons(sectionId, reorderLessonsDto);
        }

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

                return instructorLessonService.completeLesson(sectionId, lessonId);
        }

        /**
         * Create a new lesson with quiz in a single transaction
         * Endpoint: POST /api/instructor/sections/{sectionId}/lessons/with-quiz
         * 
         * @param sectionId               The ID of the section where the lesson will be
         *                                created
         * @param createLessonWithQuizDto The lesson and quiz creation data
         * @return LessonWithQuizResponseDto containing the created lesson and quiz
         *         details
         */
        @PostMapping("/{sectionId}/lessons/with-quiz")
        @Operation(summary = "Create lesson with quiz", description = """
                        Creates a new lesson with attached quiz questions in a single transaction.
                        This endpoint allows instructors to create both lesson content and quiz questions simultaneously,
                        ensuring data consistency through transactional operations.

                        **Important Notes:**
                        - Quiz questions must have exactly 4 options (A, B, C, D)
                        - Correct answer must be one of the provided options
                        - All quiz questions are created atomically with the lesson
                        - Lesson type is automatically set to 'QUIZ'
                        - Order index is automatically calculated based on existing lessons

                        **Business Rules:**
                        - Section must belong to the authenticated instructor
                        - Quiz must contain at least 1 question
                        - Each question must have 4 options with keys A, B, C, D
                        - Correct answer must match one of the option keys
                        - Explanation is optional but recommended for better learning

                        **Features:**
                        - Transactional safety (all-or-nothing creation)
                        - Automatic order index calculation
                        - Comprehensive validation of quiz questions
                        - Returns complete lesson and quiz data in response
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Lesson with quiz created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LessonWithQuizResponseDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request - validation errors in lesson or quiz data", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - instructor does not own this section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during lesson and quiz creation", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<LessonWithQuizResponseDto>> createLessonWithQuiz(
                        @Parameter(description = "Section ID where the lesson will be created", required = true) @PathVariable String sectionId,
                        @Parameter(description = "Lesson and quiz creation data", required = true) @Valid @RequestBody CreateLessonWithQuizDto createLessonWithQuizDto) {

                return instructorLessonService.createLessonWithQuiz(sectionId, createLessonWithQuizDto);
        }

        @PutMapping("/{sectionId}/lessons/quiz")
        @Operation(summary = "Update quiz questions for a lesson", description = """
                            Updates the quiz questions for a lesson owned by the instructor.
                            This endpoint allows instructors to replace existing quiz questions with new ones,
                            ensuring that all questions are updated atomically.
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quiz questions updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UpdateLessonResponseDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request - validation errors in quiz data", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - instructor does not own this section", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section or lesson not found", content = @Content(mediaType = "application/json")),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during quiz update", content = @Content(mediaType = "application/json"))
        })
        public ResponseEntity<ApiResponse<UpdateQuizResponseDto>> updateQuizQuestions(
                        @Parameter(description = "Section ID where the lesson belongs", required = true) @PathVariable String sectionId,

                        @Parameter(description = "Lesson ID to update quiz questions for", required = true) @RequestParam String lessonId,

                        @Parameter(description = "Updated quiz data", required = true) @Valid @RequestBody UpdateQuizDto updateQuizDto) {
                String currentUserId = SecurityUtil.getCurrentUserId();
                return quizService.updateQuiz(sectionId, lessonId, updateQuizDto, currentUserId);
        }

        @GetMapping("/{sectionId}/lessons/{lessonId}/quizzes")
        @Operation(summary = "Get quiz questions for a lesson", description = """
                        Retrieves paginated quiz questions for a specific lesson owned by the instructor.

                        **Permission Requirements:**
                        - User must have INSTRUCTOR role
                        - User must own the course containing the section
                        - Lesson must belong to the specified section
                        - Lesson must be of type 'QUIZ'

                        **Query Parameters:**
                        - page: Page number (0-based indexing). Default: 0
                        - size: Number of items per page (1-100). Default: 10

                        **Business Rules:**
                        - Only quiz lessons can have their questions retrieved
                        - Results are ordered by creation date (ascending)
                        - Includes question text, options, correct answer, and explanation
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Quiz questions retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid section/lesson relationship or lesson is not a quiz type"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - instructor does not own the course"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Section or lesson not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during quiz questions retrieval")
        })
        public ResponseEntity<ApiResponse<PaginatedResponse<QuizQuestionResponseDto>>> getQuizQuestions(
                        @Parameter(description = "Section ID containing the lesson", required = true) @PathVariable String sectionId,
                        @Parameter(description = "Lesson ID containing the quiz questions", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Number of items per page (1-100)") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
                return instructorLessonService.getQuizQuestions(sectionId, lessonId, pageable);
        }

        /**
         * Get lesson submissions for instructors
         * Endpoint: GET /api/instructor/sections/lessons/{lessonId}/submissions
         * 
         * @param lessonId The ID of the lesson to get submissions for
         * @param page     Page number (0-based indexing)
         * @param size     Number of items per page (1-50)
         * @return LessonSubmissionsResponseDto containing submissions data and summary
         */
        @GetMapping("/lessons/{lessonId}/submissions")
        @Operation(summary = "Get lesson submissions", description = """
                        Retrieves paginated submissions for a quiz lesson owned by the instructor.
                        Includes both students who have submitted and those who haven't, along with summary statistics.

                        **Permission Requirements:**
                        - User must have INSTRUCTOR role
                        - User must own the course containing the lesson
                        - Lesson must be of type 'QUIZ'

                        **Query Parameters:**
                        - page: Page number (0-based indexing). Default: 0
                        - size: Number of items per page (1-50). Default: 10

                        **Business Rules:**
                        - Each student can only have ONE submission per lesson
                        - If a student resubmits, it will overwrite the previous submission completely
                        - Returns all enrolled students with their submission status (submitted or not_submitted)
                        - Submission statuses: not_submitted, submitted, graded
                        - Results are ordered by submission date (most recent first), then by student name for non-submitted
                        - Score range: 0.00 to 100.00 (stored as percentage)
                        - Submission timestamps must be in the past

                        **Response includes:**
                        - Summary statistics (total students, submitted count, graded count, average score)
                        - Paginated list of student submissions with status
                        - Student information (ID, name, email)
                        - Submission details (ID, score, timestamps) when available
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Submissions retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid lesson - not a quiz type or invalid pagination parameters"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - instructor does not own the lesson"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Lesson not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during submissions retrieval")
        })
        public ResponseEntity<ApiResponse<LessonSubmissionsResponseDto>> getSubmissions(
                        @Parameter(description = "Lesson ID to get submissions for", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
                        @Parameter(description = "Number of items per page (1-50)") @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {

                Pageable pageable = PageRequest.of(page, size);
                return instructorLessonService.getSubmissions(lessonId, pageable);
        }

        /**
         * Get submission details for instructors
         * Endpoint: GET
         * /api/instructor/sections/lessons/{lessonId}/submissions/{submissionId}
         * 
         * @param lessonId     The ID of the lesson
         * @param submissionId The ID of the submission
         * @return SubmissionDetailResponseDto containing detailed submission
         *         information
         */
        @GetMapping("/lessons/{lessonId}/submissions/{submissionId}")
        @Operation(summary = "Get submission details", description = """
                        Retrieves detailed information about a specific student submission for a quiz lesson.
                        Only the instructor who owns the course can access this endpoint.

                        **Permission Requirements:**
                        - User must have INSTRUCTOR role
                        - User must own the course containing the lesson
                        - Lesson must be of type 'QUIZ'
                        - Submission must belong to the specified lesson

                        **Business Rules:**
                        - Returns complete submission data including student information
                        - Includes all quiz questions with student answers and correctness status
                        - Shows question explanations for instructor review
                        - Validates submission-lesson relationship
                        - Enforces instructor ownership of the course

                        **Response includes:**
                        - Student information (ID, name, email)
                        - Submission metadata (ID, score, submission timestamp, status)
                        - Complete list of questions with answers, options, and correctness
                        - Question explanations for detailed review
                        """)
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Submission details retrieved successfully"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request - lesson and submission do not match"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - instructor does not own the course"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Submission or lesson not found"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Server error during submission details retrieval")
        })
        public ResponseEntity<ApiResponse<SubmissionDetailResponseDto>> getSubmissionDetails(
                        @Parameter(description = "Lesson ID", required = true) @PathVariable String lessonId,
                        @Parameter(description = "Submission ID", required = true) @PathVariable String submissionId) {

                return instructorLessonService.getSubmissionDetails(lessonId, submissionId);
        }

}