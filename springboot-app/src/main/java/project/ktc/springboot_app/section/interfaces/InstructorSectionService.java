package project.ktc.springboot_app.section.interfaces;

import org.springframework.http.ResponseEntity;

import project.ktc.springboot_app.common.dto.ApiResponse;
import project.ktc.springboot_app.section.dto.CreateSectionDto;
import project.ktc.springboot_app.section.dto.ReorderSectionsDto;
import project.ktc.springboot_app.section.dto.SectionResponseDto;
import project.ktc.springboot_app.section.dto.SectionWithLessonsDto;

import java.util.List;

public interface InstructorSectionService {

    ResponseEntity<ApiResponse<List<SectionWithLessonsDto>>> getCourseSections(
            String courseId,
            String instructorId);

    ResponseEntity<ApiResponse<SectionResponseDto>> createSection(
            String courseId,
            String instructorId,
            CreateSectionDto createSectionDto);

    ResponseEntity<ApiResponse<SectionResponseDto>> updateSection(
            String courseId,
            String sectionId,
            String instructorId,
            CreateSectionDto updateSectionDto);

    ResponseEntity<ApiResponse<Void>> deleteSection(
            String courseId,
            String sectionId,
            String instructorId);

    ResponseEntity<ApiResponse<Void>> reorderSections(
            String courseId,
            String instructorId,
            ReorderSectionsDto reorderSectionsDto);
}
