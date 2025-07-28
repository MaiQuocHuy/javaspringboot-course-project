package project.ktc.springboot_app.course.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.ktc.springboot_app.auth.entitiy.User;
import project.ktc.springboot_app.category.entity.Category;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.dto.PaginatedResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.dto.CourseAdminResponseDto;
import project.ktc.springboot_app.course.dto.CourseApprovalResponseDto;
import project.ktc.springboot_app.course.dto.CourseDetailResponseDto;
import project.ktc.springboot_app.course.dto.CoursePublicResponseDto;
import project.ktc.springboot_app.course.entity.Course;
import project.ktc.springboot_app.course.enums.CourseLevel;
import project.ktc.springboot_app.course.interfaces.CourseService;
import project.ktc.springboot_app.course.repositories.CourseRepository;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.entity.VideoContent;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.LessonDto;
import project.ktc.springboot_app.section.dto.QuizDto;
import project.ktc.springboot_app.section.dto.QuizQuestionDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.VideoDto;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.utils.StringUtil;
import project.ktc.springboot_app.video.repositories.VideoContentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseServiceImp implements CourseService {
    private final CourseRepository courseRepository;
    private final VideoContentRepository videoContentRepository;
    private final InstructorSectionRepository sectionRepository;
    private final QuizQuestionRepository quizQuestionRepository;

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<CoursePublicResponseDto>>> findAllPublic(
            String search,
            String categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CourseLevel level,
            Pageable pageable) {

        log.info(
                "Finding public courses with filters: search={}, categoryId={}, minPrice={}, maxPrice={}, level={}, page={}",
                search, categoryId, minPrice, maxPrice, level, pageable.getPageNumber());

        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        Page<Course> coursePage = courseRepository.findPublishedCoursesWithFilters(
                search, categoryId, minPrice, maxPrice, level, pageable);

        // Get enrollment counts for all courses in this page
        List<String> courseIds = coursePage.getContent().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        final Map<String, Long> enrollmentCounts;
        if (!courseIds.isEmpty()) {
            List<Object[]> enrollmentData = courseRepository.findEnrollmentCountsByCourseIds(courseIds);
            enrollmentCounts = enrollmentData.stream()
                    .collect(Collectors.toMap(
                            data -> (String) data[0], // courseId
                            data -> (Long) data[1] // enrollmentCount
                    ));
        } else {
            enrollmentCounts = new HashMap<>();
        }

        List<CoursePublicResponseDto> courseResponses = coursePage.getContent().stream()
                .map(course -> mapToCoursePublicResponse(course, enrollmentCounts.getOrDefault(course.getId(), 0L)))
                .collect(Collectors.toList());

        // Create paginated response
        PaginatedResponse<CoursePublicResponseDto> paginatedResponse = PaginatedResponse
                .<CoursePublicResponseDto>builder()
                .content(courseResponses)
                .page(PaginatedResponse.PageInfo.builder()
                        .number(coursePage.getNumber())
                        .size(coursePage.getSize())
                        .totalPages(coursePage.getTotalPages())
                        .totalElements(coursePage.getTotalElements())
                        .first(coursePage.isFirst())
                        .last(coursePage.isLast())
                        .build())
                .build();

        return ApiResponseUtil.success(paginatedResponse, "Public courses retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<CourseDetailResponseDto>> findOnePublic(String courseId) {
        log.info("Finding course details for course ID: {}", courseId);

        // Step 1: Find the course with instructor only (avoid multiple bags)
        Course course = courseRepository.findPublishedCourseByIdWithDetails(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Step 2: Fetch categories separately to avoid MultipleBagFetchException
        Optional<Course> courseWithCategories = courseRepository.findCourseWithCategories(courseId);
        if (courseWithCategories.isPresent()) {
            course.setCategories(courseWithCategories.get().getCategories());
        }

        // Step 3: Fetch sections with lessons separately
        List<Section> sectionsWithLessons = courseRepository.findSectionsWithLessonsByCourseId(courseId);
        course.setSections(sectionsWithLessons);

        // Get rating information
        CourseDetailResponseDto.RatingSummary ratingSummary = getRatingSummary(courseId);

        // Get lesson count
        Long lessonCount = courseRepository.countLessonsByCourseId(courseId);

        Long enrollMentCount = courseRepository.countUserEnrolledInCourse(courseId);

        // Check if current user is enrolled
        Boolean isEnrolled = getCurrentUserEnrollmentStatus(courseId);

        // Get sample video URL (first video lesson if available)
        String sampleVideoUrl = getSampleVideoUrl(course);

        // Generate slug from title
        String slug = StringUtil.generateSlug(course.getTitle());

        // Map to DTO
        CourseDetailResponseDto responseDto = mapToCourseDetailResponse(
                course, ratingSummary, lessonCount.intValue(), isEnrolled, sampleVideoUrl, slug,
                enrollMentCount.intValue());

        return ApiResponseUtil.success(responseDto, "Course details retrieved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<PaginatedResponse<CourseAdminResponseDto>>> findCoursesForAdmin(
            Boolean isApproved,
            String categoryId,
            String search,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            CourseLevel level,
            Pageable pageable) {

        log.info(
                "Finding courses for admin with filters: isApproved={}, categoryId={}, search={}, minPrice={}, maxPrice={}, level={}, page={}",
                isApproved, categoryId, search, minPrice, maxPrice, level, pageable.getPageNumber());

        // Validate price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }

        Page<Course> coursePage = courseRepository.findCoursesForAdmin(
                isApproved, categoryId, search, minPrice, maxPrice, level, pageable);

        // Get enrollment counts for all courses in this page
        List<String> courseIds = coursePage.getContent().stream()
                .map(Course::getId)
                .collect(Collectors.toList());

        final Map<String, Long> enrollmentCounts;
        if (!courseIds.isEmpty()) {
            List<Object[]> enrollmentData = courseRepository.findEnrollmentCountsByCourseIds(courseIds);
            enrollmentCounts = enrollmentData.stream()
                    .collect(Collectors.toMap(
                            data -> (String) data[0], // courseId
                            data -> (Long) data[1] // enrollmentCount
                    ));
        } else {
            enrollmentCounts = new HashMap<>();
        }

        List<CourseAdminResponseDto> courseResponses = coursePage.getContent().stream()
                .map(course -> mapToCourseAdminResponse(course, enrollmentCounts.getOrDefault(course.getId(), 0L)))
                .collect(Collectors.toList());

        // Create paginated response
        PaginatedResponse<CourseAdminResponseDto> paginatedResponse = PaginatedResponse
                .<CourseAdminResponseDto>builder()
                .content(courseResponses)
                .page(PaginatedResponse.PageInfo.builder()
                        .number(coursePage.getNumber())
                        .size(coursePage.getSize())
                        .totalPages(coursePage.getTotalPages())
                        .totalElements(coursePage.getTotalElements())
                        .first(coursePage.isFirst())
                        .last(coursePage.isLast())
                        .build())
                .build();

        return ApiResponseUtil.success(paginatedResponse, "Courses retrieved successfully for admin");
    }

    private CourseDetailResponseDto.RatingSummary getRatingSummary(String courseId) {
        Double averageRating = courseRepository.findAverageRatingByCourseId(courseId).orElse(0.0);
        Long totalReviews = courseRepository.countReviewsByCourseId(courseId);

        return CourseDetailResponseDto.RatingSummary.builder()
                .average(averageRating)
                .totalReviews(totalReviews)
                .build();
    }

    private Boolean getCurrentUserEnrollmentStatus(String courseId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                User currentUser = (User) authentication.getPrincipal();
                return courseRepository.isUserEnrolledInCourse(courseId, currentUser.getId());
            }
        } catch (Exception e) {
            log.warn("Could not determine enrollment status: {}", e.getMessage());
        }
        return false;
    }

    private String getSampleVideoUrl(Course course) {
        if (course.getSections() != null && !course.getSections().isEmpty()) {
            for (Section section : course.getSections()) {
                if (section.getLessons() != null && !section.getLessons().isEmpty()) {
                    for (Lesson lesson : section.getLessons()) {
                        if ("VIDEO".equals(lesson.getType()) && lesson.getContentId() != null) {
                            // Get the actual video URL from VideoContent entity
                            Optional<VideoContent> videoContent = videoContentRepository
                                    .findById(lesson.getContentId());
                            if (videoContent.isPresent()) {
                                return videoContent.get().getUrl();
                            } else {
                                log.warn("VideoContent not found for contentId: {}", lesson.getContentId());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private CourseDetailResponseDto mapToCourseDetailResponse(Course course,
            CourseDetailResponseDto.RatingSummary ratingSummary,
            Integer lessonCount,
            Boolean isEnrolled,
            String sampleVideoUrl,
            String slug,
            Integer enrollCount) {

        // Map instructor
        CourseDetailResponseDto.InstructorSummary instructorSummary = null;
        if (course.getInstructor() != null) {
            instructorSummary = CourseDetailResponseDto.InstructorSummary.builder()
                    .id(course.getInstructor().getId())
                    .name(course.getInstructor().getName())
                    .build();
        }

        // Map sections and lessons
        List<CourseDetailResponseDto.SectionSummary> sectionSummaries = null;
        if (course.getSections() != null) {
            sectionSummaries = course.getSections().stream()
                    .sorted((s1, s2) -> Integer.compare(
                            s1.getOrderIndex() != null ? s1.getOrderIndex() : 0,
                            s2.getOrderIndex() != null ? s2.getOrderIndex() : 0))
                    .map(this::mapToSectionSummary)
                    .collect(Collectors.toList());
        }

        // Map Reviews
        List<CourseDetailResponseDto.ReviewSummary> reviewSummaries = courseRepository
                .findReviewsByCourseId(course.getId())
                .stream()
                .map(review -> CourseDetailResponseDto.ReviewSummary.builder()
                        .id(review.getId())
                        .rating(review.getRating())
                        .comment(review.getReviewText())
                        .userId(review.getUser().getId())
                        .userName(review.getUser().getName())
                        .createdAt(review.getReviewedAt())
                        .build())
                .collect(Collectors.toList());

        return CourseDetailResponseDto.builder()
                .id(course.getId())
                .slug(slug)
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .lessonCount(lessonCount)
                .enrollCount(enrollCount)
                .sampleVideoUrl(sampleVideoUrl)
                .rating(ratingSummary)
                .isEnrolled(isEnrolled)
                .instructor(instructorSummary)
                .sections(sectionSummaries)
                .reviews(reviewSummaries)
                .build();
    }

    private CourseDetailResponseDto.SectionSummary mapToSectionSummary(Section section) {
        List<CourseDetailResponseDto.LessonSummary> lessonSummaries = null;
        if (section.getLessons() != null) {
            lessonSummaries = section.getLessons().stream()
                    .sorted((l1, l2) -> Integer.compare(
                            l1.getOrderIndex() != null ? l1.getOrderIndex() : 0,
                            l2.getOrderIndex() != null ? l2.getOrderIndex() : 0))
                    .map(lesson -> CourseDetailResponseDto.LessonSummary.builder()
                            .id(lesson.getId())
                            .title(lesson.getTitle())
                            .type(lesson.getType())
                            .build())
                    .collect(Collectors.toList());
        }

        return CourseDetailResponseDto.SectionSummary.builder()
                .id(section.getId())
                .title(section.getTitle())
                .lessons(lessonSummaries)
                .build();
    }

    private CoursePublicResponseDto mapToCoursePublicResponse(Course course, Long enrollCount) {
        // Get primary category (first one if multiple)
        CoursePublicResponseDto.CategorySummary categorySum = null;
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            Category primaryCategory = course.getCategories().get(0);
            categorySum = CoursePublicResponseDto.CategorySummary.builder()
                    .id(primaryCategory.getId())
                    .name(primaryCategory.getName())
                    .build();
        }

        // Get instructor info
        CoursePublicResponseDto.InstructorSummary instructorSum = null;
        if (course.getInstructor() != null) {
            instructorSum = CoursePublicResponseDto.InstructorSummary.builder()
                    .id(course.getInstructor().getId())
                    .name(course.getInstructor().getName())
                    .avatar(course.getInstructor().getThumbnailUrl()) // Assuming thumbnail_url is avatar
                    .build();
        }

        // Average rating
        Double averageRating = courseRepository.findAverageRatingByCourseId(course.getId()).orElse(0.0);
        // Section Count
        Long sectionCount = sectionRepository.countSectionsByCourseId(course.getId());
        return CoursePublicResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .level(course.getLevel())
                .thumbnailUrl(course.getThumbnailUrl())
                .enrollCount(enrollCount)
                .averageRating(averageRating)
                .sectionCount(sectionCount)
                .category(categorySum)
                .instructor(instructorSum)
                .build();
    }

    private CourseAdminResponseDto mapToCourseAdminResponse(Course course, Long enrollCount) {
        // Get primary category (first one if multiple)
        CourseAdminResponseDto.CategoryInfo categoryInfo = null;
        if (course.getCategories() != null && !course.getCategories().isEmpty()) {
            Category primaryCategory = course.getCategories().get(0);
            categoryInfo = CourseAdminResponseDto.CategoryInfo.builder()
                    .id(primaryCategory.getId())
                    .name(primaryCategory.getName())
                    .build();
        }

        // Get instructor info
        CourseAdminResponseDto.InstructorInfo instructorInfo = null;
        if (course.getInstructor() != null) {
            instructorInfo = CourseAdminResponseDto.InstructorInfo.builder()
                    .id(course.getInstructor().getId())
                    .name(course.getInstructor().getName())
                    .email(course.getInstructor().getEmail())
                    .avatar(course.getInstructor().getThumbnailUrl()) // Assuming thumbnail_url is avatar
                    .build();
        }

        // Average rating and rating count
        Double averageRating = courseRepository.findAverageRatingByCourseId(course.getId()).orElse(0.0);
        Long ratingCount = courseRepository.countReviewsByCourseId(course.getId());

        // Section Count
        Long sectionCount = sectionRepository.countSectionsByCourseId(course.getId());

        return CourseAdminResponseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .instructor(instructorInfo)
                .isApproved(course.getIsApproved())
                .isPublished(course.getIsPublished())
                .level(course.getLevel())
                .price(course.getPrice())
                .enrollmentCount(enrollCount)
                .averageRating(averageRating)
                .ratingCount(ratingCount)
                .sectionCount(sectionCount)
                .category(categoryInfo)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    @Override
    public ResponseEntity<ApiResponse<CourseApprovalResponseDto>> approveCourse(String courseId) {
        log.info("Approving course with ID: {}", courseId);

        // Find the course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Check if course is already approved
        if (Boolean.TRUE.equals(course.getIsApproved())) {
            throw new IllegalStateException("Course is already approved");
        }

        // Check if course is deleted
        if (Boolean.TRUE.equals(course.getIsDeleted())) {
            throw new IllegalStateException("Cannot approve a deleted course");
        }

        // Approve the course
        course.setIsApproved(true);
        course.setUpdatedAt(LocalDateTime.now());

        Course savedCourse = courseRepository.save(course);

        log.info("Course {} approved successfully", courseId);

        // Create response DTO
        CourseApprovalResponseDto responseDto = CourseApprovalResponseDto.builder()
                .id(savedCourse.getId())
                .title(savedCourse.getTitle())
                .isApproved(savedCourse.getIsApproved())
                .approvedAt(savedCourse.getUpdatedAt())
                .build();

        return ApiResponseUtil.success(responseDto, "Course approved successfully");
    }

    @Override
    public ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseDetailsForAdmin(String courseId) {
        log.info("Admin retrieving course details for course ID: {}", courseId);

        // Verify course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        log.info("Found course: {} for admin review", course.getTitle());

        // Get sections with lessons for the course
        List<Section> sectionsWithLessons = courseRepository.findSectionsWithLessonsByCourseId(courseId);

        // Map to DTOs
        List<SectionWithLessonsDto> sectionDtos = sectionsWithLessons.stream()
                .map(this::mapToSectionWithLessonsDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} sections for course {}", sectionDtos.size(), courseId);

        return ApiResponseUtil.success(sectionDtos, "Course sections retrieved successfully");
    }

    private SectionWithLessonsDto mapToSectionWithLessonsDto(Section section) {
        List<LessonDto> lessonDtos = null;

        if (section.getLessons() != null && !section.getLessons().isEmpty()) {
            lessonDtos = section.getLessons().stream()
                    .sorted((l1, l2) -> Integer.compare(
                            l1.getOrderIndex() != null ? l1.getOrderIndex() : 0,
                            l2.getOrderIndex() != null ? l2.getOrderIndex() : 0))
                    .map(this::mapToLessonDto)
                    .collect(Collectors.toList());
        }

        return SectionWithLessonsDto.builder()
                .id(section.getId())
                .title(section.getTitle())
                .orderIndex(section.getOrderIndex())
                .lessonCount(lessonDtos != null ? lessonDtos.size() : 0)
                .lessons(lessonDtos)
                .build();
    }

    private LessonDto mapToLessonDto(Lesson lesson) {
        LessonDto.LessonDtoBuilder builder = LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getType())
                .order(lesson.getOrderIndex())
                .isCompleted(false); // For admin view, completion status is not relevant

        // Handle video content
        if ("VIDEO".equals(lesson.getType()) && lesson.getContentId() != null) {
            Optional<VideoContent> videoContent = videoContentRepository.findById(lesson.getContentId());
            if (videoContent.isPresent()) {
                VideoDto videoDto = VideoDto.builder()
                        .id(videoContent.get().getId())
                        .url(videoContent.get().getUrl())
                        .duration(videoContent.get().getDuration())
                        .build();
                builder.video(videoDto);
            }
        }

        // Handle quiz content
        if ("QUIZ".equals(lesson.getType())) {
            List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lesson.getId());
            if (!questions.isEmpty()) {
                List<QuizQuestionDto> questionDtos = questions.stream()
                        .map(this::mapToQuizQuestionDto)
                        .collect(Collectors.toList());

                QuizDto quizDto = QuizDto.builder()
                        .questions(questionDtos)
                        .build();
                builder.quiz(quizDto);
            }
        }

        return builder.build();
    }

    private QuizQuestionDto mapToQuizQuestionDto(QuizQuestion question) {
        // Parse options from JSON string
        List<String> options = null;
        try {
            if (question.getOptions() != null) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                options = mapper.readValue(question.getOptions(),
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                        });
            }
        } catch (Exception e) {
            log.warn("Failed to parse quiz question options for question {}: {}", question.getId(), e.getMessage());
        }

        return QuizQuestionDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .options(options)
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }
}
