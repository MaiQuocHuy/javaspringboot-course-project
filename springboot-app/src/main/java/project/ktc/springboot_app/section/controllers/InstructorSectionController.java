package project.ktc.springboot_app.section.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import project.ktc.springboot_app.section.dto.CreateSectionDto;
import project.ktc.springboot_app.section.dto.ReorderSectionsDto;
import project.ktc.springboot_app.section.dto.SectionResponseDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;
import project.ktc.springboot_app.section.dto.UpdateSectionDto;
import project.ktc.springboot_app.section.services.InstructorSectionServiceImp;
import project.ktc.springboot_app.utils.SecurityUtil;

@RestController
@Tag(name = "Instructor Course Sections API", description = "Endpoints for managing course sections and lessons")
@RequestMapping("/api/instructor/courses")
@PreAuthorize("hasRole('INSTRUCTOR')")
@RequiredArgsConstructor
@Slf4j
public class InstructorSectionController {

	private final InstructorSectionServiceImp sectionService;

	@GetMapping("/{id}/sections")
	@Operation(summary = "Get course sections with lessons", description = """
			Retrieve all sections and their lessons for a course owned by the instructor.

			**Permission Requirements:**
			- User must have INSTRUCTOR role
			- User must own the course (course.instructor_id == currentUser.id)

			**Response includes:**
			- Section details (id, title, order, lesson count)
			- All lessons in each section ordered by order index
			- Video content details for VIDEO type lessons
			- Quiz questions for QUIZ type lessons

			**Business Rules:**
			- Only course owner can access sections
			- Returns empty array if course has no sections
			- Lessons are ordered by orderIndex within each section
			- Quiz questions include options, correct answers, and explanations
			""", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Sections retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "You are not allowed to access this course's sections"),
			@ApiResponse(responseCode = "404", description = "Course not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
			@Parameter(description = "Course ID", required = true) @PathVariable("id") String courseId) {

		log.info("Received request to get sections for course: {}", courseId);

		String instructorId = SecurityUtil.getCurrentUserId();

		// Call service to get course sections
		return sectionService.getCourseSections(courseId, instructorId);
	}

	@PostMapping("/{id}/sections")
	@Operation(summary = "Create a new section", description = """
			Create a new section in a course owned by the instructor.

			**Permission Requirements:**
			- User must have INSTRUCTOR role
			- User must own the course (course.instructor_id == currentUser.id)

			**Request Body:**
			- title: Section title (required, 3-255 characters)
			- description: Section description (optional, max 255 characters)

			**Business Rules:**
			- Only course owner can create sections
			- Section title must be unique within the course
			- New section gets the next order index automatically
			- Section order starts from 0 for the first section

			**Response includes:**
			- Section ID (UUID)
			- Section title
			- Order index
			- Course ID
			""", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Section created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "403", description = "You are not allowed to create sections for this course"),
			@ApiResponse(responseCode = "404", description = "Course not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<SectionResponseDto>> createSection(
			@Parameter(description = "Course ID", required = true) @PathVariable("id") String courseId,
			@Parameter(description = "Section creation data", required = true) @Valid @RequestBody CreateSectionDto createSectionDto) {

		log.info(
				"Received request to create section for course: {} with title: {} and description: {}",
				courseId,
				createSectionDto.getTitle(),
				createSectionDto.getDescription());

		String instructorId = SecurityUtil.getCurrentUserId();

		// Call service to create section
		return sectionService.createSection(courseId, instructorId, createSectionDto);
	}

	@PatchMapping("/{courseId}/sections/{sectionId}")
	@Operation(summary = "Update an existing section", description = """
			Update an existing section in a course owned by the instructor.

			**Permission Requirements:**
			- User must have INSTRUCTOR role
			- User must own the course (course.instructor_id == currentUser.id)

			**Request Body:**
			- title: Updated section title (required, 3-255 characters)

			**Business Rules:**
			- Only course owner can update sections
			- Section must exist and belong to the specified course
			- Section title must be valid (3-255 characters)
			- Order index remains unchanged during update

			**Response includes:**
			- Section ID (unchanged)
			- Updated section title
			- Order index (unchanged)
			- Course ID (unchanged)
			""", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Section updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "403", description = "You are not allowed to update sections for this course"),
			@ApiResponse(responseCode = "404", description = "Course or section not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<SectionResponseDto>> updateSection(
			@Parameter(description = "Course ID", required = true) @PathVariable("courseId") String courseId,
			@Parameter(description = "Section ID", required = true) @PathVariable("sectionId") String sectionId,
			@Parameter(description = "Section update data", required = true) @Valid @RequestBody UpdateSectionDto updateSectionDto) {

		log.info(
				"Received request to update section {} in course: {} with title: {}",
				sectionId,
				courseId,
				updateSectionDto.getTitle());

		String instructorId = SecurityUtil.getCurrentUserId();

		// Call service to update section
		return sectionService.updateSection(courseId, sectionId, instructorId, updateSectionDto);
	}

	@DeleteMapping("/{courseId}/sections/{sectionId}")
	@Operation(summary = "Delete an existing section", description = """
			Delete an existing section from a course owned by the instructor.
			After deletion, the system automatically reorders the remaining sections to maintain continuous order values.

			**Permission Requirements:**
			- User must have INSTRUCTOR role
			- User must own the course (course.instructor_id == currentUser.id)

			**Business Rules:**
			- Only course owner can delete sections
			- Section must exist and belong to the specified course
			- Deletion cascades to associated lessons in the section
			- Remaining sections are automatically reordered to maintain continuous order (0,1,2,3...)

			**Response:**
			- 204 No Content on successful deletion
			- Section data is permanently removed from the system
			- All lessons in the section are also deleted

			**Reordering Logic:**
			- Sections with order index greater than the deleted section will have their order decreased by 1
			- This ensures a continuous sequence of order values without gaps
			""", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Section deleted successfully"),
			@ApiResponse(responseCode = "403", description = "You are not allowed to delete sections for this course"),
			@ApiResponse(responseCode = "404", description = "Course or section not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> deleteSection(
			@Parameter(description = "Course ID", required = true) @PathVariable("courseId") String courseId,
			@Parameter(description = "Section ID", required = true) @PathVariable("sectionId") String sectionId) {

		log.info("Received request to delete section {} from course: {}", sectionId, courseId);

		String instructorId = SecurityUtil.getCurrentUserId();

		// Call service to delete section
		return sectionService.deleteSection(courseId, sectionId, instructorId);
	}

	@PatchMapping("/{courseId}/sections/reorder")
	@Operation(summary = "Reorder sections within a course", description = """
			Reorder all sections within a course owned by the instructor.
			The client must send a complete, ordered array of section IDs.

			**Permission Requirements:**
			- User must have INSTRUCTOR role
			- User must own the course (course.instructor_id == currentUser.id)

			**Request Body:**
			- sectionOrder: Complete array of section IDs in the desired order

			**Business Rules:**
			- Only course owner can reorder sections
			- Array must contain ALL section IDs from the course
			- Array must not contain duplicate IDs
			- Array must not contain IDs from other courses
			- Each section gets an order index equal to its position in the array (0-based)

			**Response:**
			- 200 OK on successful reordering
			- All sections are updated with new order indices
			- Order indices are sequential starting from 0

			**Validation:**
			- Checks that all current section IDs are included
			- Checks that no extra or duplicate IDs are present
			- Verifies all IDs belong to the specified course
			""", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Sections reordered successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid section order (missing, duplicate, or invalid IDs)"),
			@ApiResponse(responseCode = "403", description = "You are not allowed to reorder sections for this course"),
			@ApiResponse(responseCode = "404", description = "Course not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<project.ktc.springboot_app.common.dto.ApiResponse<Void>> reorderSections(
			@Parameter(description = "Course ID", required = true) @PathVariable("courseId") String courseId,
			@Parameter(description = "Section reorder data containing complete ordered list of section IDs", required = true) @Valid @RequestBody ReorderSectionsDto reorderSectionsDto) {

		log.info(
				"Received request to reorder sections for course: {} with order: {}",
				courseId,
				reorderSectionsDto.getSectionOrder());

		String instructorId = SecurityUtil.getCurrentUserId();

		// Call service to reorder sections
		return sectionService.reorderSections(courseId, instructorId, reorderSectionsDto);
	}
}
