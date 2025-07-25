package project.ktc.springboot_app.lesson.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.lesson.dto.CreateLessonDto;
import project.ktc.springboot_app.lesson.dto.CreateLessonResponseDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonResponseDto;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.interfaces.LessonService;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.LessonDto;
import project.ktc.springboot_app.section.dto.QuizDto;
import project.ktc.springboot_app.section.dto.QuizQuestionDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.VideoDto;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.SectionRepository;
import project.ktc.springboot_app.upload.dto.VideoUploadResponseDto;
import project.ktc.springboot_app.upload.service.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.service.FileValidationService;
import project.ktc.springboot_app.upload.exception.InvalidVideoFormatException;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.video.repositories.VideoContentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorLessonServiceImp implements LessonService {

    private final InstructorLessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final VideoContentRepository videoContentRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryServiceImp cloudinaryService;
    private final FileValidationService fileValidationService;

    @Override
    public ResponseEntity<ApiResponse<SectionWithLessonsDto>> getSectionWithLessons(String sectionId) {
        // Verify section exists and belongs to instructor

        String currentUserId = SecurityUtil.getCurrentUserId();

        Optional<Section> sectionOtp = sectionRepository.findById(sectionId);
        if (sectionOtp.isEmpty()) {
            return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
        }

        // Check if the section belongs to the instructor
        if (!sectionOtp.get().getCourse().getInstructor().getId().equals(currentUserId)) {
            return ApiResponseUtil.forbidden("You do not have permission to access this section");
        }
        Section section = sectionOtp.get();

        // Get lessons for the section
        List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(sectionId);

        // Convert lessons to DTOs
        List<LessonDto> lessonDtos = lessons.stream()
                .map(this::convertToLessonDto)
                .collect(Collectors.toList());

        SectionWithLessonsDto sectionWithLessonsDto = createSectionWithLessonsDto(section, lessonDtos);

        // Create and return SectionWithLessonsDto
        return ApiResponseUtil.success(sectionWithLessonsDto, "Section with lessons retrieved successfully");
    }

    private SectionWithLessonsDto createSectionWithLessonsDto(Section section, List<LessonDto> lessonDtos) {
        return SectionWithLessonsDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .order(section.getOrderIndex())
                .lessonCount(lessonDtos.size())
                .lessons(lessonDtos)
                .build();
    }

    private LessonDto convertToLessonDto(Lesson lesson) {
        LessonDto.LessonDtoBuilder builder = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getType())
                .order(lesson.getOrderIndex());

        // Add type-specific data based on content_id
        if (lesson.getContentId() != null) {
            // If content_id is not null, this is a VIDEO lesson
            VideoDto videoDto = getVideoContent(lesson.getContentId());
            builder.video(videoDto);
        } else {
            // If content_id is null, this is a QUIZ lesson
            QuizDto quizDto = getQuizContent(lesson.getId());
            builder.quiz(quizDto);
        }

        return builder.build();
    }

    private VideoDto getVideoContent(String contentId) {
        try {
            VideoContent videoContent = videoContentRepository.findById(contentId).orElse(null);
            if (videoContent != null) {
                return VideoDto.builder()
                        .id(videoContent.getId())
                        .url(videoContent.getUrl())
                        .duration(videoContent.getDuration())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Could not fetch video content for ID: {}", contentId, e);
        }
        return null;
    }

    private QuizDto getQuizContent(String lessonId) {
        try {
            List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lessonId);
            log.info("Retrieved {} questions for lesson {}", questions.size(), lessonId);
            List<QuizQuestionDto> questionDtos = questions.stream()
                    .map(this::convertToQuizQuestionDto)
                    .collect(Collectors.toList());

            return QuizDto.builder()
                    .questions(questionDtos)
                    .build();

        } catch (Exception e) {
            log.warn("Could not fetch quiz content for lesson ID: {}", lessonId, e);
            return QuizDto.builder()
                    .questions(new ArrayList<>())
                    .build();
        }
    }

    private QuizQuestionDto convertToQuizQuestionDto(QuizQuestion question) {
        // Parse options from JSON string
        List<String> options = new ArrayList<>();
        try {
            if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            log.warn("Could not parse options for question {}: {}", question.getId(), question.getOptions(), e);
        }

        return QuizQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(options)
                .correctAnswer(question.getCorrectAnswer())
                .build();
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<CreateLessonResponseDto>> createLesson(String sectionId,
            CreateLessonDto lessonDto, MultipartFile videoFile) {
        log.info("Creating lesson '{}' in section {} with type {}", lessonDto.getTitle(), sectionId,
                lessonDto.getType());

        String currentUserId = SecurityUtil.getCurrentUserId();

        // 1. Verify section exists and belongs to instructor
        Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
        if (sectionOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
        }

        Section section = sectionOpt.get();

        // Check if the section belongs to the instructor (section -> course ->
        // instructor)
        if (!section.getCourse().getInstructor().getId().equals(currentUserId)) {
            return ApiResponseUtil.forbidden("You do not have permission to add lessons to this section");
        }

        // 2. Validate lesson type and video file requirements
        if ("VIDEO".equals(lessonDto.getType())) {
            if (videoFile == null || videoFile.isEmpty()) {
                return ApiResponseUtil.badRequest("Video file is required for VIDEO type lessons");
            }
        }

        try {
            String contentId = null;

            // 3. Handle video upload for VIDEO type lessons
            if ("VIDEO".equals(lessonDto.getType()) && videoFile != null && !videoFile.isEmpty()) {
                // Validate the video file
                fileValidationService.validateVideoFile(videoFile);

                // Upload video to Cloudinary
                VideoUploadResponseDto uploadResult = cloudinaryService.uploadVideo(videoFile);
                log.info("Video uploaded successfully: {}", uploadResult.getPublicId());

                // Get current user for uploadedBy field
                String currentUserIdForVideo = SecurityUtil.getCurrentUserId();
                if (currentUserIdForVideo == null) {
                    return ApiResponseUtil.unauthorized("Unable to identify current user");
                }

                // Create VideoContent entity
                VideoContent videoContent = new VideoContent();
                videoContent.setUrl(uploadResult.getSecureUrl());

                // Convert duration from seconds (double) to integer
                if (uploadResult.getDuration() != null) {
                    videoContent.setDuration(uploadResult.getDuration().intValue());
                }

                // Set uploaded by user - create a User entity with just the ID
                project.ktc.springboot_app.auth.entitiy.User uploadedByUser = new project.ktc.springboot_app.auth.entitiy.User();
                uploadedByUser.setId(currentUserIdForVideo);
                videoContent.setUploadedBy(uploadedByUser);

                videoContent.setCreatedAt(LocalDateTime.now());
                videoContent.setUpdatedAt(LocalDateTime.now());

                // Save video content
                VideoContent savedVideoContent = videoContentRepository.save(videoContent);
                contentId = savedVideoContent.getId();

                log.info("VideoContent created with ID: {}", contentId);
            }

            // 4. Get current lesson count in section for order index (0-based)
            // List<Lesson> existingLessons =
            // lessonRepository.findLessonsBySectionIdOrderByOrder(sectionId);
            // int nextOrderIndex = existingLessons.size(); // 0-based indexing
            Integer nextOrderIndex = calculateNextOrderIndex(sectionId);

            // 5. Create lesson entity
            Lesson lesson = new Lesson();
            lesson.setTitle(lessonDto.getTitle().trim());
            lesson.setType(lessonDto.getType());
            lesson.setContentId(contentId); // null for QUIZ type
            lesson.setOrderIndex(nextOrderIndex);
            lesson.setSection(section);

            // Save lesson
            Lesson savedLesson = lessonRepository.save(lesson);
            log.info("Lesson created successfully with ID: {} and order index: {}", savedLesson.getId(),
                    nextOrderIndex);

            // 6. Build response DTO
            CreateLessonResponseDto responseDto = CreateLessonResponseDto.builder()
                    .id(savedLesson.getId())
                    .title(savedLesson.getTitle())
                    .type(savedLesson.getType())
                    .orderIndex(savedLesson.getOrderIndex())
                    .build();

            // Add video information if it's a video lesson
            if ("VIDEO".equals(savedLesson.getType()) && contentId != null) {
                VideoDto videoDto = getVideoContent(contentId);
                responseDto.setVideo(videoDto);
            }

            return ApiResponseUtil.created(responseDto, "Lesson created successfully");

        } catch (InvalidVideoFormatException e) {
            log.warn("Video validation failed for lesson '{}': {}", lessonDto.getTitle(), e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating lesson '{}' in section {}: {}", lessonDto.getTitle(), sectionId, e.getMessage(),
                    e);
            return ApiResponseUtil.internalServerError("Failed to create lesson. Please try again later.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<UpdateLessonResponseDto>> updateLesson(String sectionId, String lessonId,
            UpdateLessonDto lessonDto, MultipartFile videoFile) {
        log.info("Updating lesson with ID '{}' in section {}", lessonId, sectionId);

        String currentUserId = SecurityUtil.getCurrentUserId();

        // 1. Verify section exists and belongs to instructor
        Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
        if (sectionOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
        }

        Section section = sectionOpt.get();

        // Check if the section belongs to the instructor
        if (!section.getCourse().getInstructor().getId().equals(currentUserId)) {
            return ApiResponseUtil.forbidden("You do not have permission to update lessons in this section");
        }

        // 2. Verify lesson exists and belongs to the section
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Lesson not found with id: " + lessonId);
        }

        Lesson lesson = lessonOpt.get();

        // Check if lesson belongs to the section
        if (!lesson.getSection().getId().equals(sectionId)) {
            return ApiResponseUtil.badRequest("Lesson does not belong to the specified section");
        }

        // 3. Validate that at least one field is being updated
        boolean hasValidTitle = lessonDto.getTitle() != null && !lessonDto.getTitle().trim().isEmpty();
        boolean hasValidVideoFile = videoFile != null && !videoFile.isEmpty();

        if (!hasValidTitle && !hasValidVideoFile) {
            return ApiResponseUtil.badRequest("At least one field must be provided for update (title or videoFile)");
        }

        // 4. Validate lesson type cannot be changed (only if type is provided)
        if (lessonDto.getType() != null && !lesson.getType().equals(lessonDto.getType())) {
            return ApiResponseUtil.badRequest(
                    "Lesson type cannot be changed. Current type: " + lesson.getType());
        }

        try {
            String newContentId = lesson.getContentId(); // Keep existing content ID by default

            // 5. Handle video upload for VIDEO type lessons
            if ("VIDEO".equals(lesson.getType()) && videoFile != null && !videoFile.isEmpty()) {
                // Validate the video file
                fileValidationService.validateVideoFile(videoFile);

                // Upload new video to Cloudinary
                VideoUploadResponseDto uploadResult = cloudinaryService.uploadVideo(videoFile);
                log.info("New video uploaded successfully: {}", uploadResult.getPublicId());

                // Delete old video if exists
                if (lesson.getContentId() != null) {
                    Optional<VideoContent> oldVideoOpt = videoContentRepository.findById(lesson.getContentId());
                    if (oldVideoOpt.isPresent()) {
                        VideoContent oldVideo = oldVideoOpt.get();
                        String publicId = extractPublicIdFromUrl(oldVideo.getUrl());
                        if (publicId != null) {
                            cloudinaryService.deleteVideo(publicId);
                            log.info("Old video deleted: {}", publicId);
                        }
                        // Delete old video content record
                        videoContentRepository.delete(oldVideo);
                    }
                }

                // Create new VideoContent entity
                VideoContent videoContent = new VideoContent();
                videoContent.setUrl(uploadResult.getSecureUrl());

                // Convert duration from seconds (double) to integer
                if (uploadResult.getDuration() != null) {
                    videoContent.setDuration(uploadResult.getDuration().intValue());
                }

                // Set uploaded by user
                String currentUserIdForVideo = SecurityUtil.getCurrentUserId();
                if (currentUserIdForVideo == null) {
                    return ApiResponseUtil.unauthorized("Unable to identify current user");
                }

                project.ktc.springboot_app.auth.entitiy.User uploadedByUser = new project.ktc.springboot_app.auth.entitiy.User();
                uploadedByUser.setId(currentUserIdForVideo);
                videoContent.setUploadedBy(uploadedByUser);

                videoContent.setCreatedAt(LocalDateTime.now());
                videoContent.setUpdatedAt(LocalDateTime.now());

                // Save new video content
                VideoContent savedVideoContent = videoContentRepository.save(videoContent);
                newContentId = savedVideoContent.getId();

                log.info("New VideoContent created with ID: {}", newContentId);
            }

            // 6. Update lesson (only update fields that are provided)
            if (lessonDto.getTitle() != null && !lessonDto.getTitle().trim().isEmpty()) {
                lesson.setTitle(lessonDto.getTitle().trim());
            }
            lesson.setContentId(newContentId);

            // Save updated lesson
            Lesson savedLesson = lessonRepository.save(lesson);
            log.info("Lesson updated successfully with ID: {}", savedLesson.getId());

            // 7. Build response DTO
            UpdateLessonResponseDto responseDto = UpdateLessonResponseDto.builder()
                    .id(savedLesson.getId())
                    .title(savedLesson.getTitle())
                    .type(savedLesson.getType())
                    .orderIndex(savedLesson.getOrderIndex())
                    .build();

            // Add video information if it's a video lesson
            if ("VIDEO".equals(savedLesson.getType()) && newContentId != null) {
                VideoDto videoDto = getVideoContent(newContentId);
                responseDto.setVideo(videoDto);
            }

            return ApiResponseUtil.success(responseDto, "Lesson updated successfully");

        } catch (InvalidVideoFormatException e) {
            log.warn("Video validation failed for lesson '{}': {}", lessonId, e.getMessage());
            return ApiResponseUtil.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating lesson '{}' in section {}: {}", lessonId, sectionId, e.getMessage(),
                    e);
            return ApiResponseUtil.internalServerError("Failed to update lesson. Please try again later.");
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     * Example URL:
     * https://res.cloudinary.com/dlusux56d/video/upload/v1234567890/videos/abc123.mp4
     * Public ID: videos/abc123
     */
    private String extractPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // Split by '/' to get URL parts
            String[] parts = url.split("/");

            // Find the index of "upload"
            int uploadIndex = -1;
            for (int i = 0; i < parts.length; i++) {
                if ("upload".equals(parts[i])) {
                    uploadIndex = i;
                    break;
                }
            }

            if (uploadIndex == -1 || uploadIndex + 2 >= parts.length) {
                log.warn("Could not find 'upload' in URL or insufficient parts: {}", url);
                return null;
            }

            // Skip version part (starts with 'v') and get the rest
            StringBuilder publicId = new StringBuilder();
            for (int i = uploadIndex + 2; i < parts.length; i++) {
                if (i > uploadIndex + 2) {
                    publicId.append("/");
                }
                // Remove file extension from the last part
                String part = parts[i];
                if (i == parts.length - 1 && part.contains(".")) {
                    part = part.substring(0, part.lastIndexOf("."));
                }
                publicId.append(part);
            }

            return publicId.toString();
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", url, e);
            return null;
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> deleteLesson(String sectionId, String lessonId) {
        log.info("Deleting lesson with ID '{}' from section {}", lessonId, sectionId);

        String currentUserId = SecurityUtil.getCurrentUserId();

        // 1. Verify section exists and belongs to instructor
        Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
        if (sectionOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
        }

        Section section = sectionOpt.get();

        // Check if the section belongs to the instructor
        if (!section.getCourse().getInstructor().getId().equals(currentUserId)) {
            return ApiResponseUtil.forbidden("You do not have permission to delete lessons in this section");
        }

        // 2. Verify lesson exists and belongs to the section
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return ApiResponseUtil.notFound("Lesson not found with id: " + lessonId);
        }

        Lesson lesson = lessonOpt.get();

        // Check if lesson belongs to the section
        if (!lesson.getSection().getId().equals(sectionId)) {
            return ApiResponseUtil.badRequest("Lesson does not belong to the specified section");
        }

        try {
            // 3. Delete associated video if lesson is of type VIDEO
            if ("VIDEO".equals(lesson.getType()) && lesson.getContentId() != null) {
                Optional<VideoContent> videoOpt = videoContentRepository.findById(lesson.getContentId());
                if (videoOpt.isPresent()) {
                    VideoContent video = videoOpt.get();
                    String publicId = extractPublicIdFromUrl(video.getUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteVideo(publicId);
                        log.info("Video deleted from Cloudinary: {}", publicId);
                    }
                    // Delete video content record
                    videoContentRepository.delete(video);
                    log.info("VideoContent record deleted with ID: {}", video.getId());
                }
            }

            // 4. Delete the lesson
            lessonRepository.delete(lesson);
            log.info("Lesson deleted successfully with ID: {}", lessonId);

            // Get the order index of the lesson
            Integer deletedOrderIndex = lesson.getOrderIndex();

            // 5. Reorder remaining lessons in the section
            reorderLessonsAfterDeletion(sectionId, deletedOrderIndex);

            return ApiResponseUtil.success(null, "Lesson deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting lesson '{}' from section {}: {}", lessonId, sectionId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to delete lesson: " + e.getMessage());
        }
    }

    /**
     * Reorders lessons after a lesson is deleted to maintain continuous order
     * indices
     * 
     * @param sectionId    The section ID
     * @param deletedOrder The order index of the deleted lesson
     */
    private void reorderLessonsAfterDeletion(String sectionId, Integer deletedOrderIndex) {
        try {
            List<Lesson> lessonsToReorder = lessonRepository.findLessonsBySectionIdAndOrderGreaterThan(
                    sectionId, deletedOrderIndex);

            for (Lesson lesson : lessonsToReorder) {
                // Decrement order index by 1 for each lesson after the deleted one
                lesson.setOrderIndex(lesson.getOrderIndex() - 1);
            }

            // Save all reordered lessons in a single batch
            if (!lessonsToReorder.isEmpty()) {
                lessonRepository.saveAll(lessonsToReorder);
            }

        } catch (Exception e) {
            log.error("Error reordering lessons after deletion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reorder lessons after deletion: " + e.getMessage());
        }
    }

    private Integer calculateNextOrderIndex(String sectionId) {
        List<Lesson> existingLessons = lessonRepository.findLessonsBySectionIdOrderByOrder(sectionId);
        if (existingLessons.isEmpty()) {
            return 0;
        }
        return existingLessons.get(existingLessons.size() - 1).getOrderIndex() + 1;
    }

}
