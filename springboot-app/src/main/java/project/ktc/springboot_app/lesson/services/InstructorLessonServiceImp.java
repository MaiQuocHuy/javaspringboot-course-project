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
import project.ktc.springboot_app.lesson.dto.ReorderLessonsDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonDto;
import project.ktc.springboot_app.lesson.dto.UpdateLessonResponseDto;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.interfaces.LessonService;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.lesson.repositories.LessonCompletionRepository;
import project.ktc.springboot_app.lesson.repositories.LessonTypeRepository;
import project.ktc.springboot_app.entity.LessonCompletion;
import project.ktc.springboot_app.entity.LessonType;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.LessonDto;
import project.ktc.springboot_app.section.dto.QuizDto;
import project.ktc.springboot_app.section.dto.QuizQuestionDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.VideoDto;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.upload.dto.VideoUploadResponseDto;
import project.ktc.springboot_app.upload.service.CloudinaryServiceImp;
import project.ktc.springboot_app.upload.service.FileValidationService;
import project.ktc.springboot_app.upload.exception.InvalidVideoFormatException;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.video.repositories.VideoContentRepository;
import project.ktc.springboot_app.log.services.SystemLogHelper;
import project.ktc.springboot_app.log.dto.LessonLogDto;
import project.ktc.springboot_app.log.utils.LessonLogMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorLessonServiceImp implements LessonService {

    private final InstructorLessonRepository lessonRepository;
    private final InstructorSectionRepository sectionRepository;
    private final VideoContentRepository videoContentRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final LessonTypeRepository lessonTypeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final CloudinaryServiceImp cloudinaryService;
    private final FileValidationService fileValidationService;
    private final SystemLogHelper systemLogHelper;

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
                .map((lesson) -> convertToLessonDto(lesson, currentUserId))
                .collect(Collectors.toList());

        SectionWithLessonsDto sectionWithLessonsDto = createSectionWithLessonsDto(section, lessonDtos);

        // Create and return SectionWithLessonsDto
        return ApiResponseUtil.success(sectionWithLessonsDto, "Section with lessons retrieved successfully");
    }

    private SectionWithLessonsDto createSectionWithLessonsDto(Section section, List<LessonDto> lessonDtos) {
        return SectionWithLessonsDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .orderIndex(section.getOrderIndex())
                .lessonCount(lessonDtos.size())
                .lessons(lessonDtos)
                .build();
    }

    private LessonDto convertToLessonDto(Lesson lesson, String currentUserId) {
        Boolean isCompleted = lessonCompletionRepository.existsByUserIdAndLessonId(currentUserId, lesson.getId());
        String lessonType = lesson.getLessonType() != null ? lesson.getLessonType().getName() : "UNKNOWN";

        LessonDto.LessonDtoBuilder builder = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lessonType)
                .order(lesson.getOrderIndex())
                .isCompleted(isCompleted);

        // Add type-specific data based on content relationship
        if (lesson.getContent() != null) {
            // If content is not null, this is a VIDEO lesson
            VideoDto videoDto = VideoDto.builder()
                    .id(lesson.getContent().getId())
                    .url(lesson.getContent().getUrl())
                    .duration(lesson.getContent().getDuration())
                    .build();
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
            // 3. Handle video upload for VIDEO type lessons
            final String contentId;
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
            } else {
                contentId = null;
            }

            // 4. Get current lesson count in section for order index (0-based)
            // List<Lesson> existingLessons =
            // lessonRepository.findLessonsBySectionIdOrderByOrder(sectionId);
            // int nextOrderIndex = existingLessons.size(); // 0-based indexing
            Integer nextOrderIndex = calculateNextOrderIndex(sectionId);

            // 5. Create lesson entity
            Lesson lesson = new Lesson();
            lesson.setTitle(lessonDto.getTitle().trim());

            // Set lesson type using LessonType entity
            LessonType lessonType = lessonTypeRepository.findByName(lessonDto.getType())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid lesson type: " + lessonDto.getType()));
            lesson.setLessonType(lessonType);

            // Set content for VIDEO type
            if (contentId != null) {
                VideoContent videoContent = videoContentRepository.findById(contentId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Video content not found with ID: " + contentId));
                lesson.setContent(videoContent);
            }

            lesson.setOrderIndex(nextOrderIndex);
            lesson.setSection(section);

            // Save lesson
            Lesson savedLesson = lessonRepository.save(lesson);
            log.info("Lesson created successfully with ID: {} and order index: {}", savedLesson.getId(),
                    nextOrderIndex);

            // Log the lesson creation
            try {
                User instructor = section.getCourse().getInstructor();
                LessonLogDto lessonLogDto = LessonLogMapper.toLogDto(savedLesson);
                systemLogHelper.logCreate(instructor, "Lesson", savedLesson.getId(), lessonLogDto);
                log.debug("Lesson creation logged successfully for lessonId: {}", savedLesson.getId());
            } catch (Exception logException) {
                log.warn("Failed to log lesson creation: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            // 6. Build response DTO
            CreateLessonResponseDto responseDto = CreateLessonResponseDto.builder()
                    .id(savedLesson.getId())
                    .title(savedLesson.getTitle())
                    .type(savedLesson.getLessonType() != null ? savedLesson.getLessonType().getName() : "UNKNOWN")
                    .orderIndex(savedLesson.getOrderIndex())
                    .build();

            // Add video information if it's a video lesson
            String lessonTypeName = savedLesson.getLessonType() != null ? savedLesson.getLessonType().getName()
                    : "UNKNOWN";
            if ("VIDEO".equals(lessonTypeName) && savedLesson.getContent() != null) {
                VideoDto videoDto = getVideoContent(savedLesson.getContent().getId());
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

        // Capture old values for logging
        LessonLogDto oldLessonLogDto = LessonLogMapper.toLogDto(lesson);

        // 3. Validate that at least one field is being updated
        boolean hasValidTitle = lessonDto.getTitle() != null && !lessonDto.getTitle().trim().isEmpty();
        boolean hasValidVideoFile = videoFile != null && !videoFile.isEmpty();

        if (!hasValidTitle && !hasValidVideoFile) {
            return ApiResponseUtil.badRequest("At least one field must be provided for update (title or videoFile)");
        }

        // 4. Validate lesson type cannot be changed (only if type is provided)
        String currentLessonType = lesson.getLessonType() != null ? lesson.getLessonType().getName() : "UNKNOWN";
        if (lessonDto.getType() != null && !currentLessonType.equals(lessonDto.getType())) {
            return ApiResponseUtil.badRequest(
                    "Lesson type cannot be changed. Current type: " + currentLessonType);
        }

        try {
            // 5. Handle video upload for VIDEO type lessons
            final String newContentId;
            if ("VIDEO".equals(currentLessonType) && videoFile != null && !videoFile.isEmpty()) {
                // Validate the video file
                fileValidationService.validateVideoFile(videoFile);

                // Upload new video to Cloudinary
                VideoUploadResponseDto uploadResult = cloudinaryService.uploadVideo(videoFile);
                log.info("New video uploaded successfully: {}", uploadResult.getPublicId());

                // Delete old video if exists
                if (lesson.getContent() != null) {
                    VideoContent oldVideo = lesson.getContent();
                    String publicId = extractPublicIdFromUrl(oldVideo.getUrl());
                    if (publicId != null) {
                        cloudinaryService.deleteVideo(publicId);
                        log.info("Old video deleted: {}", publicId);
                    }
                    // Delete old video content record
                    videoContentRepository.delete(oldVideo);
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
            } else {
                newContentId = lesson.getContent() != null ? lesson.getContent().getId() : null;
            }

            // 6. Update lesson (only update fields that are provided)
            if (lessonDto.getTitle() != null && !lessonDto.getTitle().trim().isEmpty()) {
                lesson.setTitle(lessonDto.getTitle().trim());
            }

            // Set content for the lesson
            if (newContentId != null) {
                VideoContent videoContent = videoContentRepository.findById(newContentId)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Video content not found with ID: " + newContentId));
                lesson.setContent(videoContent);
            } else if (!"VIDEO".equals(currentLessonType)) {
                // For non-video lessons, ensure content is null
                lesson.setContent(null);
            }

            // Save updated lesson
            Lesson savedLesson = lessonRepository.save(lesson);
            log.info("Lesson updated successfully with ID: {}", savedLesson.getId());

            // Log the lesson update
            try {
                User instructor = section.getCourse().getInstructor();
                LessonLogDto newLessonLogDto = LessonLogMapper.toLogDto(savedLesson);
                systemLogHelper.logUpdate(instructor, "Lesson", savedLesson.getId(), oldLessonLogDto, newLessonLogDto);
                log.debug("Lesson update logged successfully for lessonId: {}", savedLesson.getId());
            } catch (Exception logException) {
                log.warn("Failed to log lesson update: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            // 7. Build response DTO
            UpdateLessonResponseDto responseDto = UpdateLessonResponseDto.builder()
                    .id(savedLesson.getId())
                    .title(savedLesson.getTitle())
                    .type(savedLesson.getLessonType() != null ? savedLesson.getLessonType().getName() : "UNKNOWN")
                    .orderIndex(savedLesson.getOrderIndex())
                    .build();

            // Add video information if it's a video lesson
            String savedLessonType = savedLesson.getLessonType() != null ? savedLesson.getLessonType().getName()
                    : "UNKNOWN";
            if ("VIDEO".equals(savedLessonType) && savedLesson.getContent() != null) {
                VideoDto videoDto = getVideoContent(savedLesson.getContent().getId());
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
            // Capture lesson data for logging before deletion
            LessonLogDto lessonLogDto = LessonLogMapper.toLogDto(lesson);

            // 3. Delete associated video if lesson is of type VIDEO
            String lessonTypeName = lesson.getLessonType() != null ? lesson.getLessonType().getName() : "UNKNOWN";
            if ("VIDEO".equals(lessonTypeName) && lesson.getContent() != null) {
                VideoContent video = lesson.getContent();
                String publicId = extractPublicIdFromUrl(video.getUrl());
                if (publicId != null) {
                    cloudinaryService.deleteVideo(publicId);
                    log.info("Video deleted from Cloudinary: {}", publicId);
                }
                // Delete video content record
                videoContentRepository.delete(video);
                log.info("VideoContent record deleted with ID: {}", video.getId());
            }

            // 4. Delete the lesson
            lessonRepository.delete(lesson);
            log.info("Lesson deleted successfully with ID: {}", lessonId);

            // Log the lesson deletion
            try {
                User instructor = section.getCourse().getInstructor();
                systemLogHelper.logDelete(instructor, "Lesson", lessonId, lessonLogDto);
                log.debug("Lesson deletion logged successfully for lessonId: {}", lessonId);
            } catch (Exception logException) {
                log.warn("Failed to log lesson deletion: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

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

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> reorderLessons(String sectionId, ReorderLessonsDto reorderLessonsDto) {
        log.info("Reordering lessons for sectionId: {}, new order: {}",
                sectionId, reorderLessonsDto.getLessonOrder());

        String currentUserId = SecurityUtil.getCurrentUserId();

        try {
            // 1. Verify section exists and belongs to instructor
            Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
            }

            Section section = sectionOpt.get();

            // Check if the section belongs to the instructor
            if (!section.getCourse().getInstructor().getId().equals(currentUserId)) {
                return ApiResponseUtil.forbidden("You do not have permission to reorder lessons in this section");
            }

            // 2. Get all current lessons for the section
            List<Lesson> currentLessons = lessonRepository.findLessonsBySectionIdOrderByOrder(sectionId);

            // 3. Validate the reorder request
            String validationError = validateLessonOrder(currentLessons, reorderLessonsDto.getLessonOrder());
            if (validationError != null) {
                log.warn("Invalid lesson order for section {}: {}", sectionId, validationError);
                return ApiResponseUtil.badRequest(validationError);
            }

            // 4. Reorder lessons based on the new order
            reorderLessonsWithNewOrder(currentLessons, reorderLessonsDto.getLessonOrder());

            log.info("Lessons reordered successfully for section {}", sectionId);
            return ApiResponseUtil.success(null, "Lessons reordered successfully");

        } catch (Exception e) {
            log.error("Error reordering lessons for section {}: {}", sectionId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to reorder lessons: " + e.getMessage());
        }
    }

    /**
     * Validates that the provided lesson order contains all and only the lessons
     * that belong to the section, with no duplicates.
     */
    private String validateLessonOrder(List<Lesson> currentLessons, List<String> newOrder) {
        if (newOrder == null || newOrder.isEmpty()) {
            return "Lesson order cannot be empty";
        }

        if (currentLessons.size() != newOrder.size()) {
            return String.format("Lesson order must contain exactly %d lessons, but got %d",
                    currentLessons.size(), newOrder.size());
        }

        // Check for duplicates in the new order
        Set<String> orderSet = new HashSet<>(newOrder);
        if (orderSet.size() != newOrder.size()) {
            return "Lesson order contains duplicate IDs";
        }

        // Check that all lesson IDs in the new order exist and belong to the section
        Set<String> currentLessonIds = new HashSet<>();
        for (Lesson lesson : currentLessons) {
            currentLessonIds.add(lesson.getId());
        }

        for (String lessonId : newOrder) {
            if (!currentLessonIds.contains(lessonId)) {
                return String.format("Lesson ID %s does not exist or does not belong to this section", lessonId);
            }
        }

        // Check that all current lessons are included in the new order
        for (String currentId : currentLessonIds) {
            if (!orderSet.contains(currentId)) {
                return String.format("Missing lesson ID %s in the reorder request", currentId);
            }
        }

        return null; // No validation errors
    }

    /**
     * Updates the order index of lessons based on the new order list.
     * Each lesson gets an order index equal to its position in the list (0-based).
     * Uses a two-phase approach with dynamic offset calculation to avoid unique
     * constraint violations.
     */
    @Transactional
    private void reorderLessonsWithNewOrder(List<Lesson> lessons, List<String> newOrder) {
        log.info("Updating lesson order for {} lessons", newOrder.size());

        try {
            // Create a Map for O(1) lookup instead of nested loops
            Map<String, Lesson> lessonMap = lessons.stream()
                    .collect(Collectors.toMap(Lesson::getId, lesson -> lesson));

            // Build the ordered list of lessons to update
            List<Lesson> lessonsToUpdate = newOrder.stream()
                    .map(lessonMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (lessonsToUpdate.isEmpty()) {
                log.warn("No valid lessons found to reorder");
                return;
            }

            // Calculate dynamic offset to avoid conflicts
            Integer maxCurrentOrder = lessons.stream()
                    .mapToInt(Lesson::getOrderIndex)
                    .max()
                    .orElse(0);
            int offset = maxCurrentOrder + 1000;

            log.info("Using offset {} to avoid conflicts (max order: {})", offset, maxCurrentOrder);

            // Phase 1: Set temporary offset values to avoid constraint violations
            for (int i = 0; i < lessonsToUpdate.size(); i++) {
                lessonsToUpdate.get(i).setOrderIndex(offset + i);
            }

            lessonRepository.saveAll(lessonsToUpdate);
            lessonRepository.flush(); // Force immediate database update
            log.info("Step 1 complete: Set temporary offset order for {} lessons", lessonsToUpdate.size());

            // Phase 2: Set final order values (0-based indexing)
            for (int i = 0; i < lessonsToUpdate.size(); i++) {
                lessonsToUpdate.get(i).setOrderIndex(i);
            }

            lessonRepository.saveAll(lessonsToUpdate);
            lessonRepository.flush(); // Force immediate database update
            log.info("Step 2 complete: Set final order for {} lessons", lessonsToUpdate.size());

        } catch (Exception e) {
            log.error("Error updating lesson order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update lesson order", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<String>> completeLesson(String sectionId, String lessonId) {
        log.info("Instructor completion request for lesson {} in section {}", lessonId, sectionId);

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();

            // Verify section exists and belongs to instructor
            Optional<Section> sectionOpt = sectionRepository.findById(sectionId);
            if (sectionOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Section not found with id: " + sectionId);
            }

            Section section = sectionOpt.get();
            if (!section.getCourse().getInstructor().getId().equals(currentUserId)) {
                return ApiResponseUtil.forbidden("You do not have permission to access this section");
            }

            // Verify lesson exists and belongs to the section
            Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
            if (lessonOpt.isEmpty()) {
                return ApiResponseUtil.notFound("Lesson not found with id: " + lessonId);
            }

            Lesson lesson = lessonOpt.get();
            if (!lesson.getSection().getId().equals(sectionId)) {
                return ApiResponseUtil.badRequest("Lesson does not belong to the specified section");
            }

            // Check if lesson is already completed by the instructor (idempotent operation)
            boolean alreadyCompleted = lessonCompletionRepository.existsByUserIdAndLessonId(currentUserId, lessonId);
            if (alreadyCompleted) {
                log.info("Lesson {} already completed by instructor {}", lessonId, currentUserId);
                return ApiResponseUtil.success("Lesson completion already recorded",
                        "Lesson completion updated successfully");
            }

            // Get instructor user entity
            Optional<User> userOpt = userRepository.findById(currentUserId);
            if (userOpt.isEmpty()) {
                return ApiResponseUtil.notFound("User not found with id: " + currentUserId);
            }
            User instructor = userOpt.get();

            // Create lesson completion record
            LessonCompletion completion = new LessonCompletion();
            completion.setUser(instructor);
            completion.setLesson(lesson);
            completion.setCompletedAt(LocalDateTime.now());

            lessonCompletionRepository.save(completion);
            log.info("Successfully recorded lesson completion for instructor {} on lesson {}", currentUserId, lessonId);

            return ApiResponseUtil.success("Lesson completion recorded successfully",
                    "Lesson completion recorded successfully");

        } catch (Exception e) {
            log.error("Error completing lesson {} for instructor: {}", lessonId, e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to record lesson completion: " + e.getMessage());
        }
    }

}
