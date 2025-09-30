package project.ktc.springboot_app.cache.mappers;

import java.time.LocalDateTime;
import project.ktc.springboot_app.cache.dto.InstructorStatisticsCacheDto;
import project.ktc.springboot_app.instructor_dashboard.dto.InsDashboardDto;
import project.ktc.springboot_app.instructor_dashboard.dto.StatisticItemDto;

/**
 * Static utility class for mapping between InsDashboardDto and
 * InstructorStatisticsCacheDto Follows
 * the established pattern of CategoryCacheMapper for clean separation of
 * concerns between service
 * DTOs and cache DTOs.
 */
public final class InstructorStatisticsCacheMapper {

	private InstructorStatisticsCacheMapper() {
		// Prevent instantiation
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Converts InsDashboardDto to cache DTO for Redis storage
	 *
	 * @param insDashboardDto
	 *            the service DTO
	 * @param instructorId
	 *            the instructor identifier
	 * @return cache DTO optimized for Redis
	 */
	public static InstructorStatisticsCacheDto toCacheDto(
			InsDashboardDto insDashboardDto, String instructorId) {
		if (insDashboardDto == null) {
			return null;
		}

		return InstructorStatisticsCacheDto.builder()
				.courseTitle(
						insDashboardDto.getCourseStatistics() != null
								? insDashboardDto.getCourseStatistics().getTitle()
								: null)
				.courseValue(
						insDashboardDto.getCourseStatistics() != null
								? insDashboardDto.getCourseStatistics().getValue()
								: null)
				.courseDescription(
						insDashboardDto.getCourseStatistics() != null
								? insDashboardDto.getCourseStatistics().getDescription()
								: null)
				.studentTitle(
						insDashboardDto.getStudentStatistics() != null
								? insDashboardDto.getStudentStatistics().getTitle()
								: null)
				.studentValue(
						insDashboardDto.getStudentStatistics() != null
								? insDashboardDto.getStudentStatistics().getValue()
								: null)
				.studentDescription(
						insDashboardDto.getStudentStatistics() != null
								? insDashboardDto.getStudentStatistics().getDescription()
								: null)
				.revenueTitle(
						insDashboardDto.getRevenueStatistics() != null
								? insDashboardDto.getRevenueStatistics().getTitle()
								: null)
				.revenueValue(
						insDashboardDto.getRevenueStatistics() != null
								? insDashboardDto.getRevenueStatistics().getValue()
								: null)
				.revenueDescription(
						insDashboardDto.getRevenueStatistics() != null
								? insDashboardDto.getRevenueStatistics().getDescription()
								: null)
				.ratingTitle(
						insDashboardDto.getRatingStatistics() != null
								? insDashboardDto.getRatingStatistics().getTitle()
								: null)
				.ratingValue(
						insDashboardDto.getRatingStatistics() != null
								? insDashboardDto.getRatingStatistics().getValue()
								: null)
				.ratingDescription(
						insDashboardDto.getRatingStatistics() != null
								? insDashboardDto.getRatingStatistics().getDescription()
								: null)
				.instructorId(instructorId)
				.cachedAt(LocalDateTime.now())
				.build();
	}

	/**
	 * Converts cache DTO back to service DTO
	 *
	 * @param cacheDto
	 *            the cache DTO
	 * @return service DTO for business logic
	 */
	public static InsDashboardDto fromCacheDto(InstructorStatisticsCacheDto cacheDto) {
		if (cacheDto == null) {
			return null;
		}

		StatisticItemDto courseStatistics = StatisticItemDto.builder()
				.title(cacheDto.getCourseTitle())
				.value(cacheDto.getCourseValue())
				.description(cacheDto.getCourseDescription())
				.build();

		StatisticItemDto studentStatistics = StatisticItemDto.builder()
				.title(cacheDto.getStudentTitle())
				.value(cacheDto.getStudentValue())
				.description(cacheDto.getStudentDescription())
				.build();

		StatisticItemDto revenueStatistics = StatisticItemDto.builder()
				.title(cacheDto.getRevenueTitle())
				.value(cacheDto.getRevenueValue())
				.description(cacheDto.getRevenueDescription())
				.build();

		StatisticItemDto ratingStatistics = StatisticItemDto.builder()
				.title(cacheDto.getRatingTitle())
				.value(cacheDto.getRatingValue())
				.description(cacheDto.getRatingDescription())
				.build();

		return InsDashboardDto.builder()
				.courseStatistics(courseStatistics)
				.studentStatistics(studentStatistics)
				.revenueStatistics(revenueStatistics)
				.ratingStatistics(ratingStatistics)
				.build();
	}
}
