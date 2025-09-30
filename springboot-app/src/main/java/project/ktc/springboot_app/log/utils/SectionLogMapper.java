package project.ktc.springboot_app.log.utils;

import project.ktc.springboot_app.log.dto.SectionLogDto;
import project.ktc.springboot_app.section.entity.Section;

/**
 * Utility class for mapping Section entity to SectionLogDto Used for system
 * logging and audit trail
 */
public class SectionLogMapper {

	/**
	 * Convert Section entity to SectionLogDto for logging purposes
	 *
	 * @param section
	 *            The section entity to convert
	 * @return SectionLogDto with essential data for logging
	 */
	public static SectionLogDto toLogDto(Section section) {
		if (section == null) {
			return null;
		}

		return SectionLogDto.builder()
				.id(section.getId())
				.title(section.getTitle())
				.description(section.getDescription())
				.orderIndex(section.getOrderIndex())
				.courseId(section.getCourse() != null ? section.getCourse().getId() : null)
				.courseTitle(section.getCourse() != null ? section.getCourse().getTitle() : null)
				.createdAt(section.getCreatedAt())
				.updatedAt(section.getUpdatedAt())
				.lessonCount(section.getLessons() != null ? section.getLessons().size() : 0)
				.build();
	}
}
