package project.ktc.springboot_app.course.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.ktc.springboot_app.cache.services.domain.CoursesCacheService;
import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.common.exception.ResourceNotFoundException;
import project.ktc.springboot_app.common.utils.ApiResponseUtil;
import project.ktc.springboot_app.course.dto.CourseProgressDto;
import project.ktc.springboot_app.course.dto.CourseProgressLessonDto;
import project.ktc.springboot_app.course.dto.CourseProgressSummaryDto;
import project.ktc.springboot_app.course.dto.CourseStructureLessonDto;
import project.ktc.springboot_app.course.dto.CourseStructureQuizDto;
import project.ktc.springboot_app.course.dto.CourseStructureSectionDto;
import project.ktc.springboot_app.course.dto.CourseStructureVideoDto;
import project.ktc.springboot_app.course.interfaces.StudentCourseService;
import project.ktc.springboot_app.enrollment.repositories.EnrollmentRepository;
import project.ktc.springboot_app.entity.LessonCompletion;
import project.ktc.springboot_app.entity.QuizQuestion;
import project.ktc.springboot_app.lesson.entity.Lesson;
import project.ktc.springboot_app.lesson.repositories.InstructorLessonRepository;
import project.ktc.springboot_app.lesson.repositories.LessonCompletionRepository;
import project.ktc.springboot_app.quiz.repositories.QuizQuestionRepository;
import project.ktc.springboot_app.section.dto.*;
import project.ktc.springboot_app.section.entity.Section;
import project.ktc.springboot_app.section.repositories.InstructorSectionRepository;
import project.ktc.springboot_app.utils.SecurityUtil;
import project.ktc.springboot_app.utils.StringUtil;

/**
 * Student Course Service Implementation
 *
 * <p>
 * This service handles course-related operations for students including: -
 * Retrieving course
 * sections with lessons - Loading lesson completion status and timestamps -
 * Optimized for
 * performance with batch queries to avoid N+1 problems
 *
 * <p>
 * Performance optimizations: - Batch loading of lesson completions to reduce
 * database queries -
 * Efficient mapping of completion data using pre-loaded maps
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentCourseServiceImp implements StudentCourseService {

	private final InstructorSectionRepository sectionRepository;
	private final InstructorLessonRepository lessonRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final LessonCompletionRepository lessonCompletionRepository;
	private final QuizQuestionRepository quizQuestionRepository;
	private final ObjectMapper objectMapper;
	private final CoursesCacheService coursesCacheService;

	/**
	 * Retrieves all sections and lessons for a course with completion status and
	 * timestamps.
	 *
	 * <p>
	 * This method performs the following operations: 1. Verifies user enrollment in
	 * the course 2.
	 * Fetches all sections for the course 3. Batch loads lesson completion data for
	 * performance
	 * optimization 4. Maps to DTOs with completion information including
	 * completedAt timestamps
	 *
	 * @param courseId
	 *            The ID of the course to retrieve sections for
	 * @return ResponseEntity containing list of sections with lessons and
	 *         completion data
	 */
	@Override
	public ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
			String courseId) {
		log.info("Fetching sections for course: {} for student", courseId);

		try {
			String currentUserId = SecurityUtil.getCurrentUserId();

			// Verify user is enrolled in the course
			boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId);
			if (!isEnrolled) {
				log.warn("User {} is not enrolled in course {}", currentUserId, courseId);
				return ApiResponseUtil.forbidden("You are not enrolled in this course");
			}

			// Fetch all sections for the course
			List<Section> sections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

			// Convert to DTOs with lessons
			List<SectionWithLessonsDto> sectionDtos = sections.stream()
					.map(section -> mapToSectionWithLessonsDto(section, currentUserId))
					.collect(Collectors.toList());

			log.info("Successfully retrieved {} sections for course {}", sectionDtos.size(), courseId);
			return ApiResponseUtil.success(sectionDtos, "Sections retrieved successfully");

		} catch (Exception e) {
			log.error("Error retrieving sections for course {}: {}", courseId, e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve sections. Please try again later.");
		}
	}

	private SectionWithLessonsDto mapToSectionWithLessonsDto(Section section, String userId) {
		// Get lessons for this section
		List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(section.getId());

		// Get all lesson IDs for batch query to avoid N+1 problem
		Set<String> lessonIds = lessons.stream().map(Lesson::getId).collect(Collectors.toSet());

		// Batch query for lesson completions to optimize performance
		List<LessonCompletion> completions = lessonCompletionRepository.findByUserIdAndLessonIdIn(userId, lessonIds);
		Map<String, LessonCompletion> completionMap = completions.stream()
				.collect(
						Collectors.toMap(
								completion -> completion.getLesson().getId(), completion -> completion));

		// Map lessons to DTOs with completion data
		List<LessonDto> lessonDtos = lessons.stream()
				.map(lesson -> mapToLessonDto(lesson, completionMap))
				.collect(Collectors.toList());

		return SectionWithLessonsDto.builder()
				.id(section.getId())
				.title(section.getTitle())
				.description(section.getDescription())
				.orderIndex(section.getOrderIndex())
				.lessonCount(lessons.size())
				.lessons(lessonDtos)
				.build();
	}

	private LessonDto mapToLessonDto(Lesson lesson, Map<String, LessonCompletion> completionMap) {
		// Get lesson completion data from the pre-loaded map (performance optimization)
		LessonCompletion lessonCompletion = completionMap.get(lesson.getId());
		boolean isCompleted = lessonCompletion != null;
		LocalDateTime completedAt = lessonCompletion != null ? lessonCompletion.getCompletedAt() : null;

		String lessonType = lesson.getLessonType() != null ? lesson.getLessonType().getName() : "UNKNOWN";

		LessonDto.LessonDtoBuilder lessonBuilder = LessonDto.builder()
				.id(lesson.getId())
				.title(lesson.getTitle())
				.type(lessonType)
				.order(lesson.getOrderIndex())
				.isCompleted(isCompleted)
				.completedAt(completedAt);

		// Add content based on lesson type
		if ("VIDEO".equals(lessonType) && lesson.getContent() != null) {
			String videoUrl = lesson.getContent().getUrl();

			VideoDto.VideoDtoBuilder videoBuilder = VideoDto.builder()
					.id(lesson.getContent().getId())
					.url(videoUrl)
					.duration(lesson.getContent().getDuration())
					.title(StringUtil.getBeforeDot(lesson.getContent().getTitle()))
					.thumbnail(lesson.getContent().getThumbnailUrl());

			lessonBuilder.video(videoBuilder.build());
		} else if ("QUIZ".equals(lessonType)) {
			// Note: This could be further optimized with batch loading for quiz questions
			// if there are many quiz lessons in a section
			List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lesson.getId());
			List<QuizQuestionDto> questionDtos = questions.stream().map(this::mapToQuizQuestionDto)
					.collect(Collectors.toList());

			QuizDto quizDto = QuizDto.builder().questions(questionDtos).build();
			lessonBuilder.quiz(quizDto);
		}

		return lessonBuilder.build();
	}

	private QuizQuestionDto mapToQuizQuestionDto(QuizQuestion question) {
		// Parse JSON options string to Map<String, String>
		Map<String, String> optionsMap = Collections.emptyMap();
		try {
			optionsMap = objectMapper.readValue(
					question.getOptions(), new TypeReference<Map<String, String>>() {
					});
		} catch (Exception e) {
			log.warn(
					"Failed to parse options JSON for question {}: {}", question.getId(), e.getMessage());
		}

		return QuizQuestionDto.builder()
				.id(question.getId())
				.questionText(question.getQuestionText())
				.options(optionsMap)
				.correctAnswer(question.getCorrectAnswer())
				.explanation(question.getExplanation())
				.build();
	}

	@Override
	public ResponseEntity<ApiResponse<List<CourseStructureSectionDto>>> getCourseStructureForStudent(
			String courseId) {
		log.info("Fetching course structure for course: {}", courseId);

		// 1. Check if user is enrolled in course
		String userId = SecurityUtil.getCurrentUserId();
		if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
			throw new ResourceNotFoundException("Course not found or not enrolled");
		}

		// 2. Try to get from cache first
		List<CourseStructureSectionDto> cachedStructure = coursesCacheService.getCourseStructure(courseId);
		if (cachedStructure != null) {
			log.debug("Course structure retrieved from cache for course: {}", courseId);
			return ApiResponseUtil.success(cachedStructure, "Course structure retrieved successfully");
		}

		// 3. Cache miss - fetch from database
		log.debug("Cache miss for course structure, fetching from database for course: {}", courseId);

		// 4. Get all sections with lessons for the course
		List<Section> sections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

		// 5. Map to course structure DTOs
		List<CourseStructureSectionDto> structureSections = sections.stream().map(this::mapToStructureSectionDto)
				.collect(Collectors.toList());

		// 6. Store in cache
		coursesCacheService.storeCourseStructure(courseId, structureSections);
		log.debug("Course structure cached for course: {}", courseId);

		return ApiResponseUtil.success(structureSections, "Course structure retrieved successfully");
	}

	private CourseStructureSectionDto mapToStructureSectionDto(Section section) {
		List<Lesson> lessons = lessonRepository.findLessonsBySectionIdOrderByOrder(section.getId());
		List<CourseStructureLessonDto> structureLessons = lessons.stream().map(this::mapToStructureLessonDto)
				.collect(Collectors.toList());

		return CourseStructureSectionDto.builder()
				.id(section.getId())
				.title(section.getTitle())
				.description(section.getDescription())
				.order(section.getOrderIndex())
				.lessonCount(lessons.size())
				.lessons(structureLessons)
				.build();
	}

	private CourseStructureLessonDto mapToStructureLessonDto(Lesson lesson) {
		CourseStructureLessonDto.CourseStructureLessonDtoBuilder builder = CourseStructureLessonDto.builder()
				.id(lesson.getId())
				.title(lesson.getTitle())
				.order(lesson.getOrderIndex());

		// Determine lesson type and add appropriate content
		String lessonTypeId = lesson.getLessonType().getId();
		if ("type-001".equals(lessonTypeId)) { // VIDEO type
			builder.type("VIDEO");
			if (lesson.getContent() != null) {
				builder.video(
						CourseStructureVideoDto.builder()
								.id(lesson.getContent().getId())
								.url(lesson.getContent().getUrl())
								.duration(lesson.getContent().getDuration())
								.title(lesson.getTitle())
								.thumbnail(lesson.getContent().getThumbnailUrl())
								.build());
			}
		} else if ("type-002".equals(lessonTypeId)) { // QUIZ type
			builder.type("QUIZ");
			List<QuizQuestion> questions = quizQuestionRepository.findQuestionsByLessonId(lesson.getId());
			if (!questions.isEmpty()) {
				List<CourseStructureQuizDto.QuizQuestion> quizQuestions = questions.stream()
						.map(this::mapToStructureQuizQuestion).collect(Collectors.toList());

				builder.quiz(CourseStructureQuizDto.builder().questions(quizQuestions).build());
			}
		}

		return builder.build();
	}

	private CourseStructureQuizDto.QuizQuestion mapToStructureQuizQuestion(QuizQuestion question) {
		Map<String, String> optionsMap = Collections.emptyMap();
		try {
			optionsMap = objectMapper.readValue(
					question.getOptions(), new TypeReference<Map<String, String>>() {
					});
		} catch (Exception e) {
			log.warn(
					"Failed to parse options JSON for question {}: {}", question.getId(), e.getMessage());
		}

		return CourseStructureQuizDto.QuizQuestion.builder()
				.id(question.getId())
				.questionText(question.getQuestionText())
				.options(optionsMap)
				.correctAnswer(question.getCorrectAnswer())
				.explanation(question.getExplanation())
				.build();
	}

	@Override
	public ResponseEntity<ApiResponse<CourseProgressDto>> getCourseProgressForStudent(
			String courseId) {
		log.info("Fetching course progress for course: {}", courseId);

		try {
			String currentUserId = SecurityUtil.getCurrentUserId();

			// 1. Verify user is enrolled in the course
			boolean isEnrolled = enrollmentRepository.existsByUserIdAndCourseId(currentUserId, courseId);
			if (!isEnrolled) {
				log.warn("User {} is not enrolled in course {}", currentUserId, courseId);
				return ApiResponseUtil.forbidden("You are not enrolled in this course");
			}

			// 2. Get all sections for the course
			List<Section> sections = sectionRepository.findSectionsByCourseIdOrderByOrder(courseId);

			// 3. Get all lessons across all sections, maintaining order
			List<Lesson> allLessons = sections.stream()
					.flatMap(
							section -> lessonRepository.findLessonsBySectionIdOrderByOrder(section.getId()).stream())
					.collect(Collectors.toList());

			// 4. Get completion data for all lessons
			Set<String> lessonIds = allLessons.stream().map(Lesson::getId).collect(Collectors.toSet());

			List<LessonCompletion> completions = lessonCompletionRepository.findByUserIdAndLessonIdIn(currentUserId,
					lessonIds);
			Map<String, LessonCompletion> completionMap = completions.stream()
					.collect(
							Collectors.toMap(
									completion -> completion.getLesson().getId(), completion -> completion));

			// 5. Calculate progress for each lesson with global sequential ordering
			List<CourseProgressLessonDto> progressLessons = new ArrayList<>();
			for (int i = 0; i < allLessons.size(); i++) {
				// Check if previous lesson is completed (for sequential unlocking)
				boolean isPreviousCompleted = i == 0 || completionMap.containsKey(allLessons.get(i - 1).getId());
				progressLessons.add(
						mapToProgressLessonDto(allLessons.get(i), completionMap, i, isPreviousCompleted));
			}

			// 6. Calculate summary statistics
			long completedCount = progressLessons.stream()
					.mapToLong(lesson -> "COMPLETED".equals(lesson.getStatus()) ? 1 : 0)
					.sum();

			int totalLessons = allLessons.size();
			int percentage = totalLessons > 0 ? (int) Math.round((double) completedCount / totalLessons * 100) : 0;

			CourseProgressSummaryDto summary = CourseProgressSummaryDto.builder()
					.completedCount((int) completedCount)
					.totalLessons(totalLessons)
					.percentage(percentage)
					.build();

			CourseProgressDto progressDto = CourseProgressDto.builder().summary(summary).lessons(progressLessons)
					.build();

			log.info(
					"Successfully calculated progress for course {}: {}/{} lessons completed ({}%)",
					courseId, completedCount, totalLessons, percentage);
			return ApiResponseUtil.success(progressDto, "Progress retrieved successfully");

		} catch (Exception e) {
			log.error("Error retrieving progress for course {}: {}", courseId, e.getMessage(), e);
			return ApiResponseUtil.internalServerError(
					"Failed to retrieve progress. Please try again later.");
		}
	}

	private CourseProgressLessonDto mapToProgressLessonDto(
			Lesson lesson,
			Map<String, LessonCompletion> completionMap,
			int globalOrder,
			boolean isPreviousCompleted) {
		LessonCompletion completion = completionMap.get(lesson.getId());

		// Determine lesson status based on completion and sequential rules
		String status;
		LocalDateTime completedAt = null;

		if (completion != null) {
			// Lesson is completed
			status = "COMPLETED";
			completedAt = completion.getCompletedAt();
		} else if (globalOrder == 0 || isPreviousCompleted) {
			// First lesson or previous lesson is completed - UNLOCKED
			status = "UNLOCKED";
		} else {
			// Previous lesson not completed - LOCKED
			status = "LOCKED";
		}

		return CourseProgressLessonDto.builder()
				.lessonId(lesson.getId())
				.order(globalOrder) // Use global sequential order instead of section-based order
				.status(status)
				.completedAt(completedAt)
				.build();
	}
}
