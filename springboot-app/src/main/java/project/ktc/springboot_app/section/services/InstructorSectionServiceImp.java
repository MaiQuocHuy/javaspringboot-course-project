package project.ktc.springboot_app.section.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.lesson.repositories.LessonCompletionRepository;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.CreateSectionDto;
import project.ktc.springboot_app.section.dto.LessonDto;
import project.ktc.springboot_app.section.dto.QuizDto;
import project.ktc.springboot_app.section.dto.QuizQuestionDto;
import project.ktc.springboot_app.section.dto.ReorderSectionsDto;
import project.ktc.springboot_app.section.dto.SectionResponseDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.UpdateSectionDto;
import project.ktc.springboot_app.section.dto.VideoDto;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.interfaces.InstructorSectionService;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.video.repositories.VideoContentRepository;
import project.ktc.springboot_app.log.services.SystemLogHelper;
import project.ktc.springboot_app.log.dto.SectionLogDto;
import project.ktc.springboot_app.log.utils.SectionLogMapper;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.user.repositories.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstructorSectionServiceImp implements InstructorSectionService {

    private final InstructorSectionRepository sectionRepository;
    private final InstructorLessonRepository lessonRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final VideoContentRepository videoContentRepository;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;
    private final SystemLogHelper systemLogHelper;
    private final UserRepository userRepository;

    @Override
    public ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
            String courseId,
            String instructorId) {

        log.info("Getting course sections for courseId: {}, instructorId: {}", courseId, instructorId);

        try {
            String currentUserId = SecurityUtil.getCurrentUserId();
            // Verify course exists and instructor owns it
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to access this course's sections");
            }

            // Get all sections for the course
            List<Section> sections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

            // Convert to DTOs with lessons
            List<SectionWithLessonsDto> sectionDtos = sections.stream()
                    .map((section) -> convertToSectionWithLessonsDto(section, currentUserId))
                    .collect(Collectors.toList());

            log.info("Retrieved {} sections for course {}", sectionDtos.size(), courseId);
            return ApiResponseUtil.success(sectionDtos, "Sections retrieved successfully");

        } catch (Exception e) {
            log.error("Error getting course sections: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to retrieve course sections. Please try again later.");
        }
    }

    private SectionWithLessonsDto convertToSectionWithLessonsDto(Section section, String currentUserId) {
        // Get lessons for this section
        List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(section.getId());

        // Convert lessons to DTOs
        List<LessonDto> lessonDtos = lessons.stream()
                .map((lesson) -> convertToLessonDto(lesson, currentUserId))
                .collect(Collectors.toList());

        return SectionWithLessonsDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .orderIndex(section.getOrderIndex())
                .lessonCount(lessons.size())
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

        // Add type-specific data based on lesson type and content
        if ("VIDEO".equals(lessonType) && lesson.getContent() != null) {
            // This is a VIDEO lesson with content
            VideoDto videoDto = getVideoContent(lesson.getContent().getId());
            builder.video(videoDto);
        } else if ("QUIZ".equals(lessonType)) {
            // This is a QUIZ lesson
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
            if (question.getOptions() != null) {
                options = objectMapper.readValue(question.getOptions(), new TypeReference<List<String>>() {
                });
            }
        } catch (Exception e) {
            log.warn("Could not parse options for question {}: {}", question.getId(), e.getMessage());
        }

        return QuizQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(options)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }

    @Override
    public ResponseEntity<ApiResponse<SectionResponseDto>> createSection(
            String courseId,
            String instructorId,
            CreateSectionDto createSectionDto) {

        log.info("Creating section for courseId: {}, instructorId: {}, title: {}",
                courseId, instructorId, createSectionDto.getTitle());

        try {
            // Verify course exists and instructor owns it
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to create sections for this course");
            }

            // Calculate the next order index
            Integer nextOrderIndex = calculateNextOrderIndex(courseId);

            // Create new section
            Section section = new Section();
            section.setId(UUID.randomUUID().toString());
            section.setTitle(createSectionDto.getTitle());
            section.setDescription(createSectionDto.getDescription());
            section.setCourse(course);
            section.setOrderIndex(nextOrderIndex);

            // Save section
            Section savedSection = sectionRepository.save(section);

            // Log the section creation
            try {
                User instructor = userRepository.findById(instructorId).orElse(null);
                if (instructor != null) {
                    SectionLogDto sectionLogDto = SectionLogMapper.toLogDto(savedSection);
                    systemLogHelper.logCreate(instructor, "Section", savedSection.getId(), sectionLogDto);
                    log.debug("Section creation logged successfully for sectionId: {}", savedSection.getId());
                }
            } catch (Exception logException) {
                log.warn("Failed to log section creation: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            // Convert to response DTO
            SectionResponseDto responseDto = SectionResponseDto.builder()
                    .id(savedSection.getId())
                    .title(savedSection.getTitle())
                    .description(savedSection.getDescription())
                    .orderIndex(savedSection.getOrderIndex())
                    .courseId(savedSection.getCourse().getId())
                    .build();

            log.info("Section created successfully with ID: {}", savedSection.getId());
            return ApiResponseUtil.created(responseDto, "Section created successfully");

        } catch (Exception e) {
            log.error("Error creating section: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to create section. Please try again later.");
        }
    }

    private Integer calculateNextOrderIndex(String courseId) {
        List<Section> existingSections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);
        if (existingSections.isEmpty()) {
            return 0;
        }
        return existingSections.get(existingSections.size() - 1).getOrderIndex() + 1;
    }

    @Override
    public ResponseEntity<ApiResponse<SectionResponseDto>> updateSection(
            String courseId,
            String sectionId,
            String instructorId,
            UpdateSectionDto updateSectionDto) {

        log.info("Updating section {} for courseId: {}, instructorId: {}, title: {}, description: {}",
                sectionId, courseId, instructorId, updateSectionDto.getTitle(), updateSectionDto.getDescription());

        try {
            // Verify course exists and instructor owns it
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to update sections for this course");
            }

            // Find the section and verify it belongs to the course
            Section section = sectionRepository.findById(sectionId).orElse(null);
            if (section == null) {
                log.warn("Section not found with ID: {}", sectionId);
                return ApiResponseUtil.notFound("Section not found");
            }

            // Verify section belongs to the course
            if (!section.getCourse().getId().equals(courseId)) {
                log.warn("Section {} does not belong to course {}", sectionId, courseId);
                return ApiResponseUtil.notFound("Section not found in this course");
            }

            // Capture old values for logging
            SectionLogDto oldSectionLogDto = SectionLogMapper.toLogDto(section);

            // Update section title
            if (!updateSectionDto.getTitle().isEmpty()) {
                section.setTitle(updateSectionDto.getTitle());
            }
            if (!updateSectionDto.getDescription().isEmpty()) {
                section.setDescription(updateSectionDto.getDescription());
            }

            // Save updated section
            Section updatedSection = sectionRepository.save(section);

            // Log the section update
            try {
                User instructor = userRepository.findById(instructorId).orElse(null);
                if (instructor != null) {
                    SectionLogDto newSectionLogDto = SectionLogMapper.toLogDto(updatedSection);
                    systemLogHelper.logUpdate(instructor, "Section", updatedSection.getId(), oldSectionLogDto,
                            newSectionLogDto);
                    log.debug("Section update logged successfully for sectionId: {}", updatedSection.getId());
                }
            } catch (Exception logException) {
                log.warn("Failed to log section update: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            // Convert to response DTO
            SectionResponseDto responseDto = SectionResponseDto.builder()
                    .id(updatedSection.getId())
                    .title(updatedSection.getTitle())
                    .description(updatedSection.getDescription())
                    .orderIndex(updatedSection.getOrderIndex())
                    .courseId(updatedSection.getCourse().getId())
                    .build();

            log.info("Section updated successfully with ID: {}", updatedSection.getId());
            return ApiResponseUtil.success(responseDto, "Section updated successfully");

        } catch (Exception e) {
            log.error("Error updating section: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to update section. Please try again later.");
        }
    }

    @Override
    public ResponseEntity<ApiResponse<Void>> deleteSection(
            String courseId,
            String sectionId,
            String instructorId) {

        log.info("Deleting section {} for courseId: {}, instructorId: {}",
                sectionId, courseId, instructorId);

        try {
            // Verify course exists and instructor owns it
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to delete sections for this course");
            }

            // Find the section and verify it belongs to the course
            Section section = sectionRepository.findById(sectionId).orElse(null);
            if (section == null) {
                log.warn("Section not found with ID: {}", sectionId);
                return ApiResponseUtil.notFound("Section not found");
            }

            // Verify section belongs to the course
            if (!section.getCourse().getId().equals(courseId)) {
                log.warn("Section {} does not belong to course {}", sectionId, courseId);
                return ApiResponseUtil.notFound("Section not found in this course");
            }

            // Get the order index of the section to be deleted for reordering
            Integer deletedSectionOrder = section.getOrderIndex();

            // Capture section data for logging before deletion
            SectionLogDto sectionLogDto = SectionLogMapper.toLogDto(section);

            // Delete the section (this will cascade delete lessons if configured properly)
            sectionRepository.delete(section);
            log.info("Section {} deleted successfully", sectionId);

            // Log the section deletion
            try {
                User instructor = userRepository.findById(instructorId).orElse(null);
                if (instructor != null) {
                    systemLogHelper.logDelete(instructor, "Section", sectionId, sectionLogDto);
                    log.debug("Section deletion logged successfully for sectionId: {}", sectionId);
                }
            } catch (Exception logException) {
                log.warn("Failed to log section deletion: {}", logException.getMessage());
                // Don't fail the whole operation due to logging error
            }

            // Reorder remaining sections to maintain continuous order values
            reorderSectionsAfterDeletion(courseId, deletedSectionOrder);

            log.info("Section deleted and reordering completed for course {}", courseId);
            return ApiResponseUtil.noContent("Section deleted successfully");

        } catch (Exception e) {
            log.error("Error deleting section: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to delete section. Please try again later.");
        }
    }

    /**
     * Reorders sections after deletion to maintain continuous order values.
     * Sections with order index greater than the deleted section's order
     * will have their order decreased by 1.
     */
    private void reorderSectionsAfterDeletion(String courseId, Integer deletedOrderIndex) {
        log.info("Reordering sections for course {} after deletion at order {}", courseId, deletedOrderIndex);

        try {
            // Get all sections with order greater than the deleted section
            List<Section> sectionsToReorder = sectionRepository.findSectionsByCourseIdAndOrderGreaterThan(
                    courseId, deletedOrderIndex);

            // Decrease order index by 1 for each section
            for (Section section : sectionsToReorder) {
                section.setOrderIndex(section.getOrderIndex() - 1);
            }

            // Batch save all updated sections
            if (!sectionsToReorder.isEmpty()) {
                sectionRepository.saveAll(sectionsToReorder);
                log.info("Reordered {} sections for course {}", sectionsToReorder.size(), courseId);
            }

        } catch (Exception e) {
            log.error("Error reordering sections for course {}: {}", courseId, e.getMessage(), e);
            throw new RuntimeException("Failed to reorder sections after deletion", e);
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<Void>> reorderSections(
            String courseId,
            String instructorId,
            ReorderSectionsDto reorderSectionsDto) {

        log.info("Reordering sections for courseId: {}, instructorId: {}, new order: {}",
                courseId, instructorId, reorderSectionsDto.getSectionOrder());

        try {
            // Verify course exists and instructor owns it
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            // Check ownership
            if (!course.getInstructor().getId().equals(instructorId)) {
                log.warn("Instructor {} does not own course {}", instructorId, courseId);
                return ApiResponseUtil.forbidden("You are not allowed to reorder sections for this course");
            }

            // Get all current sections for the course
            List<Section> currentSections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

            // Validate the reorder request
            String validationError = validateSectionOrder(currentSections, reorderSectionsDto.getSectionOrder());
            if (validationError != null) {
                log.warn("Invalid section order for course {}: {}", courseId, validationError);
                return ApiResponseUtil.badRequest(validationError);
            }

            // Reorder sections based on the new order
            reorderSectionsWithNewOrder(currentSections, reorderSectionsDto.getSectionOrder());

            log.info("Sections reordered successfully for course {}", courseId);
            return ApiResponseUtil.success("Sections reordered successfully");

        } catch (Exception e) {
            log.error("Error reordering sections: {}", e.getMessage(), e);
            return ApiResponseUtil.internalServerError("Failed to reorder sections. Please try again later.");
        }
    }

    /**
     * Validates that the provided section order contains all and only the sections
     * that belong to the course, with no duplicates.
     */
    private String validateSectionOrder(List<Section> currentSections, List<String> newOrder) {
        if (newOrder == null || newOrder.isEmpty()) {
            return "Section order cannot be empty";
        }

        if (currentSections.size() != newOrder.size()) {
            return String.format("Section order must contain exactly %d sections, but got %d",
                    currentSections.size(), newOrder.size());
        }

        // Check for duplicates in the new order
        Set<String> orderSet = new HashSet<>(newOrder);
        if (orderSet.size() != newOrder.size()) {
            return "Section order contains duplicate IDs";
        }

        // Check that all section IDs in the new order exist and belong to the course
        Set<String> currentSectionIds = new HashSet<>();
        for (Section section : currentSections) {
            currentSectionIds.add(section.getId());
        }

        for (String sectionId : newOrder) {
            if (!currentSectionIds.contains(sectionId)) {
                return String.format("Section ID %s does not exist or does not belong to this course", sectionId);
            }
        }

        // Check that all current sections are included in the new order
        for (String currentId : currentSectionIds) {
            if (!orderSet.contains(currentId)) {
                return String.format("Missing section ID %s in the reorder request", currentId);
            }
        }

        return null; // No validation errors
    }

    /**
     * Updates the order index of sections based on the new order list.
     * Each section gets an order index equal to its position in the list (0-based).
     * Uses a two-phase approach with dynamic offset calculation to avoid unique
     * constraint violations.
     */
    @Transactional
    private void reorderSectionsWithNewOrder(List<Section> sections, List<String> newOrder) {
        log.info("Updating section order for {} sections", newOrder.size());

        try {
            // Create a Map for O(1) lookup instead of nested loops
            Map<String, Section> sectionMap = sections.stream()
                    .collect(Collectors.toMap(Section::getId, section -> section));

            // Build the ordered list of sections to update
            List<Section> sectionsToUpdate = newOrder.stream()
                    .map(sectionMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (sectionsToUpdate.isEmpty()) {
                log.warn("No valid sections found to reorder");
                return;
            }

            // Calculate dynamic offset to avoid conflicts
            Integer maxCurrentOrder = sections.stream()
                    .mapToInt(Section::getOrderIndex)
                    .max()
                    .orElse(0);
            int offset = maxCurrentOrder + 1000;

            log.info("Using offset {} to avoid conflicts (max order: {})", offset, maxCurrentOrder);

            // Phase 1: Set temporary offset values to avoid constraint violations
            for (int i = 0; i < sectionsToUpdate.size(); i++) {
                sectionsToUpdate.get(i).setOrderIndex(offset + i);
            }

            sectionRepository.saveAll(sectionsToUpdate);
            sectionRepository.flush(); // Force immediate database update
            log.info("Step 1 complete: Set temporary offset order for {} sections", sectionsToUpdate.size());

            // Phase 2: Set final order values (0-based indexing)
            for (int i = 0; i < sectionsToUpdate.size(); i++) {
                sectionsToUpdate.get(i).setOrderIndex(i);
            }

            sectionRepository.saveAll(sectionsToUpdate);
            sectionRepository.flush(); // Force immediate database update
            log.info("Step 2 complete: Set final order for {} sections", sectionsToUpdate.size());

        } catch (Exception e) {
            log.error("Error updating section order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update section order", e);
        }
    }
}
