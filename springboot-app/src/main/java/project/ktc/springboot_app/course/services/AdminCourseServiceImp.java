package project.ktc.springboot_app.course.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.dto.CourseReviewDetailResponseDto;
import project.ktc.springboot_app.course.dto.CourseReviewFilterDto;
import project.ktc.springboot_app.course.dto.CourseReviewResponseDto;
import project.ktc.springboot_app.course.dto.CourseReviewStatusUpdateResponseDto;
import project.ktc.springboot_app.course.dto.UpdateCourseReviewStatusDto;
import project.ktc.springboot_app.course.dto.projection.CourseReviewProjection;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.entity.CourseReviewStatus;
import project.ktc.springboot_app.course.entity.CourseReviewStatusHistory;
import project.ktc.springboot_app.course.interfaces.AdminCourseService;
import project.ktc.springboot_app.course.repositories.AdminCourseRepository;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.course.repositories.CourseReviewStatusRepository;
import project.ktc.springboot_app.course.repositories.CourseReviewStatusHistoryRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.user.repositories.UserRepository;
import project.ktc.springboot_app.auth.entitiy.User;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCourseServiceImp implements AdminCourseService {

    private final AdminCourseRepository adminCourseRepository;
    private final CourseRepository courseRepository;
    private final InstructorSectionRepository sectionRepository;
    private final InstructorLessonRepository lessonRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final CourseReviewStatusRepository courseReviewStatusRepository;
    private final CourseReviewStatusHistoryRepository courseReviewStatusHistoryRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<CourseReviewResponseDto>>> getReviewCourses(
            CourseReviewFilterDto filterDto, Pageable pageable) {
        log.info("Fetching courses for review with filters: status={}, createdBy={}, dateRange=[{} to {}]",
                filterDto.getStatus(), filterDto.getCreatedBy(), filterDto.getDateFrom(), filterDto.getDateTo());

        try {
            // Apply default status filter if none provided
            List<String> statusList = getStatusFilter(filterDto.getStatus());

            // Execute query
            Page<CourseReviewProjection> projectionPage = adminCourseRepository
                    .findCoursesForReview(
                            statusList,
                            filterDto.getCreatedBy(),
                            filterDto.getDateFrom(),
                            filterDto.getDateTo(),
                            pageable);

            // Map projections to DTOs
            Page<CourseReviewResponseDto> responsePage = projectionPage.map(this::mapProjectionToDto);

            String message = statusList.isEmpty() ? "All courses retrieved successfully"
                    : "Courses with status " + String.join(", ", statusList) + " retrieved successfully";

            log.info("Successfully retrieved {} courses for review", responsePage.getTotalElements());

            PaginatedResponse<CourseReviewResponseDto> body = toPaginated(responsePage);
            return ApiResponseUtil.success(body, message);

        } catch (Exception e) {
            log.error("Error fetching courses for review: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve courses for review", e);
        }
    }

    private List<String> getStatusFilter(List<String> requestedStatuses) {
        if (requestedStatuses == null || requestedStatuses.isEmpty()) {
            // Default to PENDING and RESUBMITTED if no status provided
            return Arrays.asList("PENDING", "RESUBMITTED");
        }

        // Validate status values
        List<String> validStatuses = Arrays.asList("PENDING", "APPROVED", "DENIED", "RESUBMITTED");
        for (String status : requestedStatuses) {
            if (!validStatuses.contains(status.toUpperCase())) {
                throw new IllegalArgumentException("Invalid status: " + status);
            }
        }

        return requestedStatuses;
    }

    private PaginatedResponse<CourseReviewResponseDto> toPaginated(Page<CourseReviewResponseDto> page) {
        return PaginatedResponse.<CourseReviewResponseDto>builder()
                .content(page.getContent())
                .page(PaginatedResponse.PageInfo.builder()
                        .number(page.getNumber())
                        .size(page.getSize())
                        .totalPages(page.getTotalPages())
                        .totalElements(page.getTotalElements())
                        .first(page.isFirst())
                        .last(page.isLast())
                        .build())
                .build();
    }

    private CourseReviewResponseDto mapProjectionToDto(CourseReviewProjection projection) {
        return CourseReviewResponseDto.builder()
                .id(projection.getId())
                .title(projection.getTitle())
                .description(projection.getDescription())
                .createdBy(CourseReviewResponseDto.CreatedByDto.builder()
                        .id(projection.getCreatedById())
                        .name(projection.getCreatedByName())
                        .build())
                .createdAt(projection.getCreatedAt())
                .status(projection.getStatus())
                .countSection(projection.getCountSection())
                .countLesson(projection.getCountLesson())
                .totalDuration(projection.getTotalDuration())
                .statusUpdatedAt(projection.getStatusUpdatedAt())
                .build();
    }

    @Override
    public ResponseEntity<ApiResponse<CourseReviewDetailResponseDto>> getCourseReviewDetail(String courseId) {
        log.info("Fetching course review detail for course ID: {}", courseId);

        try {
            // Find the course with instructor details
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isEmpty()) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }

            Course course = courseOpt.get();

            // Get sections with lessons
            List<Section> sections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

            // Map course to DTO
            CourseReviewDetailResponseDto courseDetail = mapCourseToDetailDto(course, sections);

            log.info("Successfully retrieved course review detail for course ID: {}", courseId);
            return ApiResponseUtil.success(courseDetail, "Course retrieved successfully");

        } catch (Exception e) {
            log.error("Error fetching course review detail for course ID {}: {}", courseId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve course review detail", e);
        }
    }

    private CourseReviewDetailResponseDto mapCourseToDetailDto(Course course, List<Section> sections) {
        // Calculate aggregated data
        int totalLessons = sections.stream()
                .mapToInt(section -> section.getLessons().size())
                .sum();

        int totalDuration = sections.stream()
                .flatMap(section -> section.getLessons().stream())
                .filter(lesson -> lesson.getContent() instanceof VideoContent)
                .mapToInt(lesson -> {
                    VideoContent video = (VideoContent) lesson.getContent();
                    return video.getDuration() != null ? video.getDuration() : 0;
                })
                .sum();

        // Map sections
        List<CourseReviewDetailResponseDto.SectionDetailDto> sectionDtos = sections.stream()
                .map(this::mapSectionToDetailDto)
                .collect(Collectors.toList());

        return CourseReviewDetailResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .createdBy(CourseReviewDetailResponseDto.CreatedByDto.builder()
                        .id(course.getInstructor().getId())
                        .name(course.getInstructor().getName())
                        .email(course.getInstructor().getEmail())
                        .build())
                .countSection(sections.size())
                .countLesson(totalLessons)
                .totalDuration(totalDuration)
                .sections(sectionDtos)
                .build();
    }

    private CourseReviewDetailResponseDto.SectionDetailDto mapSectionToDetailDto(Section section) {
        List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(section.getId());

        // Calculate section aggregated data
        int totalVideoDuration = lessons.stream()
                .filter(lesson -> lesson.getContent() instanceof VideoContent)
                .mapToInt(lesson -> {
                    VideoContent video = (VideoContent) lesson.getContent();
                    return video.getDuration() != null ? video.getDuration() : 0;
                })
                .sum();

        int totalQuizQuestions = lessons.stream()
                .mapToInt(lesson -> (int) quizQuestionRepository.countByLessonId(lesson.getId()))
                .sum();

        // Map lessons
        List<CourseReviewDetailResponseDto.LessonDetailDto> lessonDtos = lessons.stream()
                .map(this::mapLessonToDetailDto)
                .collect(Collectors.toList());

        return CourseReviewDetailResponseDto.SectionDetailDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .order(section.getOrderIndex())
                .lessonCount(lessons.size())
                .totalVideoDuration(totalVideoDuration)
                .totalQuizQuestion(totalQuizQuestions)
                .lessons(lessonDtos)
                .build();
    }

    private CourseReviewDetailResponseDto.LessonDetailDto mapLessonToDetailDto(Lesson lesson) {
        CourseReviewDetailResponseDto.VideoDetailDto videoDto = null;
        CourseReviewDetailResponseDto.QuizDetailDto quizDto = null;

        // Check lesson type and map content
        if (lesson.getContent() instanceof VideoContent) {
            VideoContent video = (VideoContent) lesson.getContent();
            videoDto = CourseReviewDetailResponseDto.VideoDetailDto.builder()
                    .id(video.getId())
                    .url(video.getUrl())
                    .duration(video.getDuration())
                    .build();
        } else {
            // This is a quiz lesson, get quiz questions
            List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lesson.getId());
            List<CourseReviewDetailResponseDto.QuestionDetailDto> questionDtos = questions.stream()
                    .map(question -> {
                        List<String> options = parseOptionsFromJson(question.getOptions());
                        return CourseReviewDetailResponseDto.QuestionDetailDto.builder()
                                .id(question.getId())
                                .questionText(question.getQuestionText())
                                .options(options)
                                .correctAnswer(question.getCorrectAnswer())
                                .explanation(question.getExplanation())
                                .build();
                    })
                    .collect(Collectors.toList());

            quizDto = CourseReviewDetailResponseDto.QuizDetailDto.builder()
                    .questions(questionDtos)
                    .build();
        }

        return CourseReviewDetailResponseDto.LessonDetailDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getLessonType().getName()) // Using getLessonType() instead of getType()
                .video(videoDto)
                .quiz(quizDto)
                .build();
    }

    private List<String> parseOptionsFromJson(String optionsJson) {
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse options JSON: {}", optionsJson, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public ResponseEntity<ApiResponse<CourseReviewStatusUpdateResponseDto>> updateCourseReviewStatus(
            String courseId, UpdateCourseReviewStatusDto updateDto, String reviewerEmail) {
        log.info("Admin request to update course review status - courseId: {}, status: {}, reviewer: {}",
                courseId, updateDto.getStatus(), reviewerEmail);

        try {
            // Validate REJECTED status requires reason
            if ("REJECTED".equals(updateDto.getStatus()) &&
                    (updateDto.getReason() == null || updateDto.getReason().trim().isEmpty())) {
                return ApiResponseUtil.badRequest("Reason is required when status is REJECTED");
            }

            // Find course
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isEmpty()) {
                log.warn("Course not found with ID: {}", courseId);
                return ApiResponseUtil.notFound("Course not found");
            }
            Course course = courseOpt.get();

            // Find reviewer
            Optional<User> reviewerOpt = userRepository.findByEmail(reviewerEmail);
            if (reviewerOpt.isEmpty()) {
                log.warn("Reviewer not found with email: {}", reviewerEmail);
                return ApiResponseUtil.badRequest("Reviewer not found");
            }
            User reviewer = reviewerOpt.get();

            // Find or create course review status
            CourseReviewStatus reviewStatus = courseReviewStatusRepository.findByCourseId(courseId)
                    .orElseGet(() -> {
                        log.info("Creating new course review status for course: {}", courseId);
                        return CourseReviewStatus.builder()
                                .id(UUID.randomUUID().toString())
                                .course(course)
                                .status(CourseReviewStatus.ReviewStatus.PENDING)
                                .createdAt(LocalDateTime.now())
                                .build();
                    });

            // Map status from API to enum (REJECTED -> DENIED)
            CourseReviewStatus.ReviewStatus newStatus = "APPROVED".equals(updateDto.getStatus())
                    ? CourseReviewStatus.ReviewStatus.APPROVED
                    : CourseReviewStatus.ReviewStatus.DENIED;

            CourseReviewStatus.ReviewStatus previousStatus = reviewStatus.getStatus();

            // Update review status
            reviewStatus.setStatus(newStatus);
            reviewStatus.setUpdatedAt(LocalDateTime.now());
            CourseReviewStatus savedReviewStatus = courseReviewStatusRepository.save(reviewStatus);

            // Create history record
            CourseReviewStatusHistory history = CourseReviewStatusHistory.builder()
                    .id(UUID.randomUUID().toString())
                    .courseReview(savedReviewStatus)
                    .action(newStatus.name())
                    .reason(updateDto.getReason())
                    .reviewer(reviewer)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            courseReviewStatusHistoryRepository.save(history);

            // Build response DTO
            CourseReviewStatusUpdateResponseDto.CourseReviewStatusUpdateResponseDtoBuilder responseBuilder = CourseReviewStatusUpdateResponseDto
                    .builder()
                    .id(course.getId())
                    .title(course.getTitle())
                    .description(course.getDescription())
                    .status(updateDto.getStatus()); // Return API status (APPROVED/REJECTED)

            // Include reason only if status is REJECTED
            if ("REJECTED".equals(updateDto.getStatus())) {
                responseBuilder.reason(updateDto.getReason());
                Course courseRejected = courseRepository.findById(courseId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Course not found with ID: " + courseId));
                courseRejected.setIsApproved(false);
                courseRejected.setIsPublished(false);
                courseRejected.setUpdatedAt(LocalDateTime.now());
                courseRepository.save(courseRejected);
            }

            CourseReviewStatusUpdateResponseDto responseDto = responseBuilder.build();

            log.info("Successfully updated course review status - courseId: {}, previousStatus: {}, newStatus: {}",
                    courseId, previousStatus, newStatus);

            return ApiResponseUtil.success(responseDto, "Course review status updated successfully");

        } catch (Exception e) {
            log.error("Error updating course review status for courseId: {}", courseId, e);
            return ApiResponseUtil.internalServerError("Failed to update course review status");
        }
    }
}